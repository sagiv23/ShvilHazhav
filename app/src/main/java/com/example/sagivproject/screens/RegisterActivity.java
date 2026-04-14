package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.Validator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that handles the user registration process.
 * <p>
 * This screen provides a form for new users to enter their personal details,
 * including name, birthdate, email, and password. It uses the {@link Validator}
 * and {@link CalendarUtil} for data validation and date picking. Upon successful
 * registration, the user is directed to the Emergency Contacts setup screen.
 * </p>
 */
@AndroidEntryPoint
public class RegisterActivity extends BaseActivity {
    /**
     * Utility for standardized date picking.
     */
    @Inject
    protected CalendarUtil calendarUtil;

    /**
     * Utility for validating form fields.
     */
    @Inject
    protected Validator validator;

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextBirthDate;
    /**
     * Holds the selected birthdate in milliseconds.
     */
    private long birthDateMillis = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_register, R.id.registerPage);
        setupMenu();

        findViewById(R.id.btnRegister).setOnClickListener(v -> tryRegister());
        editTextFirstName = findViewById(R.id.edt_register_first_name);
        editTextLastName = findViewById(R.id.edt_register_last_name);
        editTextBirthDate = findViewById(R.id.edt_register_birth_date);
        editTextEmail = findViewById(R.id.edt_register_email);
        editTextPassword = findViewById(R.id.edt_register_password);

        editTextBirthDate.setFocusable(false);
        editTextBirthDate.setClickable(true);
        editTextBirthDate.setOnClickListener(v -> openDatePicker());
    }

    /**
     * Attempts to register a new user using the form data.
     */
    private void tryRegister() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String birthDate = editTextBirthDate.getText().toString();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInput(firstName, lastName, birthDate, email, password)) {
            return;
        }

        databaseService.getAuthService().register(firstName, lastName, birthDateMillis, email, password, new IAuthService.RegisterCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(RegisterActivity.this, "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                sharedPreferencesUtil.saveUser(user);
                Intent intent = new Intent(RegisterActivity.this, EmergencyContactsActivity.class);
                intent.putExtra("isFromRegistration", true);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validates that all registration fields meet the required constraints.
     *
     * @param firstName User's first name.
     * @param lastName  User's last name.
     * @param birthDate Formatted birthdate string.
     * @param email     User's email.
     * @param password  User's password.
     * @return true if all inputs are valid.
     */
    private boolean validateInput(String firstName, String lastName, String birthDate, String email, String password) {
        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isNameNotValid(firstName)) {
            editTextFirstName.requestFocus();
            Toast.makeText(this, "שם פרטי חייב להכיל לפחות 2 תווים", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isNameNotValid(lastName)) {
            editTextLastName.requestFocus();
            Toast.makeText(this, "שם משפחה חייב להכיל לפחות 2 תווים", Toast.LENGTH_LONG).show();
            return false;
        }

        if (birthDateMillis <= 0) {
            editTextBirthDate.requestFocus();
            Toast.makeText(this, "תאריך הלידה אינו תקין, נא לבחור שוב", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isAgeNotValid(birthDateMillis)) {
            editTextBirthDate.requestFocus();
            Toast.makeText(this, "הגיל המינימלי לשימוש באפליקציה הוא 12", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isEmailNotValid(email)) {
            editTextEmail.requestFocus();
            Toast.makeText(this, "כתובת האימייל אינה תקינה (לדוגמה: example@mail.com)", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isPasswordNotValid(password)) {
            editTextPassword.requestFocus();
            Toast.makeText(this, "סיסמה חייבת להכיל לפחות 6 תווים", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * Displays the date picker dialog to select the user's birthdate.
     */
    private void openDatePicker() {
        calendarUtil.openDatePicker(this, birthDateMillis, (dateMillis, formattedDate) -> {
            this.birthDateMillis = dateMillis;
            editTextBirthDate.setText(formattedDate);
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT);
    }
}