package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IAuthService;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity for new user registration.
 * <p>
 * This screen allows a new user to create an account by providing their first name,
 * last name, birthdate, email, and password. It includes input validation and
 * handles the registration process through the authentication service.
 * </p>
 */
@AndroidEntryPoint
public class RegisterActivity extends BaseActivity {
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextBirthDate;
    private long birthDateMillis = -1;

    /**
     * Initializes the activity, sets up the UI, input fields, and the registration button.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        Button btnRegister = findViewById(R.id.btnRegister);

        editTextFirstName = findViewById(R.id.edt_register_first_name);
        editTextLastName = findViewById(R.id.edt_register_last_name);
        editTextBirthDate = findViewById(R.id.edt_register_birth_date);
        editTextEmail = findViewById(R.id.edt_register_email);
        editTextPassword = findViewById(R.id.edt_register_password);

        editTextBirthDate.setFocusable(false);
        editTextBirthDate.setClickable(true);

        editTextBirthDate.setOnClickListener(v -> openDatePicker());
        btnRegister.setOnClickListener(view -> tryRegister());
    }

    /**
     * Attempts to register a new user with the provided details.
     * Validates the input before calling the registration service.
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
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validates all the user registration input fields.
     *
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param birthDate The user's birthdate as a string.
     * @param email     The user's email address.
     * @param password  The user's chosen password.
     * @return {@code true} if all inputs are valid, {@code false} otherwise.
     */
    private boolean validateInput(String firstName, String lastName, String birthDate, String email, String password) {
        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isNameNotValid(firstName)) {
            editTextFirstName.requestFocus();
            Toast.makeText(this, "שם פרטי קצר מדי", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isNameNotValid(lastName)) {
            editTextLastName.requestFocus();
            Toast.makeText(this, "שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
            return false;
        }

        if (birthDateMillis <= 0) {
            editTextBirthDate.requestFocus();
            Toast.makeText(this, "נא לבחור תאריך לידה", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isAgeNotValid(birthDateMillis)) {
            editTextBirthDate.requestFocus();
            Toast.makeText(this, "הגיל המינימלי להרשמה הוא 12", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isEmailNotValid(email)) {
            editTextEmail.requestFocus();
            Toast.makeText(this, "כתובת האימייל אינה תקינה", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isPasswordNotValid(password)) {
            editTextPassword.requestFocus();
            Toast.makeText(this, "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * Opens a date picker dialog to allow the user to select their birthdate.
     */
    private void openDatePicker() {
        calendarUtil.openDatePicker(this, birthDateMillis, (dateMillis, formattedDate) -> {
            this.birthDateMillis = dateMillis;
            editTextBirthDate.setText(formattedDate);
        });
    }
}
