package com.example.sagivproject.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.models.AuthHelper;
import com.example.sagivproject.models.LogoutHelper;
import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsAboutUserActivity extends AppCompatActivity {
    private Button btnToContact, btnToHomePage, btnToExit, btnEditUser;
    private TextView txtTitle, txtFirstName, txtLastName, txtEmail, txtPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_about_user);

        mAuth = FirebaseAuth.getInstance();

        AuthHelper.checkUserLoggedIn(this);
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser != null && currentUser.getIsAdmin()) {
            startActivity(new Intent(this, AdminPageActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsAboutUserPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToContact = findViewById(R.id.btnDetailsAboutUserPageToContactPage);
        btnToHomePage = findViewById(R.id.btnDetailsAboutUserPageToHomePage);
        btnToExit = findViewById(R.id.btnDetailsAboutUserPageToExit);
        btnEditUser = findViewById(R.id.btnDetailsAboutUserEditUser);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(DetailsAboutUserActivity.this, ContactActivity.class)));
        btnToHomePage.setOnClickListener(view -> startActivity(new Intent(DetailsAboutUserActivity.this, HomePageActivity.class)));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(DetailsAboutUserActivity.this));
        btnEditUser.setOnClickListener(view -> openEditDialog());

        txtTitle = findViewById(R.id.txtDetailsAboutUserTitle);
        txtFirstName = findViewById(R.id.txtDetailsAboutUserFirstName);
        txtLastName = findViewById(R.id.txtDetailsAboutUserLastName);
        txtEmail = findViewById(R.id.txtDetailsAboutUserEmail);
        txtPassword = findViewById(R.id.txtDetailsAboutUserPassword);

        // אתחול DatabaseReference לקריאת נתוני המשתמשים
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserDetails();
    }

    private void loadUserDetails() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "לא נמצא משתמש מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class);
                    if (currentUser != null) {
                        txtTitle.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                        txtFirstName.setText(currentUser.getFirstName());
                        txtLastName.setText(currentUser.getLastName());
                        txtEmail.setText(currentUser.getEmail());
                        txtPassword.setText(currentUser.getPassword());
                    }
                } else {
                    Toast.makeText(DetailsAboutUserActivity.this, "לא נמצאו נתונים למשתמש", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openEditDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        builder.setView(dialogView);

        EditText inputFirstName = dialogView.findViewById(R.id.inputEditUserFirstName);
        EditText inputLastName = dialogView.findViewById(R.id.inputEditUserLastName);
        EditText inputPassword = dialogView.findViewById(R.id.inputEditUserPassword);
        Button btnSave = dialogView.findViewById(R.id.btnEditUserSave);
        Button btnCancel = dialogView.findViewById(R.id.btnEditUserCancel);

        inputFirstName.setText(currentUser.getFirstName());
        inputLastName.setText(currentUser.getLastName());
        inputPassword.setText(currentUser.getPassword());

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newFirst = inputFirstName.getText().toString().trim();
            String newLast = inputLastName.getText().toString().trim();
            String newPass = inputPassword.getText().toString().trim();

            if (!newFirst.isEmpty() && !newLast.isEmpty() && !newPass.isEmpty()) {
                currentUser.setFirstName(newFirst);
                currentUser.setLastName(newLast);
                currentUser.setPassword(newPass);
                updateUserInDatabase();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUserInDatabase() {
        usersRef.child(currentUser.getId()).setValue(currentUser)
                .addOnSuccessListener(aVoid -> {
                    txtFirstName.setText(currentUser.getFirstName());
                    txtLastName.setText(currentUser.getLastName());
                    txtPassword.setText(currentUser.getPassword());
                    txtTitle.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                    Toast.makeText(this, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בעדכון הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}