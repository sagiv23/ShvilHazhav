package com.example.sagivproject.screens;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.User;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The main fragment for logged-in users.
 * <p>
 * This fragment serves as the primary navigation hub, providing buttons to access
 * medication management, forums, AI assistant, games, statistics, and emergency contacts.
 * It also greets the user by name and requests notification permissions.
 * </p>
 */
@AndroidEntryPoint
public class HomeFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = sharedPreferencesUtil.getUser();

        Button btnToMedicationList = view.findViewById(R.id.btn_home_to_MedicationList);
        Button btnToForum = view.findViewById(R.id.btn_home_to_forum_categories);
        Button btnToAi = view.findViewById(R.id.btn_home_to_Ai);
        Button btnToGameHomeScreen = view.findViewById(R.id.btn_home_to_GameHomeScreen);
        Button btnToMathProblems = view.findViewById(R.id.btn_home_to_MathProblems);
        Button btnToTipOfTheDay = view.findViewById(R.id.btn_home_to_TipOfTheDay);
        Button btnToStats = view.findViewById(R.id.btn_home_to_Stats);
        Button btnToEmergency = view.findViewById(R.id.btn_home_to_emergency);
        TextView txtHomePageTitle = view.findViewById(R.id.txt_home_Title);

        btnToMedicationList.setOnClickListener(v -> navigateTo(R.id.action_homeFragment_to_medicationListFragment));
        btnToForum.setOnClickListener(v -> navigateTo(R.id.action_homeFragment_to_forumCategoriesFragment));
        btnToAi.setOnClickListener(v -> navigateTo(R.id.action_homeFragment_to_aiFragment));
        btnToGameHomeScreen.setOnClickListener(v -> navigateTo(R.id.action_homeFragment_to_gameHomeScreenFragment));
        btnToMathProblems.setOnClickListener(v -> navigateTo(R.id.action_homeFragment_to_mathProblemsFragment));
        btnToTipOfTheDay.setOnClickListener(v -> navigateTo(R.id.action_homeFragment_to_tipOfTheDayFragment));
        btnToStats.setOnClickListener(v -> navigateTo(R.id.action_homeFragment_to_userStatsFragment));
        btnToEmergency.setOnClickListener(v -> navigateTo(R.id.action_mainFragment_to_emergencyContactsFragment));

        if (user != null) {
            txtHomePageTitle.setText(String.format("שלום %s", user.getFullName()));
        }

        requestNotificationPermission();
    }

    /**
     * Requests the POST_NOTIFICATIONS permission required for Android 13+.
     */
    private void requestNotificationPermission() {
        requestPermissions(Manifest.permission.POST_NOTIFICATIONS);
    }
}
