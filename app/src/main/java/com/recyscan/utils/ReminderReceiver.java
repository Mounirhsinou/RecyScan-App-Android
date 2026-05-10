package com.recyscan.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.recyscan.R;
import com.recyscan.RecyScanApp;
import com.recyscan.activities.MainActivity;
import com.recyscan.models.Reminder;

import java.util.Calendar;

/**
 * BroadcastReceiver that fires when a reminder alarm triggers.
 * Shows a notification reminding the user about waste collection.
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("reminder_title");
        String wasteType = intent.getStringExtra("reminder_waste_type");
        int reminderId = intent.getIntExtra(Constants.NOTIFICATION_REMINDER_ID, 0);

        if (title == null) title = "Müllabfuhr-Erinnerung";
        if (wasteType == null) wasteType = "";

        // Create intent to open app when notification is tapped
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, reminderId, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, RecyScanApp.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("♻️ " + title)
                .setContentText("Morgen wird " + wasteType + " abgeholt. Tonne rausstellen!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Morgen wird " + wasteType + " abgeholt. Bitte die Tonne heute Abend an die Straße stellen!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        // Show the notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(reminderId, builder.build());
        } catch (SecurityException e) {
            // Permission not granted - silently fail
            e.printStackTrace();
        }
    }

    /**
     * Schedule a recurring weekly alarm for a reminder.
     */
    public static void scheduleReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_title", reminder.getTitle());
        intent.putExtra("reminder_waste_type", reminder.getWasteType());
        intent.putExtra(Constants.NOTIFICATION_REMINDER_ID, reminder.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, reminder.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set up the alarm for the specified day and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, reminder.getDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed this week, schedule for next week
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        // Schedule repeating weekly alarm
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );
    }

    /**
     * Cancel a scheduled reminder alarm.
     */
    public static void cancelReminder(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, reminderId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
