package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A unified dialog fragment for adding a new user or editing an existing one.
 * <p>
 * This dialog adapts its UI (title, button text, and pre-filled data) based on whether
 * a {@link User} object is provided. It performs standardized validation before submission.
 * </p>
 */
@AndroidEntryPoint
public class UserDialog extends DialogFragment {
    private static final String ARG_USER = "arg_user";

    @Inject
    Validator validator;

    @Inject
    CalendarUtil calendarUtil;

    private User user;
    private long birthDateMillis = -1;
    private UserDialogListener listener;

    @Inject
    public UserDialog() {
    }

    /**
     * Sets the user data and listener.
     *
     * @param user     The {@link User} to edit, or null to add a new user.
     * @param listener The listener for submission events.
     */
    public void setData(@Nullable User user, UserDialogListener listener) {
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
        dialog.setContentView(R.layout.dialog_user);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        TextView txtTitle = dialog.findViewById(R.id.txtUserDialogTitle);
        EditText inputFirstName = dialog.findViewById(R.id.inputUserFirstName);
        EditText inputLastName = dialog.findViewById(R.id.inputUserLastName);
        EditText inputBirthDate = dialog.findViewById(R.id.inputUserBirthDate);
        EditText inputEmail = dialog.findViewById(R.id.inputUserEmail);
        EditText inputPassword = dialog.findViewById(R.id.inputUserPassword);
        Button btnSave = dialog.findViewById(R.id.btnUserSave);
        Button btnCancel = dialog.findViewById(R.id.btnUserCancel);

        boolean isEditMode = user != null;

        if (isEditMode) {
            txtTitle.setText(R.string.update_user_details);
            btnSave.setText(R.string.save);

            inputFirstName.setText(user.getFirstName());
            inputLastName.setText(user.getLastName());
            inputEmail.setText(user.getEmail());
            inputPassword.setText(user.getPassword());
            birthDateMillis = user.getBirthDateMillis();
            if (birthDateMillis > 0) {
                inputBirthDate.setText(calendarUtil.formatDate(birthDateMillis));
            }
        } else {
            txtTitle.setText(R.string.add_user);
            btnSave.setText(R.string.add);
        }

        inputBirthDate.setOnClickListener(v -> calendarUtil.openDatePicker(requireContext(), birthDateMillis, (millis, dateStr) -> {
            birthDateMillis = millis;
            inputBirthDate.setText(dateStr);
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT));

        btnSave.setOnClickListener(v -> {
            String fName = inputFirstName.getText().toString().trim();
            String lName = inputLastName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!areAllFieldsValid(fName, lName, email, password, inputFirstName, inputLastName, inputEmail, inputPassword, inputBirthDate)) {
                return;
            }

            if (listener != null) {
                listener.onSubmit(fName, lName, birthDateMillis, email, password);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
        return dialog;
    }

    private boolean areAllFieldsValid(String fName, String lName, String email, String password, EditText firstNameEdt, EditText lastNameEdt, EditText emailEdt, EditText passwordEdt, EditText birthDateEdt) {
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty() || birthDateEdt.getText().toString().isEmpty()) {
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

        if (birthDateMillis <= 0) {
            birthDateEdt.requestFocus();
            Toast.makeText(requireContext(), "תאריך הלידה אינו תקין, נא לבחור שוב", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isAgeNotValid(birthDateMillis)) {
            birthDateEdt.requestFocus();
            Toast.makeText(requireContext(), "הגיל המינימלי לשימוש באפליקציה הוא 12", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isEmailNotValid(email)) {
            emailEdt.requestFocus();
            Toast.makeText(requireContext(), "כתובת האימייל אינה תקינה (לדוגמה: example@mail.com)", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isPasswordNotValid(password)) {
            passwordEdt.requestFocus();
            Toast.makeText(requireContext(), "סיסמה חייבת להכיל לפחות 6 תווים", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public interface UserDialogListener {
        void onSubmit(String fName, String lName, long birthDateMillis, String email, String password);
    }
}