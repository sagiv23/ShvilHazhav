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

public class MedicationUsageAdapter extends BaseAdapter<MedicationUsage, MedicationUsageAdapter.UsageViewHolder> {
    private final Context context;

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
                color = android.R.color.holo_green_dark;
                break;
            case NOT_TAKEN:
                color = android.R.color.holo_red_dark;
                break;
            case SNOOZED:
                color = android.R.color.holo_orange_dark;
                break;
            default:
                color = R.color.text_color;
        }
        holder.txtStatus.setTextColor(ContextCompat.getColor(context, color));
    }

    public static class UsageViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName, txtDateTime, txtStatus;

        public UsageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_UsageRow_Name);
            txtDateTime = itemView.findViewById(R.id.txt_UsageRow_DateTime);
            txtStatus = itemView.findViewById(R.id.txt_UsageRow_Status);
        }
    }
}
