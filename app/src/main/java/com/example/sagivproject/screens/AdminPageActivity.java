package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Objects;

public class AdminPageActivity extends AppCompatActivity {
    private Button btnToUserTable, btnLogout;
    private TextView txtAdminTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DatabaseService.getInstance().getUser(Objects.requireNonNull(SharedPreferencesUtil.getUserId(this)), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (updatedUser == null) {
                    failedToGetUser();
                    return;
                }
                SharedPreferencesUtil.saveUser(AdminPageActivity.this, updatedUser);
            }

            @Override
            public void onFailed(Exception e) {
                failedToGetUser();
            }

            private void failedToGetUser() {
                SharedPreferencesUtil.signOutUser(AdminPageActivity.this);
                Intent intent = new Intent(AdminPageActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        PagePermissions.checkAdminPage(this);

        btnToUserTable = findViewById(R.id.btn_admin_to_UsersTablePage);
        btnLogout = findViewById(R.id.btn_admin_to_exit);
        txtAdminTitle = findViewById(R.id.txt_admin_title);
        btnToUserTable.setOnClickListener(view -> startActivity(new Intent(AdminPageActivity.this, UsersTableActivity.class)));
        btnLogout.setOnClickListener(view -> LogoutHelper.logout(this));

        User localUser = SharedPreferencesUtil.getUser(this);
        if (localUser != null) {
            showUserName(localUser);
        }
    }

    private void showUserName(User user) {
        String fullName = user.getFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            txtAdminTitle.setText("שלום מנהל יקר");
        } else {
            txtAdminTitle.setText("שלום " + fullName);
        }
    }
}