package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.enums.MedicationType;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment for adding a new medication or editing an existing one in the user's schedule.
 * <p>
 * This dialog provides a comprehensive form including:
 * <ul>
 * <li>Medication name and physical form (via a themed dropdown).</li>
 * <li>Dosage or specific intake instructions.</li>
 * <li>Multiple reminder hours, managed visually using {@link ChipGroup}.</li>
 * <li>Integration with {@link TimePickerDialog} for precise time selection.</li>
 * </ul>
 * It validates all inputs before submitting the data through the {@link OnMedicationSubmitListener}.
 * </p>
 */
@AndroidEntryPoint
public class MedicationDialog extends DialogFragment {
    private static final String ARG_MEDICATION = "arg_medication";

    /**
     * Internal list of selected reminder times in "HH:mm" format.
     */
    private final ArrayList<String> selectedHours = new ArrayList<>();

    private ChipGroup chipGroupSelectedHours;
    private Medication medToEdit;
    private OnMedicationSubmitListener listener;

    /**
     * Constructs a new MedicationDialog.
     */
    @Inject
    public MedicationDialog() {
    }

    /**
     * Sets the initial state of the dialog and the submission callback.
     *
     * @param medToEdit The medication record to modify, or null to create a new one.
     * @param listener  The listener to handle add or edit operations.
     */
    public void setData(Medication medToEdit, OnMedicationSubmitListener listener) {
        Bundle args = new Bundle();
        if (medToEdit != null) {
            args.putSerializable(ARG_MEDICATION, medToEdit);
        }
        setArguments(args);
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            medToEdit = BundleCompat.getSerializable(getArguments(), ARG_MEDICATION, Medication.class);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_medication);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        EditText edtName = dialog.findViewById(R.id.edt_medication_name);
        AutoCompleteTextView spinnerType = dialog.findViewById(R.id.spinner_medication_type);
        EditText edtDetails = dialog.findViewById(R.id.edt_medication_details);
        Button btnSelectHours = dialog.findViewById(R.id.btn_select_hours);
        chipGroupSelectedHours = dialog.findViewById(R.id.chip_group_selected_hours);
        Button btnConfirm = dialog.findViewById(R.id.btn_add_medication_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_add_medication_cancel);

        selectedHours.clear();

        List<String> typeNames = new ArrayList<>();
        for (MedicationType type : MedicationType.values()) {
            typeNames.add(type.getDisplayName());
        }
        spinnerType.setAdapter(createMedicationTypeAdapter(typeNames));

        if (medToEdit != null) {
            edtName.setText(medToEdit.getName());
            edtDetails.setText(medToEdit.getDetails());
            if (medToEdit.getType() != null) {
                spinnerType.setText(medToEdit.getType().getDisplayName(), false);
            }
            if (medToEdit.getReminderHours() != null && !medToEdit.getReminderHours().isEmpty()) {
                selectedHours.addAll(medToEdit.getReminderHours());
                updateSelectedHoursChips();
            }
        }

        btnSelectHours.setOnClickListener(v -> showHourPicker());

        btnConfirm.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String typeString = spinnerType.getText().toString();
            String details = edtDetails.getText().toString().trim();

            MedicationType selectedType = getTypeFromString(typeString);

            if (!validateInputs(name, typeString, details, selectedType)) {
                return;
            }

            Medication medicationData = new Medication();
            medicationData.setName(name);
            medicationData.setDetails(details);
            medicationData.setType(selectedType);
            medicationData.setReminderHours(new ArrayList<>(selectedHours));

            if (medToEdit == null) {
                if (listener != null) listener.onAdd(medicationData);
            } else {
                medicationData.setId(medToEdit.getId());
                if (listener != null) listener.onEdit(medicationData);
            }

            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return dialog;
    }

    /**
     * Validates that all required fields are populated correctly.
     *
     * @return true if input is valid.
     */
    private boolean validateInputs(String name, String typeString, String details, MedicationType selectedType) {
        if (name.isEmpty() || typeString.isEmpty() || details.isEmpty()) {
            showToast("אנא מלא את כל השדות");
            return false;
        }
        if (selectedHours.isEmpty()) {
            showToast("אנא בחר לפחות שעת תזכורת אחת");
            return false;
        }
        if (selectedType == null) {
            showToast("אנא בחר סוג תרופה");
            return false;
        }
        return true;
    }

    /**
     * Maps a display name string back to its corresponding enum constant.
     */
    private MedicationType getTypeFromString(String typeString) {
        for (MedicationType type : MedicationType.values()) {
            if (type.getDisplayName().equals(typeString)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Opens the system time picker to add a new reminder hour.
     */
    private void showHourPicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                R.style.TimePickerCustomTheme,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
                    if (!selectedHours.contains(time)) {
                        selectedHours.add(time);
                        updateSelectedHoursChips();
                    }
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    /**
     * Refreshes the display of chosen reminder times using Material Chips.
     */
    private void updateSelectedHoursChips() {
        chipGroupSelectedHours.removeAllViews();
        Collections.sort(selectedHours);
        for (String hour : selectedHours) {
            Chip chip = new Chip(requireContext());
            chip.setText(hour);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedHours.remove(hour);
                updateSelectedHoursChips();
            });
            chipGroupSelectedHours.addView(chip);
        }
    }

    /**
     * Creates a styled adapter for the medication form dropdown.
     */
    private ArrayAdapter<String> createMedicationTypeAdapter(List<String> typeNames) {
        return new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                typeNames
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                styleTextView(tv, false);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                styleTextView(tv, true);
                return tv;
            }
        };
    }

    /**
     * Applies custom Hebrew typography and sizing to dropdown items.
     */
    private void styleTextView(TextView tv, boolean isDropdown) {
        tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.text_hebrew));
        tv.setTextSize(22);
        tv.setTextColor(requireContext().getColor(R.color.text_color));
        tv.setPadding(24, 24, 24, 24);

        if (isDropdown) {
            tv.setBackgroundColor(
                    requireContext().getColor(R.color.background_color_buttons)
            );
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Listener interface for returning medication data results.
     */
    public interface OnMedicationSubmitListener {
        /**
         * Called when a new medication is ready to be added.
         */
        void onAdd(Medication medication);

        /**
         * Called when existing medication details have been modified.
         */
        void onEdit(Medication medication);
    }
}