package com.recyscan.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AI Classifier for recycling waste.
 * Loads a TFLite model and classifies images into material types.
 *
 * Thread-safety: This class is NOT thread-safe by itself.
 * The caller (ScannerActivity) ensures single-threaded access via AtomicBoolean gate.
 */
public class WasteClassifier {

    private static final String MODEL_PATH = "waste_classifier.tflite";
    private static final String LABELS_PATH = "labels.txt";

    private Interpreter interpreter;
    private List<String> labels;
    private int inputImageWidth = 224;
    private int inputImageHeight = 224;
    private boolean isSimulationMode = false;

    // ── Reusable inference buffers (allocated once) ────────────────────────────
    private ImageProcessor imageProcessor;
    private TensorBuffer outputBuffer;

    // ── Simulation mode helpers ────────────────────────────────────────────────
    private final Random simulationRng = new Random();
    private int simulationFrameCounter = 0;
    private int simulationCurrentIndex = 0;

    // ── Performance tracking ───────────────────────────────────────────────────
    private long lastInferenceTimeMs = 0;

    public WasteClassifier(Context context) throws IOException {
        try {
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, MODEL_PATH);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
            interpreter = new Interpreter(modelBuffer, options);
            labels = FileUtil.loadLabels(context, LABELS_PATH);

            // Get model input shape
            int[] inputShape = interpreter.getInputTensor(0).shape(); // e.g. [1, 224, 224, 3]
            inputImageHeight = inputShape[1];
            inputImageWidth = inputShape[2];

            // Pre-allocate reusable objects
            imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(0f, 255f)) // Normalize to [0, 1]
                    .build();

            int[] outputShape = interpreter.getOutputTensor(0).shape();
            DataType outputDataType = interpreter.getOutputTensor(0).dataType();
            outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

        } catch (Exception e) {
            // ── Fallback to Simulation Mode if model is missing ────────────────
            isSimulationMode = true;
            labels = new ArrayList<>();
            labels.add("plastic_bottle");
            labels.add("cardboard");
            labels.add("paper");
            labels.add("glass_bottle");
            labels.add("metal_can");
            labels.add("food_waste");
            labels.add("carton");
            labels.add("plastic_cup");
            labels.add("restmüll");
        }
    }

    public boolean isSimulationMode() {
        return isSimulationMode;
    }

    /** Returns the last TFLite inference time in milliseconds. Useful for performance monitoring. */
    public long getLastInferenceTimeMs() {
        return lastInferenceTimeMs;
    }

    /**
     * Classify a bitmap and return a sorted list of recognitions (highest confidence first).
     *
     * @param bitmap            The camera frame bitmap (any size; will be resized internally).
     * @param sensorOrientation Rotation degrees from the camera sensor (unused currently
     *                          because we capture from PreviewView which is already oriented).
     * @return Sorted list of Recognition results, never null but may be empty.
     */
    public List<Recognition> classify(Bitmap bitmap, int sensorOrientation) {
        if (isSimulationMode) {
            return simulateClassification();
        }

        if (bitmap == null || bitmap.isRecycled()) {
            return new ArrayList<>();
        }

        try {
            // ── Prepare input tensor ───────────────────────────────────────────
            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);

            // ── Run inference ──────────────────────────────────────────────────
            // Rewind the output buffer for a fresh write
            outputBuffer.getBuffer().rewind();

            long startTime = SystemClock.uptimeMillis();
            interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer().rewind());
            lastInferenceTimeMs = SystemClock.uptimeMillis() - startTime;

            // ── Map output to labels ───────────────────────────────────────────
            Map<String, Float> labeledProbability =
                    new TensorLabel(labels, outputBuffer).getMapWithFloatValue();

            List<Recognition> recognitions = new ArrayList<>(labeledProbability.size());
            for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
                recognitions.add(new Recognition(entry.getKey(), entry.getValue()));
            }

            // Sort by confidence descending
            Collections.sort(recognitions, (o1, o2) -> Float.compare(o2.confidence, o1.confidence));

            return recognitions;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Simulation Mode — Cycles through labels to avoid "stuck" appearance
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Produces realistic-looking simulated results that change over time,
     * so that the detection pipeline can be tested end-to-end even without
     * the real TFLite model.
     *
     * Behavior:
     *  - Every ~15 frames the "dominant" label changes.
     *  - Confidence is mildly randomized.
     *  - Occasionally returns low-confidence results (below typical threshold)
     *    so the smoothing / gating logic in ScannerActivity can be exercised.
     */
    private List<Recognition> simulateClassification() {
        simulationFrameCounter++;

        // Rotate to the next label every ~15 frames
        if (simulationFrameCounter % 15 == 0) {
            simulationCurrentIndex = (simulationCurrentIndex + 1) % labels.size();
        }

        List<Recognition> recognitions = new ArrayList<>();

        // Primary label with randomized confidence (0.55 – 0.98)
        float primaryConf = 0.55f + simulationRng.nextFloat() * 0.43f;

        // ~10 % of frames produce a weak detection to test threshold logic
        if (simulationRng.nextFloat() < 0.10f) {
            primaryConf = 0.20f + simulationRng.nextFloat() * 0.20f; // 0.20 – 0.40
        }

        recognitions.add(new Recognition(labels.get(simulationCurrentIndex), primaryConf));

        // Add a runner-up label
        int runnerUpIndex = (simulationCurrentIndex + 1) % labels.size();
        float runnerUpConf = Math.max(0.01f, (1f - primaryConf) * simulationRng.nextFloat());
        recognitions.add(new Recognition(labels.get(runnerUpIndex), runnerUpConf));

        // Sort descending
        Collections.sort(recognitions, (o1, o2) -> Float.compare(o2.confidence, o1.confidence));

        return recognitions;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Recognition DTO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Recognition result containing label and confidence.
     */
    public static class Recognition {
        public final String label;
        public final float confidence;

        public Recognition(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f%%)", label, confidence * 100);
        }
    }
}
