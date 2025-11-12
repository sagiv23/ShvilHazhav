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

public class RegisterActivity extends AppCompatActivity {
    private Button btnToContact, btnToMain, btnToLogin, btnRegister;
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        if (!AuthHelper.checkUserLoggedInFromspecialActivities(RegisterActivity.this)) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToContact = findViewById(R.id.btnRegisterPageToContactPage);
        btnToMain = findViewById(R.id.btnRegisterPageToMainPage);
        btnToLogin = findViewById(R.id.btnRegisterPageToLoginPage);
        btnRegister = findViewById(R.id.btnRegister);

        editTextFirstName = findViewById(R.id.RegisterEditTextFirstName);
        editTextLastName = findViewById(R.id.RegisterEditTextLastName);
        editTextEmail = findViewById(R.id.RegisterEditTextEmail);
        editTextPassword = findViewById(R.id.RegisterEditTextPassword);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, ContactActivity.class)));
        btnToMain.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, MainActivity.class)));
        btnToLogin.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
        btnRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        } else if (firstName.length() < 2) {
            Toast.makeText(this, "שם פרטי קצר מדי", Toast.LENGTH_LONG).show();
            return;
        } else if (lastName.length() < 2) {
            Toast.makeText(this, "שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
            return;
        } else if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "כתובת האימייל לא תקינה", Toast.LENGTH_LONG).show();
            return;
        } else if (password.length() < 6) {
            Toast.makeText(this, "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            return;
        } else if (password.length() > 20) {
            Toast.makeText(this, "הסיסמה ארוכה מדי", Toast.LENGTH_LONG).show();
            return;
        }

        String uid = DatabaseService.getInstance().generateUserId();
        User newUser = new User(firstName, lastName, email, password, false, uid);

        DatabaseService.getInstance().checkIfEmailExists(email, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    Toast.makeText(RegisterActivity.this, "אימייל זה תפוס", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseService.getInstance().createNewUser(newUser, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            SharedPreferencesUtil.saveUser(RegisterActivity.this, newUser);
                            Toast.makeText(RegisterActivity.this, "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, HomePageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(RegisterActivity.this, "שגיאה בשמירת הנתונים: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            SharedPreferencesUtil.signOutUser(RegisterActivity.this);
                        }
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                String errorMessage = FirebaseErrorsHelper.getFriendlyFirebaseAuthError(e);
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}