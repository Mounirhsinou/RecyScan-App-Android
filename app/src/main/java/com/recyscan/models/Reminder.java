package com.recyscan.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Reminder entity for waste collection schedule.
 * Users can set reminders for different waste types on specific days.
 */
@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    @NonNull
    private String title;  // e.g., "Gelbe Tonne", "Biomüll"

    @ColumnInfo(name = "description")
    private String description;  // Additional info

    @ColumnInfo(name = "waste_type")
    @NonNull
    private String wasteType;  // Category of waste

    @ColumnInfo(name = "day_of_week")
    private int dayOfWeek;  // Calendar.MONDAY = 2, etc.

    @ColumnInfo(name = "hour")
    private int hour;  // Hour of reminder (0-23)

    @ColumnInfo(name = "minute")
    private int minute;  // Minute of reminder (0-59)

    @ColumnInfo(name = "enabled")
    private boolean enabled;  // Whether reminder is active

    @ColumnInfo(name = "color")
    private String color;  // Color identifier for the category

    // ========== Constructor ==========

    public Reminder(@NonNull String title, String description, @NonNull String wasteType,
                    int dayOfWeek, int hour, int minute, boolean enabled, String color) {
        this.title = title;
        this.description = description;
        this.wasteType = wasteType;
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.color = color;
    }

    // ========== Getters & Setters ==========

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @NonNull
    public String getWasteType() { return wasteType; }
    public void setWasteType(@NonNull String wasteType) { this.wasteType = wasteType; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    /**
     * Returns formatted time string (e.g., "07:00")
     */
    public String getFormattedTime() {
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Returns German day name for the day of week
     */
    public String getDayName() {
        switch (dayOfWeek) {
            case 1: return "Sonntag";
            case 2: return "Montag";
            case 3: return "Dienstag";
            case 4: return "Mittwoch";
            case 5: return "Donnerstag";
            case 6: return "Freitag";
            case 7: return "Samstag";
            default: return "Unbekannt";
        }
    }
}
