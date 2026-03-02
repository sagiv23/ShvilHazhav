package com.example.sagivproject.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity for managing application settings.
 * <p>
 * This screen provides options to toggle dark mode and, if the user is logged in,
 * to log out of their account.
 * </p>
 */
@AndroidEntryPoint
public class SettingsActivity extends BaseActivity {

    /**
     * Initializes the activity, sets up the UI, and configures the dark mode switch and logout button.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btn_settings_back).setOnClickListener(v -> finish());
        Button btnLogout = findViewById(R.id.btn_logout);

        boolean isFromLoggedIn = getIntent().getBooleanExtra("isFromLoggedIn", false);
        if (!isFromLoggedIn) {
            btnLogout.setVisibility(View.GONE);
        }

        btnLogout.setOnClickListener(v -> logout());

        SwitchMaterial switchDarkMode = findViewById(R.id.switch_dark_mode);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);
        updateDarkModeText(switchDarkMode, isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            updateDarkModeText(switchDarkMode, isChecked);
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    /**
     * Updates the text of the dark mode switch based on its current state.
     *
     * @param switchDarkMode The dark mode switch.
     * @param isDarkMode     The current dark mode state.
     */
    private void updateDarkModeText(SwitchMaterial switchDarkMode, boolean isDarkMode) {
        if (isDarkMode) {
            switchDarkMode.setText(R.string.bright_mode);
        } else {
            switchDarkMode.setText(R.string.dark_mode);
        }
    }

    /**
     * Logs out the current user, clears their session data, and navigates to the Login screen.
     */
    private void logout() {
        Runnable onConfirm = () -> {
            String email = databaseService.getAuthService().logout();
            Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("userEmail", email);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        };

        // Use injected dialog from DialogService
        dialogService.showConfirmDialog("התנתקות", "האם ברצונך להתנתק?", "התנתק", "בטל", onConfirm);
    }
}
