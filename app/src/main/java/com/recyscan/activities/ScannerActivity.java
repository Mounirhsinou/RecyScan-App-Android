package com.recyscan.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.recyscan.R;
import com.recyscan.utils.Constants;
import com.recyscan.utils.LocaleHelper;
import com.recyscan.utils.RecyclingMapper;
import com.recyscan.utils.WasteClassifier;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI Waste Scanner Activity.
 * Uses CameraX and TensorFlow Lite to detect waste materials in real-time.
 */
public class ScannerActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────────────────
    private PreviewView viewFinder;
    private View layoutAiResult;
    private TextView tvDetectedObject, tvConfidence, tvAiInstructions, scannerStatus;
    private ProgressBar scannerProgressBar;
    private View flashButton;

    // ── Threading ───────────────────────────────────────────────────────────────
    private ExecutorService cameraExecutor;
    private ExecutorService inferenceExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ── AI / Camera ────────────────────────────────────────────────────────────
    private WasteClassifier classifier;
    private Camera camera;
    private boolean isFlashOn = false;

    // ── Frame gating ───────────────────────────────────────────────────────────
    /** Prevents a new inference from starting while one is still in-flight. */
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    /** Minimum interval between two inference attempts (ms). */
    private static final long ANALYSIS_INTERVAL_MS = 500; // analyze every 500 ms
    private volatile long lastAnalysisTimestamp = 0;

    // ── Prediction smoothing ───────────────────────────────────────────────────
    /** Label that was detected on the previous accepted frame. */
    private String lastDetectedLabel = "";
    /** How many consecutive frames the same label has been top-1. */
    private int sameLabelCount = 0;
    /** Number of consistent frames required before showing a result. */
    private static final int STABLE_DETECTION_THRESHOLD = 3;
    /** Minimum confidence to consider a prediction valid. */
    private static final float CONFIDENCE_THRESHOLD = 0.85f; // require ≥85 % confidence

    // ── Staleness / auto-clear ─────────────────────────────────────────────────
    /** Timestamp of the last valid (above-threshold) detection. */
    private volatile long lastValidDetectionTime = 0;
    /** How long without a valid detection before we reset the UI (ms). */
    private static final long STALE_TIMEOUT_MS = 2000;
    /** Runnable that periodically checks for staleness. */
    private final Runnable stalenessChecker = new Runnable() {
        @Override
        public void run() {
            if (isFinishing() || isDestroyed()) return;
            long elapsed = System.currentTimeMillis() - lastValidDetectionTime;
            if (elapsed > STALE_TIMEOUT_MS && layoutAiResult.getVisibility() == View.VISIBLE) {
                clearPredictions();
            }
            mainHandler.postDelayed(this, 500);
        }
    };

    // ── Scanning animation ─────────────────────────────────────────────────────
    private Animation pulseAnimation;

    // ═══════════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        bindViews();
        initClassifier();
        initAnimations();

        // Two separate single-thread executors: one for CameraX, one for TFLite inference
        cameraExecutor = Executors.newSingleThreadExecutor();
        inferenceExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION);
        }

        setupButtons();

        // Start the staleness checker
        lastValidDetectionTime = System.currentTimeMillis();
        mainHandler.postDelayed(stalenessChecker, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        cameraExecutor.shutdown();
        inferenceExecutor.shutdown();
        if (classifier != null) {
            classifier.close();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Initialization
    // ═══════════════════════════════════════════════════════════════════════════

    private void bindViews() {
        viewFinder = findViewById(R.id.view_finder);
        layoutAiResult = findViewById(R.id.layout_ai_result);
        tvDetectedObject = findViewById(R.id.tv_detected_object);
        tvConfidence = findViewById(R.id.tv_confidence);
        tvAiInstructions = findViewById(R.id.tv_ai_instructions);
        scannerStatus = findViewById(R.id.scanner_status);
        flashButton = findViewById(R.id.btn_flash);

        // ProgressBar may not exist in older layouts – guard against null
        scannerProgressBar = findViewById(R.id.scanner_progress_bar);
    }

    private void initClassifier() {
        try {
            classifier = new WasteClassifier(this);
        } catch (IOException e) {
            String errorMsg = "KI-Modell 'waste_classifier.tflite' fehlt in 'app/src/main/assets/'. Bitte Datei hinzufügen.";
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initAnimations() {
        pulseAnimation = new AlphaAnimation(1.0f, 0.4f);
        pulseAnimation.setDuration(600);
        pulseAnimation.setRepeatMode(Animation.REVERSE);
        pulseAnimation.setRepeatCount(Animation.INFINITE);
    }

    private void setupButtons() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        flashButton.setOnClickListener(v -> toggleFlash());
        findViewById(R.id.btn_manual_input).setOnClickListener(v -> showManualInputDialog());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Camera Setup
    // ═══════════════════════════════════════════════════════════════════════════

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(224, 224))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Frame Analysis — The Core Fix
    //
    //  CRITICAL DESIGN:
    //   1. The analyzer callback fires on cameraExecutor.
    //   2. We IMMEDIATELY close imageProxy after capturing the bitmap.
    //      This ensures CameraX is never blocked waiting for us.
    //   3. Inference runs on a SEPARATE inferenceExecutor thread.
    //   4. AtomicBoolean prevents overlapping inference.
    // ═══════════════════════════════════════════════════════════════════════════

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        // ── Gate 1: Cooldown interval ──────────────────────────────────────────
        long now = System.currentTimeMillis();
        if (now - lastAnalysisTimestamp < ANALYSIS_INTERVAL_MS) {
            imageProxy.close(); // ALWAYS close
            return;
        }

        // ── Gate 2: Previous inference still running ───────────────────────────
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close(); // ALWAYS close
            return;
        }

        // Record the rotation before we leave the analyzer thread
        final int rotation = imageProxy.getImageInfo().getRotationDegrees();

        // ── IMMEDIATELY close imageProxy — never hold it open ──────────────────
        imageProxy.close();

        lastAnalysisTimestamp = now;

        // ── Capture bitmap from PreviewView on the UI thread ───────────────────
        mainHandler.post(() -> {
            Bitmap bitmap = null;
            try {
                if (viewFinder != null) {
                    bitmap = viewFinder.getBitmap();
                }
            } catch (Exception e) {
                // PreviewView not ready yet
            }

            if (bitmap == null) {
                isProcessing.set(false);
                return;
            }

            // Make a copy so the PreviewView can recycle its internal buffer
            final Bitmap frameBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            // ── Centered‑object detection ────────────────────────────────────────
            if (!isObjectCentered(frameBitmap)) {
                // Not centered – treat as no detection
                isProcessing.set(false);
                showNoDetection();
                if (!frameBitmap.isRecycled()) {
                    frameBitmap.recycle();
                }
                return;
            }
            // ── Blur detection ────────────────────────────────────────────────
            if (isImageBlurry(frameBitmap)) {
                // Release processing lock and show no detection UI
                isProcessing.set(false);
                showNoDetection();
                // Recycle bitmap to avoid memory leak
                if (!frameBitmap.isRecycled()) {
                    frameBitmap.recycle();
                }
                return;
            }

            // ── Dispatch inference to dedicated background thread ───────────────
            inferenceExecutor.execute(() -> {
                try {
                    if (classifier == null) return;

                    List<WasteClassifier.Recognition> results =
                            classifier.classify(frameBitmap, rotation);

                    // Update UI on main thread
                    mainHandler.post(() -> processResults(results));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Recycle the copy
                    if (!frameBitmap.isRecycled()) {
                        frameBitmap.recycle();
                    }
                    // Release the processing lock so the next frame can be analyzed
                    isProcessing.set(false);
                }
            });
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Result Processing — Smoothing & Confidence Filtering
    // ═══════════════════════════════════════════════════════════════════════════

    private void processResults(List<WasteClassifier.Recognition> results) {
        // ── Confidence threshold ───────────────────────────────────────────────
        if (results == null || results.isEmpty() || results.get(0).confidence < CONFIDENCE_THRESHOLD) {
            // Below threshold — treat as no detection
            sameLabelCount = Math.max(0, sameLabelCount - 1);
            if (sameLabelCount == 0) {
                lastDetectedLabel = "";
                showNoDetection();
            }
            return;
        }

        // We have a valid detection — update the staleness timer
        lastValidDetectionTime = System.currentTimeMillis();

        WasteClassifier.Recognition topResult = results.get(0);
        String currentLabel = topResult.label;

        // ── Smoothing: require STABLE_DETECTION_THRESHOLD consecutive matches ─
        if (currentLabel.equals(lastDetectedLabel)) {
            sameLabelCount++;
        } else {
            // New label — reset counter
            sameLabelCount = 1;
            lastDetectedLabel = currentLabel;
        }

        if (sameLabelCount >= STABLE_DETECTION_THRESHOLD) {
            // ── Stable detection achieved — show full result ───────────────────
            updateUiWithResults(results);
        } else {
            // ── "Scanning…" transitional state ─────────────────────────────────
            showScanningState("Erkennung... " + capitalize(currentLabel));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UI Updates
    // ═══════════════════════════════════════════════════════════════════════════

    /** Show the "scanning / loading" state with optional pulse animation. */
    private void showScanningState(String message) {
        layoutAiResult.setVisibility(View.GONE);
        scannerStatus.setVisibility(View.VISIBLE);
        scannerStatus.setText(message);

        if (scannerProgressBar != null) {
            scannerProgressBar.setVisibility(View.VISIBLE);
        }

        // Pulse animation on the status text
        if (scannerStatus.getAnimation() == null) {
            scannerStatus.startAnimation(pulseAnimation);
        }
    }

    /** Show UI for "no recyclable object detected" state. */
    private void showNoDetection() {
        // Use the same scanning UI but with a clear message and hide progress bar
        layoutAiResult.setVisibility(View.GONE);
        scannerStatus.setVisibility(View.VISIBLE);
        scannerStatus.setText(R.string.scanning);
        if (scannerProgressBar != null) {
            scannerProgressBar.setVisibility(View.GONE);
        }
        // Stop any pulse animation
        scannerStatus.clearAnimation();
    }

    /** Clear all predictions and reset to the idle state. */
    private void clearPredictions() {
        layoutAiResult.setVisibility(View.GONE);
        scannerStatus.setVisibility(View.VISIBLE);
        scannerStatus.setText(R.string.hold_camera);
        scannerStatus.clearAnimation();

        if (scannerProgressBar != null) {
            scannerProgressBar.setVisibility(View.GONE);
        }

        // Reset smoothing state
        sameLabelCount = 0;
        lastDetectedLabel = "";
    }

    /** Display a confirmed detection result in the overlay panel. */
    private void updateUiWithResults(List<WasteClassifier.Recognition> results) {
        if (results == null || results.isEmpty()) {
            clearPredictions();
            return;
        }

        WasteClassifier.Recognition topResult = results.get(0);

        // Stop scanning animations
        scannerStatus.clearAnimation();
        scannerStatus.setVisibility(View.GONE);
        if (scannerProgressBar != null) {
            scannerProgressBar.setVisibility(View.GONE);
        }

        // Show result panel
        layoutAiResult.setVisibility(View.VISIBLE);

        String material = topResult.label;
        String binType = RecyclingMapper.fromAiLabel(material);
        String instructions = RecyclingMapper.getInstructions(this, binType);
        String localizedBinName = RecyclingMapper.getLocalizedBinName(this, binType);

        tvDetectedObject.setText(capitalize(material));
        tvConfidence.setText(getString(R.string.confidence_format, topResult.confidence * 100));
        tvAiInstructions.setText(getString(R.string.belongs_in, localizedBinName) + "\n\n" + instructions);

        // Color-code by bin type
        int colorRes;
        switch (binType) {
            case Constants.TYPE_GELBER_SACK: colorRes = R.color.yellow_primary; break;
            case Constants.TYPE_ALTPAPIER:   colorRes = R.color.blue_primary;   break;
            case Constants.TYPE_ALTGLAS:     colorRes = R.color.teal_primary;   break;
            case Constants.TYPE_BIOMUELL:    colorRes = R.color.brown_primary;  break;
            default:                         colorRes = R.color.grey_medium;    break;
        }
        tvConfidence.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Utilities
    // ═══════════════════════════════════════════════════════════════════════════

    private String capitalize(String s) {
        if (s == null || s.length() == 0) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).replace("_", " ");
    }

    /** Simple heuristic to determine if the main object is roughly centered.
     *  It extracts the central 50 % area of the bitmap and checks that its luminance
     *  variance exceeds a modest threshold, assuming a centered object creates
     *  enough visual variation compared to a background‑only frame.
     */
    private boolean isObjectCentered(Bitmap bitmap) {
        if (bitmap == null) return false;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // Define central region (25 % inset on each side → 50 % of width/height)
        int left = width / 4;
        int top = height / 4;
        int regionW = width / 2;
        int regionH = height / 2;
        int[] pixels = new int[regionW * regionH];
        bitmap.getPixels(pixels, 0, regionW, left, top, regionW, regionH);
        long sum = 0L, sumSq = 0L;
        for (int p : pixels) {
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = p & 0xFF;
            int gray = (r * 299 + g * 587 + b * 114) / 1000;
            sum += gray;
            sumSq += (long) gray * gray;
        }
        double variance = (sumSq - (sum * sum) / (double) pixels.length) / pixels.length;
        // Empirical threshold – low variance means mostly uniform background
        return variance > 800.0;
    }

    /** Simple blur detection based on pixel luminance variance. */
    private boolean isImageBlurry(Bitmap bitmap) {
        if (bitmap == null) return true;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        long sum = 0L;
        long sumSq = 0L;
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            int gray = (r * 299 + g * 587 + b * 114) / 1000; // luminance
            sum += gray;
            sumSq += (long) gray * gray;
        }
        double mean = sum / (double) pixels.length;
        double variance = (sumSq - (sum * sum) / (double) pixels.length) / pixels.length;
        // Low variance indicates a flat (potentially blurry) image.
        return variance < 1000.0; // empirically chosen threshold
    }

    private void toggleFlash() {
        if (camera != null) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Kamera-Berechtigung abgelehnt", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void showManualInputDialog() {
        // Implementation similar to previous version if needed
        Toast.makeText(this, "Manuelle Suche bald verfügbar", Toast.LENGTH_SHORT).show();
    }
}
