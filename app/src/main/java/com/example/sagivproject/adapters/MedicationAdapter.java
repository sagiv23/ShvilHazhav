package com.example.sagivproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.Medication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private Context context;
    private ArrayList<Medication> medications;
    private OnMedicationActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnMedicationActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    public MedicationAdapter(Context context, ArrayList<Medication> medications, OnMedicationActionListener listener) {
        this.context = context;
        this.medications = medications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication med = medications.get(position);

        holder.txtMedicationName.setText(med.getName());
        holder.txtMedicationDetails.setText("פרטים: " + med.getDetails());
        holder.txtMedicationDate.setText("תוקף: " + dateFormat.format(med.getDate()));

        Date today = new Date();
        int colorResId;

        if (med.getDate() != null && med.getDate().before(today)) {
            colorResId = android.R.color.holo_red_dark;
        } else {
            colorResId = R.color.text_color;
        }

        holder.txtMedicationDate.setTextColor(context.getColor(colorResId));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(position));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedicationName, txtMedicationDetails, txtMedicationDate;
        Button btnEdit, btnDelete;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicationName = itemView.findViewById(R.id.txtMedicationName);
            txtMedicationDetails = itemView.findViewById(R.id.txtMedicationDetails);
            txtMedicationDate = itemView.findViewById(R.id.txtMedicationDate);

            btnEdit = itemView.findViewById(R.id.btnEditMedication);
            btnDelete = itemView.findViewById(R.id.btnDeleteMedication);
        }
    }
}
