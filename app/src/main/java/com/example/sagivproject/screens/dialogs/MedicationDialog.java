package com.example.sagivproject.screens.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.Medication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicationDialog {
    public interface OnMedicationSubmitListener {
        void onAdd(Medication medication);
        void onEdit(Medication medication);
    }

    private final Context context;
    private final Medication medToEdit;
    private final String uid;
    private final SimpleDateFormat dateFormat;
    private final OnMedicationSubmitListener listener;

    public MedicationDialog(Context context, Medication medToEdit, String uid, SimpleDateFormat dateFormat, OnMedicationSubmitListener listener) {
        this.context = context;
        this.medToEdit = medToEdit;
        this.uid = uid;
        this.dateFormat = dateFormat;
        this.listener = listener;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_medication);

        EditText edtName = dialog.findViewById(R.id.edt_medication_name);
        EditText edtDetails = dialog.findViewById(R.id.edt_medication_details);
        EditText edtDate = dialog.findViewById(R.id.edt_medication_date);
        Button btnConfirm = dialog.findViewById(R.id.btn_add_medication_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_add_medication_cancel);

        if (medToEdit != null) {
            edtName.setText(medToEdit.getName());
            edtDetails.setText(medToEdit.getDetails());
            edtDate.setText(dateFormat.format(medToEdit.getDate()));
        }

        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (medToEdit != null && medToEdit.getDate() != null)
                calendar.setTime(medToEdit.getDate());

            DatePickerDialog picker = new DatePickerDialog(
                    context,
                    R.style.CustomDatePickerDialog,
                    (view, y, m, d) ->
                            edtDate.setText(String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    y, m + 1, d)),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            picker.getDatePicker().setMinDate(System.currentTimeMillis());
            picker.show();
        });

        btnConfirm.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String details = edtDetails.getText().toString().trim();
            String dateString = edtDate.getText().toString().trim();

            if (name.isEmpty() || details.isEmpty() || dateString.isEmpty()) {
                Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Date date = dateFormat.parse(dateString);

                if (medToEdit == null) {
                    listener.onAdd(new Medication(name, details, date, uid));
                } else {
                    medToEdit.setName(name);
                    medToEdit.setDetails(details);
                    medToEdit.setDate(date);
                    listener.onEdit(medToEdit);
                }

                dialog.dismiss();

            } catch (ParseException e) {
                Toast.makeText(context, "תאריך לא תקין", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}