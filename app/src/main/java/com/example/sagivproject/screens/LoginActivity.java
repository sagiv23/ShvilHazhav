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

import com.example.sagivproject.models.AuthHelper;
import com.example.sagivproject.models.FirebaseErrorsHelper;
import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

public class LoginActivity extends AppCompatActivity {
    private Button btnToContact, btnToMain, btnToRegister, btnLogin;
    private EditText editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        if (!AuthHelper.checkUserLoggedInFromspecialActivities(LoginActivity.this)) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToContact = findViewById(R.id.btnLoginPageToContactPage);
        btnToMain = findViewById(R.id.btnLoginPageToMainPage);
        btnToRegister = findViewById(R.id.btnLoginPageToRegisterPage);
        btnLogin = findViewById(R.id.btnLogin);

        editTextEmail = findViewById(R.id.LoginEditTextEmail);
        editTextPassword = findViewById(R.id.LoginEditTextPassword);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ContactActivity.class)));
        btnToMain.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, MainActivity.class)));
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
            return;
        }

        DatabaseService.getInstance().getUserByEmailAndPassword(email, password, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user == null) {
                    Toast.makeText(LoginActivity.this, "שגיאה בטעינת פרטי המשתמש", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferencesUtil.saveUser(LoginActivity.this, user);
                AuthHelper.checkUserIsAdmin(LoginActivity.this);

                Toast.makeText(LoginActivity.this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                String errorMessage = FirebaseErrorsHelper.getFriendlyFirebaseAuthError(e);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
