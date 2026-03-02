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

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A dialog for administrators to add a new user to the application.
 * <p>
 * This dialog provides a form for entering the new user's details, including first name,
 * last name, birthdate, email, and password. It includes input validation.
 * </p>
 */
@ActivityScoped
public class AddUserDialog {
    private final Context context;
    private final Validator validator;
    private final CalendarUtil calendarUtil;
    private long birthDateMillis = -1;

    /**
     * Constructs a new AddUserDialog.
     * Hilt uses this constructor to provide an instance.
     *
     * @param context      The context in which the dialog should be shown.
     * @param validator    The validator utility.
     * @param calendarUtil The calendar utility.
     */
    @Inject
    public AddUserDialog(@ActivityContext Context context, Validator validator, CalendarUtil calendarUtil) {
        this.context = context;
        this.validator = validator;
        this.calendarUtil = calendarUtil;
    }

    /**
     * Creates and displays the dialog, handling user input and validation.
     *
     * @param listener A listener to be notified when the add button is clicked.
     */
    public void show(AddUserDialogListener listener) {
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

        birthDateMillis = -1; // Reset for new dialog

        inputBirthDate.setOnClickListener(v -> calendarUtil.openDatePicker(context, birthDateMillis, (millis, dateStr) -> {
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

    private boolean areAllFieldsValid(String fName, String lName, String email, String password, EditText firstNameEdt, EditText lastNameEdt, EditText emailEdt, EditText passwordEdt, EditText birthDateEdt) {
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty() || birthDateMillis <= 0) {
            Toast.makeText(context, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        return isFieldValid(firstNameEdt, validator::isNameNotValid, "שם פרטי קצר מדי") &&
                isFieldValid(lastNameEdt, validator::isNameNotValid, "שם משפחה קצר מדי") &&
                isFieldValid(birthDateEdt, val -> validator.isAgeNotValid(birthDateMillis), "הגיל המינימלי הוא 12") &&
                isFieldValid(emailEdt, validator::isEmailNotValid, "כתובת האימייל לא תקינה") &&
                isFieldValid(passwordEdt, validator::isPasswordNotValid, "הסיסמה קצרה מדי");
    }

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
        void onAddUser(String fName, String lName, long birthDateMillis, String email, String password);
    }
}
