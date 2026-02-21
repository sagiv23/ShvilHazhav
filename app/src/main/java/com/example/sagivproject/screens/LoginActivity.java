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
import com.example.sagivproject.utils.Validator;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity for user login.
 * <p>
 * This screen allows users to enter their email and password to log in.
 * It validates the input and handles the authentication process.
 * Upon successful login, it navigates to the appropriate main screen
 * (Admin or regular user).
 * </p>
 */
@AndroidEntryPoint
public class LoginActivity extends BaseActivity {
    private EditText editTextEmail, editTextPassword;

    /**
     * Initializes the activity, sets up the UI, and configures the login button.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        Button btnLogin = findViewById(R.id.btnLogin);

        editTextEmail = findViewById(R.id.edt_login_email);
        editTextPassword = findViewById(R.id.edt_login_password);

        btnLogin.setOnClickListener(view -> tryLogin());

        String lastEmail = getIntent().getStringExtra("userEmail");
        if (lastEmail != null && !lastEmail.isEmpty()) {
            editTextEmail.setText(lastEmail);
        }
    }

    /**
     * Attempts to log in the user with the provided credentials.
     * Validates the input before calling the authentication service.
     */
    private void tryLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        databaseService.getAuthService().login(email, password, new IAuthService.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                sharedPreferencesUtil.saveUser(user);

                Intent intent;

                if (user.isAdmin()) {
                    Toast.makeText(LoginActivity.this, "התחברת למשתמש מנהל בהצלחה!", Toast.LENGTH_SHORT).show();
                    intent = new Intent(LoginActivity.this, AdminPageActivity.class);
                } else {
                    Toast.makeText(LoginActivity.this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validates the user's email and password input.
     *
     * @param email    The email to validate.
     * @param password The password to validate.
     * @return {@code true} if the input is valid, {@code false} otherwise.
     */
    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא אימייל וסיסמה", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Validator.isEmailNotValid(email)) {
            editTextEmail.requestFocus();
            Toast.makeText(this, "כתובת האימייל אינה תקינה", Toast.LENGTH_LONG).show();
            return false;
        }

        if (Validator.isPasswordNotValid(password)) {
            editTextPassword.requestFocus();
            Toast.makeText(this, "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
