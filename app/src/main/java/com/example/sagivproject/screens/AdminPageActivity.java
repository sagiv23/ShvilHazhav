package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        User user = sharedPreferencesUtil.getUser();

        Button btnToUserTable = findViewById(R.id.btn_admin_to_UsersTablePage);
        Button btnToUserStats = findViewById(R.id.btn_admin_to_UserStats);
        Button btnToMedicationsTable = findViewById(R.id.btn_admin_to_MedicineImagesTablePage);
        Button btnToMemoryGameLogsTable = findViewById(R.id.btn_admin_to_MemoryGameLogsTablePage);
        Button btnToForumCategories = findViewById(R.id.btn_admin_to_ForumCategories);
        Button btnToDetailsAboutUser = findViewById(R.id.btn_admin_to_DetailsAboutUser);
        Button btnToSettings = findViewById(R.id.btn_admin_to_Settings);
        TextView txtAdminTitle = findViewById(R.id.txt_admin_title);

        btnToUserTable.setOnClickListener(v -> startActivity(new Intent(this, UsersTableActivity.class)));
        btnToUserStats.setOnClickListener(v -> startActivity(new Intent(this, UserStatsActivity.class)));
        btnToMedicationsTable.setOnClickListener(v -> startActivity(new Intent(this, MedicationImagesTableActivity.class)));
        btnToMemoryGameLogsTable.setOnClickListener(v -> startActivity(new Intent(this, MemoryGameLogsTableActivity.class)));
        btnToForumCategories.setOnClickListener(v -> startActivity(new Intent(this, ForumCategoriesActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(v -> startActivity(new Intent(this, DetailsAboutUserActivity.class)));
        btnToSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("isFromLoggedIn", true);
            startActivity(intent);
        });

        if (user != null) {
            txtAdminTitle.setText(String.format("שלום %s", user.getFullName()));
        }
    }
}