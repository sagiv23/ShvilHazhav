package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;

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
        setContent(R.layout.activity_main, R.id.homePage);
        setupMenu();

        findViewById(R.id.btn_home_to_MedicationList).setOnClickListener(v -> startActivity(new Intent(this, MedicationListActivity.class)));
        findViewById(R.id.btn_home_to_forum_categories).setOnClickListener(v -> startActivity(new Intent(this, ForumCategoriesActivity.class)));
        findViewById(R.id.btn_home_to_Ai).setOnClickListener(v -> startActivity(new Intent(this, AiActivity.class)));
        findViewById(R.id.btn_home_to_GameHomeScreen).setOnClickListener(v -> startActivity(new Intent(this, GameHomeScreenActivity.class)));
        findViewById(R.id.btn_home_to_MathProblems).setOnClickListener(v -> startActivity(new Intent(this, MathProblemsActivity.class)));
        findViewById(R.id.btn_home_to_TipOfTheDay).setOnClickListener(v -> startActivity(new Intent(this, TipOfTheDayActivity.class)));
        findViewById(R.id.btn_home_to_Stats).setOnClickListener(v -> startActivity(new Intent(this, UserStatsActivity.class)));
        findViewById(R.id.btn_home_to_emergency).setOnClickListener(v -> startActivity(new Intent(this, EmergencyContactsActivity.class)));

        setGreeting(R.id.txt_home_Title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setGreeting(R.id.txt_home_Title);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setGreeting(R.id.txt_home_Title);
    }
}