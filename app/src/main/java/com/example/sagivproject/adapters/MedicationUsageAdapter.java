package com.example.sagivproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.MedicationUsage;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;

/**
 * A RecyclerView adapter for displaying a user's medication usage logs.
 * <p>
 * This adapter displays a historical record of when a medication was taken or missed.
 * It uses color-coding (e.g., green for taken, red for missed) to provide quick
 * visual feedback on the user's adherence to their medication schedule.
 * </p>
 */
public class MedicationUsageAdapter extends BaseAdapter<MedicationUsage, MedicationUsageAdapter.UsageViewHolder> {
    private final Context context;

    /**
     * Constructs a new MedicationUsageAdapter.
     * @param context The {@link ActivityContext} used for resource access.
     */
    @Inject
    public MedicationUsageAdapter(@ActivityContext Context context) { this.context = context; }

    @NonNull
    @Override
    public UsageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication_usage, parent, false);
        return new UsageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsageViewHolder holder, int position) {
        MedicationUsage usage = getItem(position);
        holder.txtName.setText(usage.getMedicationName());
        holder.txtDateTime.setText(String.format("%s %s", usage.getDate(), usage.getTime()));
        holder.txtStatus.setText(usage.getStatus().getDisplayName());

        int color;
        switch (usage.getStatus()) {
            case TAKEN:
                color = R.color.headline;
                break;
            case NOT_TAKEN:
                color = R.color.error;
                break;
            default:
                color = R.color.text_color;
        }
        holder.txtStatus.setTextColor(ContextCompat.getColor(context, color));
    }

    /** ViewHolder class for medication usage log rows. */
    public static class UsageViewHolder extends RecyclerView.ViewHolder {
        /** TextViews for medication name, date/time of the event, and intake status. */
        final TextView txtName, txtDateTime, txtStatus;

        /**
         * Constructs a new UsageViewHolder.
         * @param itemView The view representing a single log entry.
         */
        public UsageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_UsageRow_Name);
            txtDateTime = itemView.findViewById(R.id.txt_UsageRow_DateTime);
            txtStatus = itemView.findViewById(R.id.txt_UsageRow_Status);
        }
    }
}