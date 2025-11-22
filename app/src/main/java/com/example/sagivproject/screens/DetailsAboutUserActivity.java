package com.example.sagivproject.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;

public class DetailsAboutUserActivity extends AppCompatActivity {
    private Button btnToMain, btnToContact, btnToExit, btnEditUser;
    private TextView txtTitle, txtFirstName, txtLastName, txtEmail, txtPassword;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_about_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsAboutUserPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkUserPage(this);

        user = SharedPreferencesUtil.getUser(this);

        btnToMain = findViewById(R.id.btn_DetailsAboutUser_to_main);
        btnToContact = findViewById(R.id.btn_DetailsAboutUser_to_contact);
        btnToExit = findViewById(R.id.btn_DetailsAboutUser_to_exit);
        btnEditUser = findViewById(R.id.btn_DetailsAboutUser_edit_user);

        txtTitle = findViewById(R.id.txt_DetailsAboutUser_title);
        txtFirstName = findViewById(R.id.txt_DetailsAboutUser_first_name);
        txtLastName = findViewById(R.id.txt_DetailsAboutUser_last_name);
        txtEmail = findViewById(R.id.txt_DetailsAboutUser_email);
        txtPassword = findViewById(R.id.txt_DetailsAboutUser_password);

        btnToMain.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        btnToContact.setOnClickListener(v -> startActivity(new Intent(this, ContactActivity.class)));
        btnToExit.setOnClickListener(v -> LogoutHelper.logout(this));
        btnEditUser.setOnClickListener(v -> openEditDialog());

        loadUserDetailsFromSharedPref();
    }

    private void loadUserDetailsFromSharedPref() {
        txtTitle.setText(user.getFullName());
        txtFirstName.setText(user.getFirstName());
        txtLastName.setText(user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(user.getPassword());
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

        inputFirstName.setText(user.getFirstName());
        inputLastName.setText(user.getLastName());
        inputPassword.setText(user.getPassword());

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newFirst = inputFirstName.getText().toString().trim();
            String newLast = inputLastName.getText().toString().trim();
            String newPass = inputPassword.getText().toString().trim();

            if (newFirst.isEmpty() || newLast.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setFirstName(newFirst);
            user.setLastName(newLast);
            user.setPassword(newPass);

            updateUserInDatabaseAndLocal();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateUserInDatabaseAndLocal() {
        DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {

                txtFirstName.setText(user.getFirstName());
                txtLastName.setText(user.getLastName());
                txtPassword.setText(user.getPassword());
                txtTitle.setText(user.getFullName());

                SharedPreferencesUtil.saveUser(DetailsAboutUserActivity.this, user);

                Toast.makeText(DetailsAboutUserActivity.this, "הפרטים עודכנו בהצלחה!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(DetailsAboutUserActivity.this, "שגיאה בעדכון הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}