package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.example.sagivproject.utils.InputValidator;

import java.util.HashMap;

public class RegisterActivity extends BaseActivity {
    private Button btnToContact, btnToLanding, btnToLogin, btnRegister;
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword;

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

        btnToContact = findViewById(R.id.btn_register_to_contact);
        btnToLanding = findViewById(R.id.btn_register_to_landing);
        btnToLogin = findViewById(R.id.btn_register_to_login);
        btnRegister = findViewById(R.id.btnRegister);

        editTextFirstName = findViewById(R.id.edt_register_first_name);
        editTextLastName = findViewById(R.id.edt_register_last_name);
        editTextEmail = findViewById(R.id.edt_register_email);
        editTextPassword = findViewById(R.id.edt_register_password);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, ContactActivity.class)));
        btnToLanding.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LandingActivity.class)));
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
        } if (!InputValidator.isNameValid(firstName)) {
            editTextFirstName.requestFocus();
            Toast.makeText(this, "שם פרטי קצר מדי", Toast.LENGTH_LONG).show();
            return;
        } if (!InputValidator.isNameValid(lastName)) {
            editTextLastName.requestFocus();
            Toast.makeText(this, "שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
            return;
        } if (!InputValidator.isEmailValid(email)) {
            editTextEmail.requestFocus();
            Toast.makeText(this, "כתובת האימייל לא תקינה", Toast.LENGTH_LONG).show();
            return;
        } if (!InputValidator.isPasswordValid(password)) {
            editTextPassword.requestFocus();
            Toast.makeText(this, "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            return;
        }

        String uid = databaseService.generateUserId();
        User newUser = new User(uid, firstName, lastName, email, password
                , false
                , null
                , new HashMap<>()
                , 0);

        databaseService.checkIfEmailExists(email, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    Toast.makeText(RegisterActivity.this, "אימייל זה תפוס", Toast.LENGTH_SHORT).show();
                } else {
                    databaseService.createNewUser(newUser, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            SharedPreferencesUtil.saveUser(RegisterActivity.this, newUser);
                            Toast.makeText(RegisterActivity.this, "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(RegisterActivity.this, "שגיאה בשמירת הנתונים", Toast.LENGTH_LONG).show();
                            SharedPreferencesUtil.signOutUser(RegisterActivity.this);
                        }
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                SharedPreferencesUtil.signOutUser(RegisterActivity.this);
                Toast.makeText(RegisterActivity.this, "שגיאה בהרשמת המשתמש", Toast.LENGTH_LONG).show();
            }
        });
    }
}