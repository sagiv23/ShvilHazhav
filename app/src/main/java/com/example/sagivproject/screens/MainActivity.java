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

public class MainActivity extends AppCompatActivity {
    private Button btnToContact, btnToDetailsAboutUser, btnToMedicationList, btnToForum, btnToAi, btnToGameHomeScreen, btnToExit;
    private TextView txtHomePageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
                SharedPreferencesUtil.saveUser(MainActivity.this, updatedUser);
            }

            @Override
            public void onFailed(Exception e) {
                failedToGetUser();
            }

            private void failedToGetUser() {
                SharedPreferencesUtil.signOutUser(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        PagePermissions.checkUserPage(this);

        btnToContact = findViewById(R.id.btn_main_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_main_to_DetailsAboutUser);
        btnToMedicationList = findViewById(R.id.btn_main_to_MedicationList);
        btnToForum = findViewById(R.id.btn_main_to_forum);
        btnToAi = findViewById(R.id.btn_main_to_Ai);
        btnToGameHomeScreen = findViewById(R.id.btn_main_to_GameHomeScreen);
        btnToExit = findViewById(R.id.btn_main_to_exit);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DetailsAboutUserActivity.class)));
        btnToMedicationList.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MedicationListActivity.class)));
        btnToForum.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ForumActivity.class)));
        btnToAi.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AiActivity.class)));
        btnToGameHomeScreen.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, GameHomeScreenActivity.class)));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(MainActivity.this));

        txtHomePageTitle = findViewById(R.id.txt_main_Title);

        User user = SharedPreferencesUtil.getUser(this);
        if (user != null) {
            showUserName(user);
        }
    }

    private void showUserName(User user) {
        String fullName = user.getFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            txtHomePageTitle.setText("שלום מטופל יקר");
        } else {
            txtHomePageTitle.setText("שלום " + fullName);
        }
    }
}