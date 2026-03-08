package com.example.sagivproject.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.ui.CustomTypefaceSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;

/**
 * A RecyclerView adapter for displaying a list of a user's {@link Medication} objects.
 */
public class MedicationListAdapter extends BaseAdapter<Medication, MedicationListAdapter.MedicationViewHolder> {
    private final Context context;
    private final Set<String> processingMedications = new HashSet<>();
    // Key: medicationId, Value: Map of scheduledTime -> MedicationStatus
    private final Map<String, Map<String, MedicationStatus>> loggedTodayMedications = new HashMap<>();
    private OnMedicationActionListener listener;

    @Inject
    public MedicationListAdapter(@ActivityContext Context context) {
        this.context = context;
    }

    public void setListener(OnMedicationActionListener listener) {
        this.listener = listener;
    }

    public void setMedications(List<Medication> medications) {
        setData(medications);
    }

    public void setLoggedTodayMedications(List<MedicationUsage> logs) {
        this.loggedTodayMedications.clear();
        if (logs != null) {
            for (MedicationUsage usage : logs) {
                String medId = usage.getId();
                String scheduledTime = usage.getScheduledTime();
                if (scheduledTime == null) continue;

                if (!loggedTodayMedications.containsKey(medId)) {
                    loggedTodayMedications.put(medId, new HashMap<>());
                }
                Objects.requireNonNull(loggedTodayMedications.get(medId)).put(scheduledTime, usage.getStatus());
            }
        }
        setData(new ArrayList<>(dataList));
    }

    public void addLoggedTodayMedication(MedicationUsage usage) {
        String medId = usage.getId();
        String scheduledTime = usage.getScheduledTime();
        if (scheduledTime != null) {
            if (!loggedTodayMedications.containsKey(medId)) {
                loggedTodayMedications.put(medId, new HashMap<>());
            }
            Objects.requireNonNull(loggedTodayMedications.get(medId)).put(scheduledTime, usage.getStatus());
        }
        setProcessingFinished(medId);
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication med = getItem(position);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.text_hebrew);

        if (typeface != null) {
            SpannableString nameSpannable = new SpannableString(med.getName());
            nameSpannable.setSpan(new CustomTypefaceSpan("", typeface), 0, nameSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.txtMedicationName.setText(nameSpannable);
        } else {
            holder.txtMedicationName.setText(med.getName());
        }

        if (med.getType() != null) {
            holder.txtMedicationType.setText(med.getType().getDisplayName());
            holder.txtMedicationType.setVisibility(View.VISIBLE);
        } else {
            holder.txtMedicationType.setVisibility(View.GONE);
        }

        holder.txtMedicationDetails.setText(med.getDetails());
        holder.txtMedicationHours.setVisibility(View.GONE); // Replaced by dynamic rows

        holder.statusContainer.removeAllViews();
        List<String> reminderHours = med.getReminderHours();
        if (reminderHours != null) {
            Map<String, MedicationStatus> medLogs = loggedTodayMedications.get(med.getId());
            for (String hour : reminderHours) {
                addStatusRow(holder.statusContainer, med, hour, medLogs != null ? medLogs.get(hour) : null);
            }
        }

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(context, v);
            menu.inflate(R.menu.menu_medication_item);
            if (typeface != null) {
                for (int i = 0; i < menu.getMenu().size(); i++) {
                    MenuItem item = menu.getMenu().getItem(i);
                    SpannableString s = new SpannableString(item.getTitle());
                    s.setSpan(new CustomTypefaceSpan("", typeface), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    s.setSpan(new AbsoluteSizeSpan(20, true), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    item.setTitle(s);
                }
            }
            menu.setOnMenuItemClickListener(item -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return false;
                Medication currentMed = getItem(currentPos);
                if (item.getItemId() == R.id.action_edit && listener != null) {
                    listener.onEdit(currentMed);
                    return true;
                } else if (item.getItemId() == R.id.action_delete && listener != null) {
                    listener.onDelete(currentMed);
                    return true;
                }
                return false;
            });
            menu.show();
        });
    }

    private void addStatusRow(LinearLayout container, Medication med, String scheduledTime, MedicationStatus status) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.item_medication_status_row, container, false);
        TextView txtTime = rowView.findViewById(R.id.txt_MedicationRow_ScheduledTime);
        Button btnTaken = rowView.findViewById(R.id.btn_MedicationRow_Taken);
        Button btnNotTaken = rowView.findViewById(R.id.btn_MedicationRow_NotTaken);
        Button btnSnoozed = rowView.findViewById(R.id.btn_MedicationRow_Snoozed);

        txtTime.setText(scheduledTime);

        boolean isLogged = status != null;
        boolean isProcessing = processingMedications.contains(med.getId());

        btnTaken.setEnabled(!isLogged && !isProcessing);
        btnNotTaken.setEnabled(!isLogged && !isProcessing);
        btnSnoozed.setEnabled(!isLogged && !isProcessing);

        if (isLogged) {
            switch (status) {
                case TAKEN:
                    btnTaken.setAlpha(1.0f);
                    btnNotTaken.setAlpha(0.3f);
                    btnSnoozed.setAlpha(0.3f);
                    break;
                case NOT_TAKEN:
                    btnTaken.setAlpha(0.3f);
                    btnNotTaken.setAlpha(1.0f);
                    btnSnoozed.setAlpha(0.3f);
                    break;
                case SNOOZED:
                    btnTaken.setAlpha(0.3f);
                    btnNotTaken.setAlpha(0.3f);
                    btnSnoozed.setAlpha(1.0f);
                    break;
            }
        }

        btnTaken.setOnClickListener(v -> handleStatusClick(med, scheduledTime, MedicationStatus.TAKEN));
        btnNotTaken.setOnClickListener(v -> handleStatusClick(med, scheduledTime, MedicationStatus.NOT_TAKEN));
        btnSnoozed.setOnClickListener(v -> handleStatusClick(med, scheduledTime, MedicationStatus.SNOOZED));

        container.addView(rowView);
    }

    private void handleStatusClick(Medication med, String scheduledTime, MedicationStatus status) {
        if (listener != null) {
            processingMedications.add(med.getId());
            notifyItemChanged(getItemList().indexOf(med));
            listener.onStatusChanged(med, scheduledTime, status);
        }
    }

    public void setProcessingFinished(String medicationId) {
        processingMedications.remove(medicationId);
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).getId().equals(medicationId)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public interface OnMedicationActionListener {
        void onEdit(Medication medication);

        void onDelete(Medication medication);

        void onStatusChanged(Medication medication, String scheduledTime, MedicationStatus status);
    }

    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        final TextView txtMedicationName, txtMedicationType, txtMedicationDetails, txtMedicationHours;
        final ImageButton btnMenu;
        final LinearLayout statusContainer;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicationName = itemView.findViewById(R.id.txt_MedicationRow_Name);
            txtMedicationType = itemView.findViewById(R.id.txt_MedicationRow_Type);
            txtMedicationDetails = itemView.findViewById(R.id.txt_MedicationRow_Details);
            txtMedicationHours = itemView.findViewById(R.id.txt_MedicationRow_Hours);
            btnMenu = itemView.findViewById(R.id.btn_MedicationRow_Menu);
            statusContainer = itemView.findViewById(R.id.layout_MedicationRow_StatusContainer);
        }
    }
}
