package com.recyscan.utils;

/**
 * App-wide constants for RecyScan.
 */
public final class Constants {

    private Constants() {} // Prevent instantiation

    // ========== Intent Extras ==========
    public static final String EXTRA_BARCODE = "extra_barcode";
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_CATEGORY = "extra_category";

    // ========== Recycling Types ==========
    public static final String TYPE_GELBER_SACK = "Gelber Sack";
    public static final String TYPE_ALTPAPIER = "Altpapier";
    public static final String TYPE_BIOMUELL = "Biomüll";
    public static final String TYPE_ALTGLAS = "Altglas";
    public static final String TYPE_RESTMUELL = "Restmüll";
    public static final String TYPE_SONDERMUELL = "Sondermüll";

    // ========== Preferences ==========
    public static final String PREF_NAME = "recyscan_prefs";
    public static final String PREF_DARK_MODE = "dark_mode";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_LANGUAGE_CODE = "language_code";
    public static final String PREF_CITY = "city";
    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String PREF_FIRST_LAUNCH = "first_launch";

    // ========== Request Codes ==========
    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    public static final int REQUEST_BARCODE_SCAN = 200;

    // ========== Notification ==========
    public static final String NOTIFICATION_REMINDER_ID = "reminder_id";
}
