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
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private Button btnToContact, btnToMain, btnToLogin, btnRegister;
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            Toast.makeText(this, "שגיאה: המשתמש לא אותר", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String userId = firebaseUser.getUid();
                        User newUser = new User(userId, firstName, lastName, email, password, false);

                        usersRef.child(userId).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    SharedPreferencesUtil.saveUser(this, newUser);

                                    Toast.makeText(this, "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, HomePageActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "שגיאה בשמירת הנתונים: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show());
                    } else {
                        String errorMessage = FirebaseErrorsHelper.getFriendlyFirebaseAuthError(task.getException());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}