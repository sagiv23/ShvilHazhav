package com.example.sagivproject.screens;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment for managing application settings.
 */
@AndroidEntryPoint
public class SettingsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogout = view.findViewById(R.id.btn_logout);
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        boolean isFromLoggedIn = false;
        if (getArguments() != null) {
            isFromLoggedIn = getArguments().getBoolean("isFromLoggedIn", false);
        }

        if (!isFromLoggedIn) {
            btnLogout.setVisibility(View.GONE);
        }

        btnLogout.setOnClickListener(v -> logout());

        if (getContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
            switchDarkMode.setChecked(isDarkMode);
            updateDarkModeText(switchDarkMode, isDarkMode);

            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                updateDarkModeText(switchDarkMode, isChecked);
            });
        }
    }

    private void updateDarkModeText(SwitchMaterial switchDarkMode, boolean isDarkMode) {
        switchDarkMode.setText(isDarkMode ? R.string.bright_mode : R.string.dark_mode);
    }

    private void logout() {
        if (getActivity() == null) return;
        Runnable onConfirm = () -> {
            String email = databaseService.getAuthService().logout();
            sharedPreferencesUtil.signOutUser();
            if (getContext() != null)
                Toast.makeText(getContext(), "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

            Bundle args = new Bundle();
            args.putString("userEmail", email);
            navigateTo(R.id.loginFragment, args);
        };

        dialogService.showConfirmDialog(getParentFragmentManager(), "התנתקות", "האם ברצונך להתנתק?", "התנתק", "בטל", onConfirm);
    }
}
