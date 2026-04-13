package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The main dashboard activity for administrators.
 * <p>
 * This activity provides a central hub for administrative tools, including:
 * <ul>
 * <li>User account management and role assignment.</li>
 * <li>Forum moderation and category management.</li>
 * <li>Monitoring real-time memory game logs.</li>
 * <li>Managing medication card image assets.</li>
 * <li>Accessing detailed user profile information and statistics.</li>
 * </ul>
 * It also displays a personalized greeting to the administrator.
 * </p>
 */
@AndroidEntryPoint
public class AdminPageActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_admin_page, R.id.adminPage);

        User user = sharedPreferencesUtil.getUser();

        findViewById(R.id.btn_admin_to_UsersTablePage).setOnClickListener(v -> startActivity(new Intent(this, UsersTableActivity.class)));
        findViewById(R.id.btn_admin_to_UserStats).setOnClickListener(v -> startActivity(new Intent(this, UserStatsActivity.class)));
        findViewById(R.id.btn_admin_to_MedicineImagesTablePage).setOnClickListener(v -> startActivity(new Intent(this, MedicationImagesTableActivity.class)));
        findViewById(R.id.btn_admin_to_MemoryGameLogsTablePage).setOnClickListener(v -> startActivity(new Intent(this, MemoryGameLogsTableActivity.class)));
        findViewById(R.id.btn_admin_to_ForumCategories).setOnClickListener(v -> startActivity(new Intent(this, ForumCategoriesActivity.class)));
        findViewById(R.id.btn_admin_to_DetailsAboutUser).setOnClickListener(v -> startActivity(new Intent(this, DetailsAboutUserActivity.class)));
        findViewById(R.id.btn_admin_to_Settings).setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("isFromLoggedIn", true);
            startActivity(intent);
        });

        setGreeting(R.id.txt_admin_title, user);
    }
}