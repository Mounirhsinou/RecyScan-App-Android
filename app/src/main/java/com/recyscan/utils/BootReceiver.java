package com.recyscan.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.recyscan.database.AppDatabase;
import com.recyscan.models.Reminder;

import java.util.List;

/**
 * BootReceiver re-schedules all enabled reminders after device restart.
 * Alarms are lost when the device reboots, so we need to reschedule them.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Re-schedule all enabled reminders on a background thread
            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(context);
                List<Reminder> enabledReminders = db.reminderDao().getEnabledRemindersSync();

                for (Reminder reminder : enabledReminders) {
                    ReminderReceiver.scheduleReminder(context, reminder);
                }
            });
        }
    }
}
