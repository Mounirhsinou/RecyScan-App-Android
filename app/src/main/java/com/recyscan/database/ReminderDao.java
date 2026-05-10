package com.recyscan.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.recyscan.models.Reminder;

import java.util.List;

/**
 * Data Access Object for Reminder entity.
 * Manages waste collection schedule reminders.
 */
@Dao
public interface ReminderDao {

    /** Get all reminders ordered by day of week and time */
    @Query("SELECT * FROM reminders ORDER BY day_of_week ASC, hour ASC, minute ASC")
    LiveData<List<Reminder>> getAllReminders();

    /** Get all enabled reminders (synchronous, for scheduling) */
    @Query("SELECT * FROM reminders WHERE enabled = 1")
    List<Reminder> getEnabledRemindersSync();

    /** Get reminder by ID */
    @Query("SELECT * FROM reminders WHERE id = :id")
    LiveData<Reminder> getReminderById(int id);

    /** Get reminder by ID (synchronous) */
    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderByIdSync(int id);

    /** Insert a new reminder */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Reminder reminder);

    /** Update an existing reminder */
    @Update
    void update(Reminder reminder);

    /** Delete a reminder */
    @Delete
    void delete(Reminder reminder);

    /** Delete all reminders */
    @Query("DELETE FROM reminders")
    void deleteAll();
}
