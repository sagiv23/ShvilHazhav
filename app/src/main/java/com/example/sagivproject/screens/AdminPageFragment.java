package com.example.sagivproject.screens;

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
 * A fragment for the administrator's main page.
 */
@AndroidEntryPoint
public class AdminPageFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = sharedPreferencesUtil.getUser();

        Button btnToUserTable = view.findViewById(R.id.btn_admin_to_UsersTablePage);
        Button btnToUserStats = view.findViewById(R.id.btn_admin_to_UserStats);
        Button btnToMedicationsTable = view.findViewById(R.id.btn_admin_to_MedicineImagesTablePage);
        Button btnToMemoryGameLogsTable = view.findViewById(R.id.btn_admin_to_MemoryGameLogsTablePage);
        Button btnToForumCategories = view.findViewById(R.id.btn_admin_to_ForumCategories);
        Button btnToDetailsAboutUser = view.findViewById(R.id.btn_admin_to_DetailsAboutUser);
        Button btnToSettings = view.findViewById(R.id.btn_admin_to_Settings);
        TextView txtAdminTitle = view.findViewById(R.id.txt_admin_title);

        btnToUserTable.setOnClickListener(v -> navigateTo(R.id.action_adminPageFragment_to_usersTableFragment));
        btnToUserStats.setOnClickListener(v -> navigateTo(R.id.action_adminPageFragment_to_userStatsFragment));
        btnToMedicationsTable.setOnClickListener(v -> navigateTo(R.id.action_adminPageFragment_to_medicationImagesTableFragment));
        btnToMemoryGameLogsTable.setOnClickListener(v -> navigateTo(R.id.action_adminPageFragment_to_memoryGameLogsTableFragment));
        btnToForumCategories.setOnClickListener(v -> navigateTo(R.id.action_adminPageFragment_to_adminForumCategoriesFragment));
        btnToDetailsAboutUser.setOnClickListener(v -> navigateTo(R.id.action_adminPageFragment_to_detailsAboutUserFragment));
        btnToSettings.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("isFromLoggedIn", true);
            navigateTo(R.id.action_adminPageFragment_to_settingsFragment, args);
        });

        if (user != null) {
            txtAdminTitle.setText(String.format("שלום %s", user.getFullName()));
        }
    }
}
