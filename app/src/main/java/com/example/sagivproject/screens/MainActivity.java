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
 * The main dashboard activity for authenticated regular users.
 * <p>
 * This activity serves as the primary navigation hub, providing quick access to
 * medication management, forums, AI assistance, cognitive games, and statistics.
 * It also displays a personalized greeting to the logged-in user.
 * </p>
 */
@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        User user = sharedPreferencesUtil.getUser();

        Button btnToMedicationList = findViewById(R.id.btn_home_to_MedicationList);
        Button btnToForum = findViewById(R.id.btn_home_to_forum_categories);
        Button btnToAi = findViewById(R.id.btn_home_to_Ai);
        Button btnToGameHomeScreen = findViewById(R.id.btn_home_to_GameHomeScreen);
        Button btnToMathProblems = findViewById(R.id.btn_home_to_MathProblems);
        Button btnToTipOfTheDay = findViewById(R.id.btn_home_to_TipOfTheDay);
        Button btnToStats = findViewById(R.id.btn_home_to_Stats);
        Button btnToEmergency = findViewById(R.id.btn_home_to_emergency);
        TextView txtHomePageTitle = findViewById(R.id.txt_home_Title);

        btnToMedicationList.setOnClickListener(v -> startActivity(new Intent(this, MedicationListActivity.class)));
        btnToForum.setOnClickListener(v -> startActivity(new Intent(this, ForumCategoriesActivity.class)));
        btnToAi.setOnClickListener(v -> startActivity(new Intent(this, AiActivity.class)));
        btnToGameHomeScreen.setOnClickListener(v -> startActivity(new Intent(this, GameHomeScreenActivity.class)));
        btnToMathProblems.setOnClickListener(v -> startActivity(new Intent(this, MathProblemsActivity.class)));
        btnToTipOfTheDay.setOnClickListener(v -> startActivity(new Intent(this, TipOfTheDayActivity.class)));
        btnToStats.setOnClickListener(v -> startActivity(new Intent(this, UserStatsActivity.class)));
        btnToEmergency.setOnClickListener(v -> startActivity(new Intent(this, EmergencyContactsActivity.class)));

        if (user != null) {
            txtHomePageTitle.setText(String.format("שלום %s", user.getFullName()));
        }
    }
}