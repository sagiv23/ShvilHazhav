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
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.ui.CustomTypefaceSpan;

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

        Typeface typeface = ResourcesCompat.getFont(context, R.font.text_hebrew);

        if (typeface != null) {
            SpannableString nameSpannable = new SpannableString(med.getName());
            nameSpannable.setSpan(new CustomTypefaceSpan("", typeface), 0, nameSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.txtMedicationName.setText(nameSpannable);
        } else {
            holder.txtMedicationName.setText(med.getName());
        }

        holder.txtMedicationDetails.setText(med.getDetails());
        holder.txtMedicationDate.setText("תוקף: " + dateFormat.format(med.getDate()));

        if (med.getDate() != null && med.getDate().before(new Date())) {
            holder.txtMedicationDate.setTextColor(context.getColor(R.color.error));
        } else {
            holder.txtMedicationDate.setTextColor(context.getColor(R.color.text_color));
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
                int currentPos = holder.getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return false;

                if (item.getItemId() == R.id.action_edit) {
                    listener.onEdit(currentPos);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onDelete(currentPos);
                    return true;
                }
                return false;
            });

            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedicationName, txtMedicationDetails, txtMedicationDate;
        ImageButton btnMenu;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicationName = itemView.findViewById(R.id.txt_MedicationRow_Name);
            txtMedicationDetails = itemView.findViewById(R.id.txt_MedicationRow_Details);
            txtMedicationDate = itemView.findViewById(R.id.txt_MedicationRow_Date);
            btnMenu = itemView.findViewById(R.id.btn_MedicationRow_Menu);
        }
    }
}