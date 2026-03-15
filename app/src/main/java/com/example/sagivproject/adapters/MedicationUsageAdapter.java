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
 * This adapter shows the medication name, the date and time it was recorded,
 * and the status of the intake (e.g., TAKEN, NOT_TAKEN).
 * It uses different colors to visually indicate the status.
 * </p>
 */
public class MedicationUsageAdapter extends BaseAdapter<MedicationUsage, MedicationUsageAdapter.UsageViewHolder> {
    private final Context context;

    /**
     * Constructs a new MedicationUsageAdapter.
     *
     * @param context The activity context.
     */
    @Inject
    public MedicationUsageAdapter(@ActivityContext Context context) {
        this.context = context;
    }

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

    /**
     * ViewHolder for medication usage items.
     */
    public static class UsageViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName, txtDateTime, txtStatus;

        /**
         * Initializes the ViewHolder with the item view.
         *
         * @param itemView The view representing a single usage log entry.
         */
        public UsageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_UsageRow_Name);
            txtDateTime = itemView.findViewById(R.id.txt_UsageRow_DateTime);
            txtStatus = itemView.findViewById(R.id.txt_UsageRow_Status);
        }
    }
}
