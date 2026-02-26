package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.Validator;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A dialog for administrators to add a new user to the application.
 * <p>
 * This dialog provides a form for entering the new user's details, including first name,
 * last name, birthdate, email, and password. It includes input validation.
 * </p>
 */
public class AddUserDialog {
    private final Context context;
    private final AddUserDialogListener listener;
    private long birthDateMillis = -1;

    /**
     * Constructs a new AddUserDialog.
     *
     * @param context  The context in which the dialog should be shown.
     * @param listener A listener to be notified when the add button is clicked.
     */
    public AddUserDialog(Context context, AddUserDialogListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Creates and displays the dialog, handling user input and validation.
     */
    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_user);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        EditText inputFirstName = dialog.findViewById(R.id.inputAddUserFirstName);
        EditText inputLastName = dialog.findViewById(R.id.inputAddUserLastName);
        EditText inputBirthDate = dialog.findViewById(R.id.inputAddUserBirthDate);
        EditText inputEmail = dialog.findViewById(R.id.inputAddUserEmail);
        EditText inputPassword = dialog.findViewById(R.id.inputAddUserPassword);
        Button btnAdd = dialog.findViewById(R.id.btnAddUserSave);
        Button btnCancel = dialog.findViewById(R.id.btnAddUserCancel);

        inputBirthDate.setOnClickListener(v -> CalendarUtil.openDatePicker(context, birthDateMillis, (millis, dateStr) -> {
            birthDateMillis = millis;
            inputBirthDate.setText(dateStr);
        }));

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
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Sequentially validates all user input fields.
     *
     * @return True if all fields are valid, false otherwise.
     */
    private boolean areAllFieldsValid(String fName, String lName, String email, String password, EditText firstNameEdt, EditText lastNameEdt, EditText emailEdt, EditText passwordEdt, EditText birthDateEdt) {
        // Check for empty fields first
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty() || birthDateMillis <= 0) {
            Toast.makeText(context, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Chain validation checks
        return isFieldValid(firstNameEdt, Validator::isNameNotValid, "שם פרטי קצר מדי") &&
                isFieldValid(lastNameEdt, Validator::isNameNotValid, "שם משפחה קצר מדי") &&
                isFieldValid(birthDateEdt, val -> Validator.isAgeNotValid(birthDateMillis), "הגיל המינימלי הוא 12") &&
                isFieldValid(emailEdt, Validator::isEmailNotValid, "כתובת האימייל לא תקינה") &&
                isFieldValid(passwordEdt, Validator::isPasswordNotValid, "הסיסמה קצרה מדי");
    }

    /**
     * Validates a single EditText field using a predicate.
     *
     * @param editText  The EditText to validate.
     * @param predicate The validation logic to apply.
     * @param errorMsg  The error message to show if validation fails.
     * @return True if valid, false otherwise.
     */
    private boolean isFieldValid(EditText editText, Predicate<String> predicate, String errorMsg) {
        if (predicate.test(editText.getText().toString().trim())) {
            editText.requestFocus();
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * An interface to listen for the add user action.
     */
    public interface AddUserDialogListener {
        /**
         * Called when the user clicks the add button and all fields are valid.
         *
         * @param fName           The user's first name.
         * @param lName           The user's last name.
         * @param birthDateMillis The user's birthdate in milliseconds.
         * @param email           The user's email.
         * @param password        The user's password.
         */
        void onAddUser(String fName, String lName, long birthDateMillis, String email, String password);
    }
}
