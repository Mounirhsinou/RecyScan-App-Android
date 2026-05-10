package com.recyscan;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.recyscan.utils.LocaleHelper;

/**
 * Application class for RecyScan.
 */
public class RecyScanApp extends Application {

    // Notification channel ID for waste collection reminders
    public static final String CHANNEL_REMINDERS = "recyscan_reminders";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    /**
     * Creates notification channels required for Android 8.0+.
     * Channel for waste collection day reminders.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Abfuhr-Erinnerungen",
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Erinnerungen an Müllabfuhr-Termine");
            reminderChannel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(reminderChannel);
            }
        }
    }
}
