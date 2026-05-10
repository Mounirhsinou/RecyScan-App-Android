package com.recyscan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.recyscan.R;
import com.recyscan.models.Reminder;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying waste collection reminders.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders = new ArrayList<>();
    private final OnReminderActionListener listener;

    public interface OnReminderActionListener {
        void onToggle(Reminder reminder, boolean enabled);
        void onDelete(Reminder reminder);
    }

    public ReminderAdapter(OnReminderActionListener listener) {
        this.listener = listener;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.tvTitle.setText(reminder.getTitle());
        holder.tvDay.setText(reminder.getDayName());
        holder.tvTime.setText(reminder.getFormattedTime());
        holder.switchEnabled.setChecked(reminder.isEnabled());

        holder.switchEnabled.setOnCheckedChangeListener((b, checked) -> listener.onToggle(reminder, checked));
        holder.itemView.setOnLongClickListener(v -> { listener.onDelete(reminder); return true; });
    }

    @Override
    public int getItemCount() { return reminders.size(); }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDay, tvTime;
        MaterialSwitch switchEnabled;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_reminder_title);
            tvDay = itemView.findViewById(R.id.tv_reminder_day);
            tvTime = itemView.findViewById(R.id.tv_reminder_time);
            switchEnabled = itemView.findViewById(R.id.switch_reminder_enabled);
        }
    }
}
