package com.recyscan.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.recyscan.R;
import com.recyscan.adapters.ReminderAdapter;
import com.recyscan.models.Reminder;
import com.recyscan.utils.ReminderReceiver;
import com.recyscan.viewmodels.ReminderViewModel;

import java.util.Calendar;

/**
 * Reminder Fragment - Manages waste collection reminders.
 * Users can add, toggle, and delete reminders for different waste types.
 */
public class ReminderFragment extends Fragment implements ReminderAdapter.OnReminderActionListener {

    private ReminderViewModel viewModel;
    private ReminderAdapter adapter;
    private View emptyState;

    // Days and waste types for the add dialog
    private final String[] dayNames = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
    private final int[] dayValues = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
    private final String[] wasteTypes = {"Gelbe Tonne", "Biomüll", "Altpapier", "Restmüll", "Altglas"};
    private final String[] wasteColors = {"#F9A825", "#795548", "#1565C0", "#616161", "#00897B"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ReminderViewModel.class);

        RecyclerView rvReminders = view.findViewById(R.id.rv_reminders);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_reminder);
        emptyState = view.findViewById(R.id.layout_empty_reminders);

        // Setup RecyclerView
        adapter = new ReminderAdapter(this);
        rvReminders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReminders.setAdapter(adapter);

        // Observe reminders
        viewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
            adapter.setReminders(reminders);
            emptyState.setVisibility(reminders == null || reminders.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Add reminder button
        fabAdd.setOnClickListener(v -> showAddReminderDialog());
    }

    /**
     * Show dialog to add a new reminder.
     */
    private void showAddReminderDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_reminder, null);

        Spinner spinnerDay = dialogView.findViewById(R.id.spinner_day);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_waste_type);
        TextView tvTime = dialogView.findViewById(R.id.tv_selected_time);

        // Setup spinners
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, dayNames);
        spinnerDay.setAdapter(dayAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, wasteTypes);
        spinnerType.setAdapter(typeAdapter);

        // Default time
        final int[] selectedHour = {7};
        final int[] selectedMinute = {0};
        tvTime.setText("07:00");

        // Time picker
        tvTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                    (view1, hourOfDay, minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        tvTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }, selectedHour[0], selectedMinute[0], true);
            timePicker.show();
        });

        // Show dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Erinnerung hinzufügen")
                .setView(dialogView)
                .setPositiveButton("Speichern", (dialog, which) -> {
                    int dayIndex = spinnerDay.getSelectedItemPosition();
                    int typeIndex = spinnerType.getSelectedItemPosition();

                    Reminder reminder = new Reminder(
                            wasteTypes[typeIndex],
                            wasteTypes[typeIndex] + " wird abgeholt",
                            wasteTypes[typeIndex],
                            dayValues[dayIndex],
                            selectedHour[0],
                            selectedMinute[0],
                            true,
                            wasteColors[typeIndex]
                    );

                    viewModel.insert(reminder, id -> {
                        reminder.setId(id);
                        ReminderReceiver.scheduleReminder(requireContext(), reminder);
                    });
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    @Override
    public void onToggle(Reminder reminder, boolean enabled) {
        reminder.setEnabled(enabled);
        viewModel.update(reminder);

        if (enabled) {
            ReminderReceiver.scheduleReminder(requireContext(), reminder);
        } else {
            ReminderReceiver.cancelReminder(requireContext(), reminder.getId());
        }
    }

    @Override
    public void onDelete(Reminder reminder) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Erinnerung löschen?")
                .setMessage("Möchtest du diese Erinnerung wirklich löschen?")
                .setPositiveButton("Löschen", (d, w) -> {
                    ReminderReceiver.cancelReminder(requireContext(), reminder.getId());
                    viewModel.delete(reminder);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }
}
