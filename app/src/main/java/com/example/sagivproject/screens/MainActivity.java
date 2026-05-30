package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

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
        setGreeting(R.id.txt_home_Title);

        setupClickListeners();

        View fab = findViewById(R.id.fab_emergency);
        fab.setOnClickListener(v -> onNavigate(new Intent(this, EmergencyContactsActivity.class)));
        fab.setAlpha(0f);
        fab.setTranslationY(50f);
        fab.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(500).start();
    }

    private void setupClickListeners() {
        int[] cardIds = {
                R.id.btn_home_to_MedicationList,
                R.id.btn_home_to_forum_categories,
                R.id.btn_home_to_Ai,
                R.id.btn_home_to_GameHomeScreen,
                R.id.btn_home_to_MathProblems,
                R.id.btn_home_to_TipOfTheDay,
                R.id.btn_home_to_Stats,
                R.id.btn_home_to_emergency
        };

        for (int id : cardIds) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.animate().translationZ(12f).setDuration(150).start();
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        v.animate().translationZ(0f).setDuration(150).start();
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.performClick();
                    }
                    return true;
                });
            }
        }

        findViewById(R.id.btn_home_to_MedicationList).setOnClickListener(v -> onNavigate(new Intent(this, MedicationListActivity.class)));
        findViewById(R.id.btn_home_to_forum_categories).setOnClickListener(v -> onNavigate(new Intent(this, ForumCategoriesActivity.class)));
        findViewById(R.id.btn_home_to_Ai).setOnClickListener(v -> onNavigate(new Intent(this, AiActivity.class)));
        findViewById(R.id.btn_home_to_GameHomeScreen).setOnClickListener(v -> onNavigate(new Intent(this, GameHomeScreenActivity.class)));
        findViewById(R.id.btn_home_to_MathProblems).setOnClickListener(v -> onNavigate(new Intent(this, MathProblemsActivity.class)));
        findViewById(R.id.btn_home_to_TipOfTheDay).setOnClickListener(v -> onNavigate(new Intent(this, TipOfTheDayActivity.class)));
        findViewById(R.id.btn_home_to_Stats).setOnClickListener(v -> onNavigate(new Intent(this, UserStatsActivity.class)));
        findViewById(R.id.btn_home_to_emergency).setOnClickListener(v -> onNavigate(new Intent(this, EmergencyContactsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setGreeting(R.id.txt_home_Title);
    }
}