package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.utils.Validator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that handles the user authentication process.
 * <p>
 * This screen provides input fields for email and password, performs client-side validation
 * using the {@link Validator} utility, and communicates with {@link IAuthService}
 * to authenticate users. It manages redirection to either the Admin or Main dashboard
 * based on the user's role upon successful login.
 * </p>
 */
@AndroidEntryPoint
public class LoginActivity extends BaseActivity {
    /**
     * Utility for validating form inputs.
     */
    @Inject
    protected Validator validator;

    private EditText editTextEmail, editTextPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        Button btnLogin = findViewById(R.id.btnLogin);
        editTextEmail = findViewById(R.id.edt_login_email);
        editTextPassword = findViewById(R.id.edt_login_password);

        btnLogin.setOnClickListener(v -> tryLogin());

        String lastEmail = getIntent().getStringExtra("userEmail");
        if (lastEmail != null && !lastEmail.isEmpty()) {
            editTextEmail.setText(lastEmail);
        }
    }

    /**
     * Orchestrates the login flow: validation, service call, and navigation.
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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onError(String message) { Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show(); }
        });
    }

    /**
     * Validates that the login credentials meet application standards.
     * @param email The email string to check.
     * @param password The password string to check.
     * @return true if both inputs are formally valid.
     */
    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא אימייל וסיסמה", Toast.LENGTH_SHORT).show();
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
}