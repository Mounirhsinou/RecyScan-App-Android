package com.recyscan.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Helper class for managing dark/light theme.
 */
public final class ThemeHelper {

    private ThemeHelper() {}

    /**
     * Apply the saved theme preference (dark/light mode).
     */
    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(Constants.PREF_DARK_MODE, false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Toggle dark mode and save the preference.
     */
    public static void toggleDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        boolean currentMode = prefs.getBoolean(Constants.PREF_DARK_MODE, false);
        boolean newMode = !currentMode;

        prefs.edit().putBoolean(Constants.PREF_DARK_MODE, newMode).apply();

        if (newMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Check if dark mode is currently enabled.
     */
    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_DARK_MODE, false);
    }
}
