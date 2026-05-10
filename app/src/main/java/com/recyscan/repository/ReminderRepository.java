package com.recyscan.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.recyscan.database.AppDatabase;
import com.recyscan.database.ReminderDao;
import com.recyscan.models.Reminder;

import java.util.List;

/**
 * Repository for Reminder data.
 * Manages waste collection schedule reminders.
 */
public class ReminderRepository {

    private final ReminderDao reminderDao;

    public ReminderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        reminderDao = db.reminderDao();
    }

    /** Get all reminders */
    public LiveData<List<Reminder>> getAllReminders() {
        return reminderDao.getAllReminders();
    }

    /** Get enabled reminders (synchronous) */
    public List<Reminder> getEnabledRemindersSync() {
        return reminderDao.getEnabledRemindersSync();
    }

    /** Get reminder by ID */
    public LiveData<Reminder> getReminderById(int id) {
        return reminderDao.getReminderById(id);
    }

    /** Insert a reminder and return its ID */
    public void insert(Reminder reminder, OnReminderInsertedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = reminderDao.insert(reminder);
            if (callback != null) {
                callback.onInserted((int) id);
            }
        });
    }

    /** Update a reminder */
    public void update(Reminder reminder) {
        AppDatabase.databaseWriteExecutor.execute(() -> reminderDao.update(reminder));
    }

    /** Delete a reminder */
    public void delete(Reminder reminder) {
        AppDatabase.databaseWriteExecutor.execute(() -> reminderDao.delete(reminder));
    }

    /** Callback interface for insert operations */
    public interface OnReminderInsertedCallback {
        void onInserted(int id);
    }
}
