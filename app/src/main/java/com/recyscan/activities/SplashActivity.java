package com.recyscan.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.recyscan.R;
import com.recyscan.utils.LocaleHelper;
import com.recyscan.utils.ThemeHelper;

/**
 * Splash screen with animated logo.
 * Automatically navigates to MainActivity after a short delay.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before super.onCreate
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find views
        ImageView logoIcon = findViewById(R.id.splash_logo_icon);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView tagline = findViewById(R.id.splash_tagline);

        // ========== Logo Scale + Fade Animation ==========
        AnimationSet logoAnim = new AnimationSet(true);

        // Scale from 0.5 to 1.0
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.5f, 1.0f, 0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(800);

        // Fade in
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);

        logoAnim.addAnimation(scaleAnim);
        logoAnim.addAnimation(fadeIn);
        logoAnim.setFillAfter(true);
        logoIcon.startAnimation(logoAnim);

        // ========== App Name Fade In (delayed) ==========
        AlphaAnimation nameAnim = new AlphaAnimation(0f, 1f);
        nameAnim.setDuration(600);
        nameAnim.setStartOffset(500);
        nameAnim.setFillAfter(true);
        appName.startAnimation(nameAnim);

        // ========== Tagline Fade In (more delayed) ==========
        AlphaAnimation taglineAnim = new AlphaAnimation(0f, 1f);
        taglineAnim.setDuration(600);
        taglineAnim.setStartOffset(900);
        taglineAnim.setFillAfter(true);
        tagline.startAnimation(taglineAnim);

        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}
