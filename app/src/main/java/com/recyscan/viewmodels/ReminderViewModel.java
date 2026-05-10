package com.recyscan.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.recyscan.models.Reminder;
import com.recyscan.repository.ReminderRepository;

import java.util.List;

/**
 * ViewModel for the Reminder screen.
 * Manages waste collection reminders.
 */
public class ReminderViewModel extends AndroidViewModel {

    private final ReminderRepository repository;
    private final LiveData<List<Reminder>> allReminders;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        repository = new ReminderRepository(application);
        allReminders = repository.getAllReminders();
    }

    /** Get all reminders */
    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    /** Get reminder by ID */
    public LiveData<Reminder> getReminderById(int id) {
        return repository.getReminderById(id);
    }

    /** Insert a new reminder */
    public void insert(Reminder reminder, ReminderRepository.OnReminderInsertedCallback callback) {
        repository.insert(reminder, callback);
    }

    /** Update a reminder */
    public void update(Reminder reminder) {
        repository.update(reminder);
    }

    /** Delete a reminder */
    public void delete(Reminder reminder) {
        repository.delete(reminder);
    }
}
