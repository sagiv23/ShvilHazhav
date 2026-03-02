package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.Validator;

import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A dialog for editing an existing user's profile information.
 * <p>
 * This dialog pre-fills a form with the user's current details and allows for modification.
 * It includes input validation and notifies a listener of the update request.
 * </p>
 */
@ActivityScoped
public class EditUserDialog {
    private final Context context;
    private final Validator validator;
    private final CalendarUtil calendarUtil;
    private long birthDateMillis = -1;

    /**
     * Constructs a new EditUserDialog.
     * Hilt uses this constructor to provide an instance.
     *
     * @param context      The context in which the dialog should be shown.
     * @param validator    The validator utility.
     * @param calendarUtil The calendar utility.
     */
    @Inject
    public EditUserDialog(@ActivityContext Context context, Validator validator, CalendarUtil calendarUtil) {
        this.context = context;
        this.validator = validator;
        this.calendarUtil = calendarUtil;
    }

    /**
     * Creates and displays the dialog, handling user input and validation.
     *
     * @param user     The user object to be edited.
     * @param listener A listener to be notified when the update button is clicked.
     */
    public void show(User user, EditUserDialogListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_user);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        EditText inputFirstName = dialog.findViewById(R.id.inputEditUserFirstName);
        EditText inputLastName = dialog.findViewById(R.id.inputEditUserLastName);
        EditText inputEmail = dialog.findViewById(R.id.inputEditUserEmail);
        EditText inputPassword = dialog.findViewById(R.id.inputEditUserPassword);
        EditText inputBirthDate = dialog.findViewById(R.id.inputEditUserBirthDate);
        Button btnSave = dialog.findViewById(R.id.btnEditUserSave);
        Button btnCancel = dialog.findViewById(R.id.btnEditUserCancel);

        // Initialize with existing user data
        birthDateMillis = user.getBirthDateMillis();
        updateBirthDateText(inputBirthDate, birthDateMillis);
        inputFirstName.setText(user.getFirstName());
        inputLastName.setText(user.getLastName());
        inputEmail.setText(user.getEmail());
        inputPassword.setText(user.getPassword());

        inputBirthDate.setOnClickListener(v -> calendarUtil.openDatePicker(context, birthDateMillis, (millis, dateStr) -> {
            birthDateMillis = millis;
            inputBirthDate.setText(dateStr);
        }));

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
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private boolean areAllFieldsValid(String fName, String lName, String email, String pass, EditText firstNameEdt, EditText lastNameEdt, EditText emailEdt, EditText passEdt, EditText birthDateEdt) {
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || pass.isEmpty() || birthDateMillis <= 0) {
            Toast.makeText(context, "כל השדות חובה", Toast.LENGTH_SHORT).show();
            return false;
        }

        return isFieldValid(firstNameEdt, validator::isNameNotValid, "שם פרטי קצר מדי") &&
                isFieldValid(lastNameEdt, validator::isNameNotValid, "שם משפחה קצר מדי") &&
                isFieldValid(birthDateEdt, val -> validator.isAgeNotValid(birthDateMillis), "הגיל המינימלי הוא 12") &&
                isFieldValid(emailEdt, validator::isEmailNotValid, "כתובת האימייל לא תקינה") &&
                isFieldValid(passEdt, validator::isPasswordNotValid, "הסיסמה קצרה מדי");
    }

    private boolean isFieldValid(EditText editText, Predicate<String> predicate, String errorMsg) {
        if (predicate.test(editText.getText().toString().trim())) {
            editText.requestFocus();
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void updateBirthDateText(EditText editText, long millis) {
        if (millis > 0) {
            editText.setText(calendarUtil.formatDate(millis));
        }
    }

    /**
     * An interface to listen for the update user action.
     */
    public interface EditUserDialogListener {
        void onUpdateUser(String fName, String lName, long birthDateMillis, String email, String password);
    }
}
