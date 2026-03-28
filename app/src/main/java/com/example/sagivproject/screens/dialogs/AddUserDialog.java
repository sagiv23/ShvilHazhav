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
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.Validator;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment that allows an administrator to manually add a new regular user.
 * <p>
 * This dialog collects user details including first name, last name, email, password,
 * and birthdate. It handles data validation via the {@link Validator} and provides
 * a date picker via {@link CalendarUtil}.
 * </p>
 */
@AndroidEntryPoint
public class AddUserDialog extends DialogFragment {
    /**
     * Utility for validating user input data.
     */
    @Inject
    Validator validator;

    /**
     * Utility for displaying a standardized date picker.
     */
    @Inject
    CalendarUtil calendarUtil;

    /**
     * The selected birthdate in milliseconds.
     */
    private long birthDateMillis = -1;

    /**
     * Listener for returning the new user data to the caller.
     */
    private AddUserDialogListener listener;

    /**
     * Constructs a new AddUserDialog.
     */
    @Inject
    public AddUserDialog() {
    }

    /**
     * Sets the submission listener.
     *
     * @param listener The {@link AddUserDialogListener} implementation.
     */
    public void setListener(AddUserDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_user);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        EditText inputFirstName = dialog.findViewById(R.id.inputAddUserFirstName);
        EditText inputLastName = dialog.findViewById(R.id.inputAddUserLastName);
        EditText inputBirthDate = dialog.findViewById(R.id.inputAddUserBirthDate);
        EditText inputEmail = dialog.findViewById(R.id.inputAddUserEmail);
        EditText inputPassword = dialog.findViewById(R.id.inputAddUserPassword);
        Button btnAdd = dialog.findViewById(R.id.btnAddUserSave);
        Button btnCancel = dialog.findViewById(R.id.btnAddUserCancel);

        inputBirthDate.setOnClickListener(v -> calendarUtil.openDatePicker(requireContext(), birthDateMillis, (millis, dateStr) -> {
            birthDateMillis = millis;
            inputBirthDate.setText(dateStr);
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT));

        btnAdd.setOnClickListener(v -> {
            String fName = inputFirstName.getText().toString().trim();
            String lName = inputLastName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!areAllFieldsValid(fName, lName, email, password, inputFirstName, inputLastName, inputEmail, inputPassword, inputBirthDate)) {
                return;
            }

            if (listener != null) {
                listener.onAddUser(fName, lName, birthDateMillis, email, password);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return dialog;
    }

    /**
     * Validates all required fields in the addition form.
     *
     * @return true if all data is formally valid.
     */
    private boolean areAllFieldsValid(String fName, String lName, String email, String password, EditText firstNameEdt, EditText lastNameEdt, EditText emailEdt, EditText passwordEdt, EditText birthDateEdt) {
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty() || birthDateMillis <= 0) {
            Toast.makeText(requireContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        return isFieldValid(firstNameEdt, validator::isNameNotValid, "שם פרטי קצר מדי") &&
                isFieldValid(lastNameEdt, validator::isNameNotValid, "שם משפחה קצר מדי") &&
                isFieldValid(birthDateEdt, val -> validator.isAgeNotValid(birthDateMillis), "הגיל המינימלי הוא 12") &&
                isFieldValid(emailEdt, validator::isEmailNotValid, "כתובת האימייל לא תקינה") &&
                isFieldValid(passwordEdt, validator::isPasswordNotValid, "הסיסמה קצרה מדי");
    }

    /**
     * Helper to validate a specific field and provide visual feedback on failure.
     */
    private boolean isFieldValid(EditText editText, Predicate<String> predicate, String errorMsg) {
        if (predicate.test(editText.getText().toString().trim())) {
            editText.requestFocus();
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Interface for communicating user creation events.
     */
    public interface AddUserDialogListener {
        /**
         * Called when the administrator submits a valid user profile.
         *
         * @param fName           First name.
         * @param lName           Last name.
         * @param birthDateMillis Birthdate in ms.
         * @param email           Email address.
         * @param password        Account password.
         */
        void onAddUser(String fName, String lName, long birthDateMillis, String email, String password);
    }
}
