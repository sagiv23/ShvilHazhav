package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.Validator;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment for editing an existing user's profile information.
 * <p>
 * This dialog allows administrators or users themselves to update personal details
 * such as name, email, password, and birthdate. It performs validation using
 * the {@link Validator} and {@link CalendarUtil} before applying changes.
 * </p>
 */
@AndroidEntryPoint
public class EditUserDialog extends DialogFragment {
    private static final String ARG_USER = "arg_user";

    /**
     * Utility for validating user input.
     */
    @Inject
    Validator validator;

    /**
     * Utility for date formatting and picking.
     */
    @Inject
    CalendarUtil calendarUtil;

    private User user;
    private long birthDateMillis = -1;
    private EditUserDialogListener listener;

    /**
     * Constructs a new EditUserDialog.
     */
    @Inject
    public EditUserDialog() {
    }

    /**
     * Sets the user data to be edited and the submission listener.
     *
     * @param user     The {@link User} profile to edit.
     * @param listener The listener to handle the update action.
     */
    public void setData(User user, EditUserDialogListener listener) {
        Bundle args = new Bundle();
        if (user != null) {
            args.putSerializable(ARG_USER, user);
        }
        setArguments(args);
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = BundleCompat.getSerializable(getArguments(), ARG_USER, User.class);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_edit_user);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        if (user == null) return dialog;

        EditText inputFirstName = dialog.findViewById(R.id.inputEditUserFirstName);
        EditText inputLastName = dialog.findViewById(R.id.inputEditUserLastName);
        EditText inputEmail = dialog.findViewById(R.id.inputEditUserEmail);
        EditText inputPassword = dialog.findViewById(R.id.inputEditUserPassword);
        EditText inputBirthDate = dialog.findViewById(R.id.inputEditUserBirthDate);
        Button btnSave = dialog.findViewById(R.id.btnEditUserSave);
        Button btnCancel = dialog.findViewById(R.id.btnEditUserCancel);

        birthDateMillis = user.getBirthDateMillis();
        if (birthDateMillis > 0) {
            inputBirthDate.setText(calendarUtil.formatDate(birthDateMillis));
        }
        inputFirstName.setText(user.getFirstName());
        inputLastName.setText(user.getLastName());
        inputEmail.setText(user.getEmail());
        inputPassword.setText(user.getPassword());

        inputBirthDate.setOnClickListener(v -> calendarUtil.openDatePicker(requireContext(), birthDateMillis, (millis, dateStr) -> {
            birthDateMillis = millis;
            inputBirthDate.setText(dateStr);
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT));

        btnSave.setOnClickListener(v -> {
            String fName = inputFirstName.getText().toString().trim();
            String lName = inputLastName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String pass = inputPassword.getText().toString().trim();

            if (!areAllFieldsValid(fName, lName, email, pass, inputFirstName, inputLastName, inputEmail, inputPassword, inputBirthDate)) {
                return;
            }

            if (listener != null) {
                listener.onUpdateUser(fName, lName, birthDateMillis, email, pass);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return dialog;
    }

    /**
     * Validates all input fields in the edit dialog.
     *
     * @return true if all fields are valid.
     */
    private boolean areAllFieldsValid(String fName, String lName, String email, String pass, EditText firstNameEdt, EditText lastNameEdt, EditText emailEdt, EditText passEdt, EditText birthDateEdt) {
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || pass.isEmpty() || birthDateMillis <= 0) {
            Toast.makeText(requireContext(), "כל השדות חובה", Toast.LENGTH_SHORT).show();
            return false;
        }

        return isFieldValid(firstNameEdt, validator::isNameNotValid, "שם פרטי קצר מדי") &&
                isFieldValid(lastNameEdt, validator::isNameNotValid, "שם משפחה קצר מדי") &&
                isFieldValid(birthDateEdt, val -> validator.isAgeNotValid(birthDateMillis), "הגיל המינימלי הוא 12") &&
                isFieldValid(emailEdt, validator::isEmailNotValid, "כתובת האימייל לא תקינה") &&
                isFieldValid(passEdt, validator::isPasswordNotValid, "הסיסמה קצרה מדי");
    }

    /**
     * Helper to validate a single field and show feedback on error.
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
     * Listener interface for profile update events.
     */
    public interface EditUserDialogListener {
        /**
         * Called when the user submits valid updated profile information.
         *
         * @param fName           Updated first name.
         * @param lName           Updated last name.
         * @param birthDateMillis Updated birthdate.
         * @param email           Updated email address.
         * @param password        Updated password.
         */
        void onUpdateUser(String fName, String lName, long birthDateMillis, String email, String password);
    }
}