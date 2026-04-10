package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.utils.Validator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment for adding a new emergency contact or editing an existing one.
 * <p>
 * This dialog provides input fields for the contact's first name, last name, and phone number.
 * It uses the {@link Validator} utility to ensure the input data is correct before submitting
 * it through the {@link AddEmergencyContactListener}.
 * </p>
 */
@AndroidEntryPoint
public class AddEmergencyContactDialog extends BaseDialog {

    /**
     * Utility for validating contact information.
     */
    @Inject
    Validator validator;

    private AddEmergencyContactListener listener;
    private EmergencyContact contactToEdit;

    /**
     * Constructs a new AddEmergencyContactDialog.
     */
    @Inject
    public AddEmergencyContactDialog() {
    }

    /**
     * Sets the listener for contact submission events.
     *
     * @param listener The listener to set.
     */
    public void setListener(AddEmergencyContactListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the initial data for the dialog.
     *
     * @param contact  The {@link EmergencyContact} to edit, or null to add a new one.
     * @param listener The listener for handling the result.
     */
    public void setData(EmergencyContact contact, AddEmergencyContactListener listener) {
        this.contactToEdit = contact;
        this.listener = listener;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_add_emergency_contact;
    }

    @Override
    protected void setupViews(Dialog dialog) {
        EditText etFirstName = dialog.findViewById(R.id.et_dialog_contact_first_name);
        EditText etLastName = dialog.findViewById(R.id.et_dialog_contact_last_name);
        EditText etPhone = dialog.findViewById(R.id.et_dialog_contact_phone);
        Button btnSave = dialog.findViewById(R.id.btn_dialog_save_contact);
        Button btnCancel = dialog.findViewById(R.id.btn_dialog_cancel_contact);

        if (contactToEdit != null) {
            etFirstName.setText(contactToEdit.getFirstName());
            etLastName.setText(contactToEdit.getLastName());
            etPhone.setText(contactToEdit.getPhoneNumber());
        }

        btnSave.setOnClickListener(v -> {
            String fName = etFirstName.getText().toString().trim();
            String lName = etLastName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (!areAllFieldsValid(fName, lName, phone, etFirstName, etLastName, etPhone)) {
                return;
            }

            if (listener != null) {
                listener.onContactSubmit(fName, lName, phone);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        contactToEdit = null;
    }

    /**
     * Validates all input fields in the dialog.
     *
     * @return true if all fields are valid.
     */
    private boolean areAllFieldsValid(String fName, String lName, String phone, EditText firstNameEdt, EditText lastNameEdt, EditText phoneEdt) {
        if (fName.isEmpty() || lName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isNameNotValid(fName)) {
            firstNameEdt.requestFocus();
            Toast.makeText(requireContext(), "שם פרטי חייב להכיל לפחות 2 תווים", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isNameNotValid(lName)) {
            lastNameEdt.requestFocus();
            Toast.makeText(requireContext(), "שם משפחה חייב להכיל לפחות 2 תווים", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isPhoneNotValid(phone)) {
            phoneEdt.requestFocus();
            Toast.makeText(requireContext(), "מספר הטלפון אינו תקין", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * Interface for listening to contact submission events.
     */
    public interface AddEmergencyContactListener {
        /**
         * Called when the contact details are submitted and valid.
         *
         * @param firstName   The entered first name.
         * @param lastName    The entered last name.
         * @param phoneNumber The entered phone number.
         */
        void onContactSubmit(String firstName, String lastName, String phoneNumber);
    }
}