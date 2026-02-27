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

/**
 * A dialog for editing an existing user's profile information.
 * <p>
 * This dialog pre-fills a form with the user's current details and allows for modification.
 * It includes input validation and notifies a listener of the update request.
 * </p>
 */
public class EditUserDialog {
    private final Context context;
    private final User user;
    private final EditUserDialogListener listener;
    private long birthDateMillis = -1;

    /**
     * An interface to listen for the update user action.
     */
    public interface EditUserDialogListener {
        /**
         * Called when the user clicks the update button and all fields are valid.
         *
         * @param fName           The user's updated first name.
         * @param lName           The user's updated last name.
         * @param birthDateMillis The user's updated birthdate in milliseconds.
         * @param email           The user's updated email.
         * @param password        The user's updated password.
         */
        void onUpdateUser(String fName, String lName, long birthDateMillis, String email, String password);
    }

    /**
     * Constructs a new EditUserDialog.
     *
     * @param context  The context in which the dialog should be shown.
     * @param user     The user object to be edited.
     * @param listener A listener to be notified when the update button is clicked.
     */
    public EditUserDialog(Context context, User user, EditUserDialogListener listener) {
        this.context = context;
        this.user = user;
        this.listener = listener;
    }

    /**
     * Creates and displays the dialog, handling user input and validation.
     */
    public void show() {
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

        inputBirthDate.setOnClickListener(v -> CalendarUtil.openDatePicker(context, birthDateMillis, (millis, dateStr) -> {
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

    /**
     * Sequentially validates all user input fields.
     *
     * @return True if all fields are valid, false otherwise.
     */
    private boolean areAllFieldsValid(String fName, String lName, String email, String pass, EditText firstNameEdt, EditText lastNameEdt, EditText emailEdt, EditText passEdt, EditText birthDateEdt) {
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || pass.isEmpty() || birthDateMillis <= 0) {
            Toast.makeText(context, "כל השדות חובה", Toast.LENGTH_SHORT).show();
            return false;
        }

        return isFieldValid(firstNameEdt, Validator::isNameNotValid, "שם פרטי קצר מדי") &&
                isFieldValid(lastNameEdt, Validator::isNameNotValid, "שם משפחה קצר מדי") &&
                isFieldValid(birthDateEdt, val -> Validator.isAgeNotValid(birthDateMillis), "הגיל המינימלי הוא 12") &&
                isFieldValid(emailEdt, Validator::isEmailNotValid, "כתובת האימייל לא תקינה") &&
                isFieldValid(passEdt, Validator::isPasswordNotValid, "הסיסמה קצרה מדי");
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
     * Helper method to set the text of the birthdate EditText.
     *
     * @param editText The EditText to update.
     * @param millis   The date in milliseconds.
     */
    private void updateBirthDateText(EditText editText, long millis) {
        if (millis > 0) {
            editText.setText(CalendarUtil.formatDate(millis));
        }
    }
}
