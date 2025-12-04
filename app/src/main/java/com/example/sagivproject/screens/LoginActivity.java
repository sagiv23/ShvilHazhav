package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.ErrorTranslatorHelper;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.example.sagivproject.utils.Validator;

public class LoginActivity extends AppCompatActivity {
    private Button btnToContact, btnToLanding, btnToRegister, btnLogin;
    private EditText editTextEmail, editTextPassword;

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

        PagePermissions.redirectIfLoggedIn(this);

        btnToLanding = findViewById(R.id.btn_login_to_landing);
        btnToContact = findViewById(R.id.btn_login_to_contact);
        btnToRegister = findViewById(R.id.btn_login_to_register);
        btnLogin = findViewById(R.id.btnLogin);

        editTextEmail = findViewById(R.id.edt_login_email);
        editTextPassword = findViewById(R.id.edt_login_password);

        btnToLanding.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, LandingActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ContactActivity.class)));
        btnToRegister.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        btnLogin.setOnClickListener(view -> loginUser());

        String lastEmail = getIntent().getStringExtra("userEmail");
        if (lastEmail != null && !lastEmail.isEmpty()) {
            editTextEmail.setText(lastEmail);
        }
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא אימייל וסיסמה", Toast.LENGTH_SHORT).show();
        } else if (!Validator.isEmailValid(email)) {
            editTextEmail.setError("כתובת האימייל לא תקינה");
            editTextEmail.requestFocus();
            Toast.makeText(this, "כתובת האימייל לא תקינה", Toast.LENGTH_LONG).show();
        } else if (!Validator.isPasswordValid(password)) {
            editTextPassword.setError("הסיסמה קצרה מדי");
            editTextPassword.requestFocus();
            Toast.makeText(this, "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
        } else {
            DatabaseService.getInstance().getUserByEmailAndPassword(email, password, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    if (user == null) {
                        Toast.makeText(LoginActivity.this, "שגיאה בטעינת פרטי המשתמש", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferencesUtil.saveUser(LoginActivity.this, user);

                    Intent intent;
                    if (user.getIsAdmin()) {
                        Toast.makeText(LoginActivity.this, "התחברת למשתמש מנהל בהצלחה!", Toast.LENGTH_SHORT).show();
                        intent = new Intent(LoginActivity.this, AdminPageActivity.class);
                    } else {
                        Toast.makeText(LoginActivity.this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(LoginActivity.this, ErrorTranslatorHelper.getFriendlyFirebaseAuthError(e), Toast.LENGTH_LONG).show();
                    SharedPreferencesUtil.signOutUser(LoginActivity.this);
                }
            });
        }
    }
}
