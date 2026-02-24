package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.Validator;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A dialog for editing an existing user's profile information.
 * <p>
 * This dialog pre-fills a form with the user's current details and allows for modification.
 * It includes input validation and uses the {@link IAuthService} to save the changes.
 * </p>
 */
public class EditUserDialog {
    private final Context context;
    private final User user;
    private final Runnable onSuccess;
    private final IAuthService authService;
    private long birthDateMillis = -1;

    /**
     * Constructs a new EditUserDialog.
     *
     * @param context     The context in which the dialog should be shown.
     * @param user        The user object to be edited.
     * @param onSuccess   A runnable to be executed when the user is successfully updated.
     * @param authService The authentication service to handle the update.
     */
    public EditUserDialog(Context context, User user, Runnable onSuccess, IAuthService authService) {
        this.context = context;
        this.user = user;
        this.onSuccess = onSuccess;
        this.authService = authService;
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

            // Validate all fields before proceeding
            if (!areAllFieldsValid(fName, lName, email, pass, inputFirstName, inputLastName, inputEmail, inputPassword, inputBirthDate)) {
                return;
            }

            authService.updateUser(user, fName, lName, birthDateMillis, email, pass, new IAuthService.UpdateUserCallback() {
                @Override
                public void onSuccess(User updatedUser) {
                    Toast.makeText(context, "הפרטים עודכנו!", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
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
        // Check for empty fields first
        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || pass.isEmpty() || birthDateMillis <= 0) {
            Toast.makeText(context, "כל השדות חובה", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Chain validation checks
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
