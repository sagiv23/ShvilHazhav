package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

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

/**
 * A dialog for adding or editing a user's medication.
 * <p>
 * This dialog provides a form to input medication details such as name, type, and dosage.
 * It also allows the user to select multiple reminder times using a {@link TimePickerDialog}
 * and displays them as chips.
 * </p>
 */
public class MedicationDialog {
    private final Context context;
    private final Medication medToEdit;
    private final OnMedicationSubmitListener listener;
    private final ArrayList<String> selectedHours = new ArrayList<>();
    private ChipGroup chipGroupSelectedHours;

    /**
     * Constructs a new MedicationDialog.
     *
     * @param context   The context in which the dialog should be shown.
     * @param medToEdit The medication to edit, or null to add a new one.
     * @param listener  The listener to be invoked when the form is submitted.
     */
    public MedicationDialog(Context context, Medication medToEdit, OnMedicationSubmitListener listener) {
        this.context = context;
        this.medToEdit = medToEdit;
        this.listener = listener;
    }

    /**
     * Creates and displays the dialog.
     */
    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_medication);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        EditText edtName = dialog.findViewById(R.id.edt_medication_name);
        AutoCompleteTextView spinnerType = dialog.findViewById(R.id.spinner_medication_type);
        EditText edtDetails = dialog.findViewById(R.id.edt_medication_details);
        Button btnSelectHours = dialog.findViewById(R.id.btn_select_hours);
        chipGroupSelectedHours = dialog.findViewById(R.id.chip_group_selected_hours);
        Button btnConfirm = dialog.findViewById(R.id.btn_add_medication_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_add_medication_cancel);

        // Populate spinner with medication types
        List<String> typeNames = new ArrayList<>();
        for (MedicationType type : MedicationType.values()) {
            typeNames.add(type.getDisplayName());
        }
        spinnerType.setAdapter(createMedicationTypeAdapter(typeNames));

        // If editing, pre-fill the form with existing data
        if (medToEdit != null) {
            edtName.setText(medToEdit.getName());
            edtDetails.setText(medToEdit.getDetails());
            if (medToEdit.getType() != null) {
                spinnerType.setText(medToEdit.getType().getDisplayName(), false);
            }
            if (medToEdit.getReminderHours() != null && !medToEdit.getReminderHours().isEmpty()) {
                selectedHours.clear();
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
            medicationData.setReminderHours(selectedHours);

            if (medToEdit == null) {
                listener.onAdd(medicationData);
            } else {
                medicationData.setId(medToEdit.getId());
                listener.onEdit(medicationData);
            }

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Validates all the required input fields.
     *
     * @param name         The medication name.
     * @param typeString   The selected medication type as a string.
     * @param details      The medication details.
     * @param selectedType The parsed MedicationType enum.
     * @return True if all inputs are valid, false otherwise.
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
     * Converts a string representation of a medication type to the corresponding enum.
     *
     * @param typeString The string to convert.
     * @return The MedicationType enum, or null if not found.
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
     * Shows a time picker dialog to allow the user to select a reminder time.
     */
    private void showHourPicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
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
     * Updates the ChipGroup to display the currently selected reminder times.
     */
    private void updateSelectedHoursChips() {
        chipGroupSelectedHours.removeAllViews();
        Collections.sort(selectedHours);
        for (String hour : selectedHours) {
            Chip chip = new Chip(context);
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
     * Creates and customizes an ArrayAdapter for the medication type spinner.
     *
     * @param typeNames The list of medication type display names.
     * @return A customized ArrayAdapter.
     */
    private ArrayAdapter<String> createMedicationTypeAdapter(List<String> typeNames) {
        return new ArrayAdapter<>(
                context,
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
     * Applies a custom style to the TextViews used in the spinner.
     *
     * @param tv         The TextView to style.
     * @param isDropdown True if the view is for the dropdown list, false for the selected item view.
     */
    private void styleTextView(TextView tv, boolean isDropdown) {
        tv.setTypeface(ResourcesCompat.getFont(context, R.font.text_hebrew));
        tv.setTextSize(22);
        tv.setTextColor(context.getColor(R.color.text_color));
        tv.setPadding(24, 24, 24, 24);

        if (isDropdown) {
            tv.setBackgroundColor(
                    context.getColor(R.color.background_color_buttons)
            );
        }
    }

    /**
     * Shows a simple Toast message.
     *
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * An interface for listeners that are invoked when the medication form is submitted.
     */
    public interface OnMedicationSubmitListener {
        /**
         * Called when a new medication is being added.
         *
         * @param medication The new medication data from the form.
         */
        void onAdd(Medication medication);

        /**
         * Called when an existing medication is being edited.
         *
         * @param medication The updated medication data from the form.
         */
        void onEdit(Medication medication);
    }
}
