package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying a user's medication usage logs.
 * <p>
 * This adapter displays a historical record of when a medication was taken or missed.
 * It uses color-coding (e.g., green for taken, red for missed) to provide quick
 * visual feedback on the user's adherence to their medication schedule.
 * </p>
 */
public class MedicationUsageAdapter extends BaseAdapter<MedicationUsage, MedicationUsageAdapter.UsageViewHolder> {
    /**
     * Map of medication IDs to Medication objects for name resolution.
     */
    private Map<String, Medication> medicationMap = new HashMap<>();

    /**
     * Listener for usage log actions.
     */
    private OnUsageActionListener listener;

    /**
     * Constructs a new MedicationUsageAdapter.
     */
    @Inject
    public MedicationUsageAdapter() {
    }

    /**
     * Sets the listener for usage-related actions.
     *
     * @param listener The {@link OnUsageActionListener} implementation.
     */
    public void setListener(OnUsageActionListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the medication map to resolve medication IDs to names.
     *
     * @param medicationMap A map of medication IDs to {@link Medication} objects.
     */
    public void setMedicationMap(Map<String, Medication> medicationMap) {
        this.medicationMap = medicationMap != null ? medicationMap : new HashMap<>();
        setData(new ArrayList<>(getItemList()));
    }

    @NonNull
    @Override
    public UsageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication_usage, parent, false);
        return new UsageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsageViewHolder holder, int position) {
        MedicationUsage usage = getItem(position);
        Medication medication = medicationMap.get(usage.getMedicationId());
        String name = medication != null ? medication.getName() : "תרופה לא ידועה";

        holder.txtName.setText(name);
        holder.txtDateTime.setText(usage.getTime());
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
        holder.txtStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), color));

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(usage);
            }
        });
    }

    /**
     * Interface for listening to actions on usage logs.
     */
    public interface OnUsageActionListener {
        /**
         * Called when the delete option is selected for a usage log.
         *
         * @param usage The {@link MedicationUsage} to delete.
         */
        void onDelete(MedicationUsage usage);
    }

    /**
     * ViewHolder class for medication usage log rows.
     */
    public static class UsageViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName, txtDateTime, txtStatus;
        final View btnDelete;

        /**
         * Constructs a new UsageViewHolder.
         *
         * @param itemView The view representing a single log entry.
         */
        public UsageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_UsageRow_Name);
            txtDateTime = itemView.findViewById(R.id.txt_UsageRow_DateTime);
            txtStatus = itemView.findViewById(R.id.txt_UsageRow_Status);
            btnDelete = itemView.findViewById(R.id.btn_UsageRow_Delete);
        }
    }
}