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

import com.example.sagivproject.R;
import com.example.sagivproject.models.LogoutHelper;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

public class DetailsAboutUserActivity extends AppCompatActivity {
    private Button btnToContact, btnToHomePage, btnToExit, btnEditUser;
    private TextView txtTitle, txtFirstName, txtLastName, txtEmail, txtPassword;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_about_user);

        currentUser = SharedPreferencesUtil.getUser(this);

        if (currentUser == null) {
            Toast.makeText(this, "אין לך גישה לדף זה - אתה לא מחובר!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        if (currentUser.getIsAdmin()) {
            Toast.makeText(this, "ניסיון יפה, מנהל! אבל הדף הזה מיועד רק למשתמשים רגילים.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AdminPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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

        txtTitle = findViewById(R.id.txtDetailsAboutUserTitle);
        txtFirstName = findViewById(R.id.txtDetailsAboutUserFirstName);
        txtLastName = findViewById(R.id.txtDetailsAboutUserLastName);
        txtEmail = findViewById(R.id.txtDetailsAboutUserEmail);
        txtPassword = findViewById(R.id.txtDetailsAboutUserPassword);

        btnToContact.setOnClickListener(v -> startActivity(new Intent(this, ContactActivity.class)));
        btnToHomePage.setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        btnToExit.setOnClickListener(v -> LogoutHelper.logout(this));
        btnEditUser.setOnClickListener(v -> openEditDialog());

        loadUserDetailsFromSharedPref();
    }

    private void loadUserDetailsFromSharedPref() {
        txtTitle.setText(currentUser.getFullName());
        txtFirstName.setText(currentUser.getFirstName());
        txtLastName.setText(currentUser.getLastName());
        txtEmail.setText(currentUser.getEmail());
        txtPassword.setText(currentUser.getPassword());
    }

    private void openEditDialog() {
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

            if (newFirst.isEmpty() || newLast.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.setFirstName(newFirst);
            currentUser.setLastName(newLast);
            currentUser.setPassword(newPass);

            updateUserInDatabaseAndLocal();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUserInDatabaseAndLocal() {
        DatabaseService.getInstance().updateUser(currentUser, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {

                txtFirstName.setText(currentUser.getFirstName());
                txtLastName.setText(currentUser.getLastName());
                txtPassword.setText(currentUser.getPassword());
                txtTitle.setText(currentUser.getFullName());

                SharedPreferencesUtil.saveUser(DetailsAboutUserActivity.this, currentUser);

                Toast.makeText(DetailsAboutUserActivity.this, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בעדכון הנתונים: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}