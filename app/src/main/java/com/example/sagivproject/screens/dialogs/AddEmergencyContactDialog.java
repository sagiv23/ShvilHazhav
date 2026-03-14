package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.utils.Validator;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog for adding or editing an emergency contact.
 */
@AndroidEntryPoint
public class AddEmergencyContactDialog extends DialogFragment {

    @Inject
    Validator validator;

    private AddEmergencyContactListener listener;
    private EmergencyContact contactToEdit;

    @Inject
    public AddEmergencyContactDialog() {
    }

    public void setListener(AddEmergencyContactListener listener) {
        this.listener = listener;
    }

    public void setData(EmergencyContact contact, AddEmergencyContactListener listener) {
        this.contactToEdit = contact;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_emergency_contact);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

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

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        contactToEdit = null; // Clear data on close
    }

    private boolean areAllFieldsValid(String fName, String lName, String phone, EditText firstNameEdt, EditText lastNameEdt, EditText phoneEdt) {
        if (fName.isEmpty() || lName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "כל השדות חובה", Toast.LENGTH_SHORT).show();
            return false;
        }

        return isFieldValid(firstNameEdt, validator::isNameNotValid, "שם פרטי קצר מדי") &&
                isFieldValid(lastNameEdt, validator::isNameNotValid, "שם משפחה קצר מדי") &&
                isFieldValid(phoneEdt, validator::isPhoneNotValid, "מספר טלפון לא תקין");
    }

    private boolean isFieldValid(EditText editText, Predicate<String> predicate, String errorMsg) {
        if (predicate.test(editText.getText().toString().trim())) {
            editText.requestFocus();
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public interface AddEmergencyContactListener {
        void onContactSubmit(String firstName, String lastName, String phoneNumber);
    }
}
