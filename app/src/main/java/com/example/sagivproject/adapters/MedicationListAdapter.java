package com.example.sagivproject.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.ui.CustomTypefaceSpan;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;

/**
 * A RecyclerView adapter for displaying a list of a user's {@link Medication} objects.
 */
public class MedicationListAdapter extends BaseAdapter<Medication, MedicationListAdapter.MedicationViewHolder> {
    private final Context context;
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
        List<String> reminderHours = med.getReminderHours();
        if (reminderHours != null && !reminderHours.isEmpty()) {
            holder.txtMedicationHours.setText(String.format("שעות: %s", TextUtils.join(", ", reminderHours)));
            holder.txtMedicationHours.setVisibility(View.VISIBLE);
        } else {
            holder.txtMedicationHours.setVisibility(View.GONE);
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

    public interface OnMedicationActionListener {
        void onEdit(Medication medication);

        void onDelete(Medication medication);
    }

    public static class MedicationViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final TextView txtMedicationName, txtMedicationType, txtMedicationDetails, txtMedicationHours;
        final ImageButton btnMenu;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicationName = itemView.findViewById(R.id.txt_MedicationRow_Name);
            txtMedicationType = itemView.findViewById(R.id.txt_MedicationRow_Type);
            txtMedicationDetails = itemView.findViewById(R.id.txt_MedicationRow_Details);
            txtMedicationHours = itemView.findViewById(R.id.txt_MedicationRow_Hours);
            btnMenu = itemView.findViewById(R.id.btn_MedicationRow_Menu);
        }
    }
}
