package com.example.sagivproject.screens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IFallDetectionService;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that allows users to manage application preferences and account status.
 * <p>
 * This screen provides options for toggling Dark Mode, enabling or disabling the
 * background Fall Detection service (including necessary permission handling),
 * and logging out of the application.
 * </p>
 */
@AndroidEntryPoint
public class SettingsActivity extends BaseActivity {
    /** Service for starting and stopping background movement monitoring. */
    @Inject
    protected IFallDetectionService fallDetectionService;

    private SwitchMaterial switchFallDetection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        Button btnLogout = findViewById(R.id.btn_logout);
        SwitchMaterial switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchFallDetection = findViewById(R.id.switch_fall_detection);
        View separatorLogout = findViewById(R.id.separator_logout);
        View separatorFallDetection = findViewById(R.id.separator_fall_detection);

        if (sharedPreferencesUtil.isUserNotLoggedIn()) {
            btnLogout.setVisibility(View.GONE);
            switchFallDetection.setVisibility(View.GONE);
            if (separatorLogout != null) separatorLogout.setVisibility(View.GONE);
            if (separatorFallDetection != null) separatorFallDetection.setVisibility(View.GONE);
        } else {
            User currentUser = sharedPreferencesUtil.getUser();
            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setOnClickListener(v -> logout());

            if (currentUser != null && currentUser.isAdmin()) {
                switchFallDetection.setVisibility(View.GONE);
                if (separatorFallDetection != null) separatorFallDetection.setVisibility(View.GONE);
            }
        }

        boolean isDarkMode = sharedPreferencesUtil.isDarkMode();
        switchDarkMode.setChecked(isDarkMode);
        updateDarkModeText(switchDarkMode, isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferencesUtil.setDarkMode(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            updateDarkModeText(switchDarkMode, isChecked);
        });

        boolean isFallDetectionEnabled = sharedPreferencesUtil.isFallDetectionEnabled();
        switchFallDetection.setChecked(isFallDetectionEnabled);

        switchFallDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handleFallDetectionEnable();
            } else {
                disableFallDetection();
            }
        });
    }

    /**
     * Orchestrates the complex flow of enabling fall detection.
     * Checks and requests multiple required permissions: Activity Recognition,
     * Fine Location, SMS, and Notifications.
     */
    private void handleFallDetectionEnable() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissions(permissionsToRequest.toArray(new String[0]));
        } else {
            checkBackgroundLocationAndEnable();
        }
    }

    /**
     * Checks if background location (Allow all the time) is granted.
     * Displays a mandatory confirmation dialog if permission is missing.
     */
    private void checkBackgroundLocationAndEnable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            dialogService.showConfirmDialog(getSupportFragmentManager(),
                    "הרשאת מיקום ברקע",
                    "כדי שנוכל לשלוח את המיקום שלך לאנשי הקשר בזמן נפילה, עליך לאשר הרשאת 'אפשר תמיד' (Allow all the time) בהגדרות המיקום של האפליקציה.",
                    "להגדרות", "בטל",
                    () -> requestPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION));
            switchFallDetection.setChecked(false);
        } else {
            checkContactsAndEnableFallDetection();
        }
    }

    /**
     * Processes the results of the permission request specifically for fall detection.
     * @param isGranted Map of permissions and their grant status.
     */
    @Override
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
        boolean activityOk = Boolean.TRUE.equals(isGranted.getOrDefault(Manifest.permission.ACTIVITY_RECOGNITION,
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED));

        boolean locationOk = Boolean.TRUE.equals(isGranted.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));

        boolean smsOk = Boolean.TRUE.equals(isGranted.getOrDefault(Manifest.permission.SEND_SMS,
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED));

        if (activityOk && locationOk && smsOk) {
            checkBackgroundLocationAndEnable();
        } else {
            switchFallDetection.setChecked(false);
            Toast.makeText(this, "נדרשות הרשאות חיישנים, מיקום ו-SMS כדי להפעיל זיהוי נפילות", Toast.LENGTH_LONG).show();
        }
    }

    /** Disables the fall detection background service and updates preferences. */
    private void disableFallDetection() {
        sharedPreferencesUtil.setFallDetectionEnabled(false);
        fallDetectionService.stopMonitoring();
        Toast.makeText(this, "זיהוי נפילות הופסק", Toast.LENGTH_SHORT).show();
    }

    /** Final validation step before enabling fall detection: ensures the user has at least one emergency contact. */
    private void checkContactsAndEnableFallDetection() {
        User user = sharedPreferencesUtil.getUser();
        if (user == null) return;

        databaseService.getEmergencyService().getContacts(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<EmergencyContact> contacts) {
                if (contacts != null && !contacts.isEmpty()) {
                    sharedPreferencesUtil.setFallDetectionEnabled(true);
                    fallDetectionService.startMonitoring();
                    Toast.makeText(SettingsActivity.this, "זיהוי נפילות הופעל ברקע", Toast.LENGTH_SHORT).show();
                } else {
                    switchFallDetection.setChecked(false);
                    sharedPreferencesUtil.setFallDetectionEnabled(false);
                    Toast.makeText(SettingsActivity.this, "נא להוסיף אנשי קשר לחירום לפני הפעלת הזיהוי", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailed(Exception e) {
                switchFallDetection.setChecked(false);
                Toast.makeText(SettingsActivity.this, "שגיאה בבדיקת אנשי קשר", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the text label of the Dark Mode switch.
     * @param switchDarkMode The switch component.
     * @param isDarkMode Current dark mode state.
     */
    private void updateDarkModeText(SwitchMaterial switchDarkMode, boolean isDarkMode) { switchDarkMode.setText(isDarkMode ? R.string.bright_mode : R.string.dark_mode); }

    /** Logs the current user out after a confirmation dialog. */
    private void logout() {
        Runnable onConfirm = () -> {
            fallDetectionService.stopMonitoring();
            String email = databaseService.getAuthService().logout();
            sharedPreferencesUtil.signOutUser();
            Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.putExtra("userEmail", email);
            startActivity(intent);
        };

        dialogService.showConfirmDialog(getSupportFragmentManager(), "התנתקות", "האם ברצונך להתנתק?", "התנתק", "בטל", onConfirm);
    }
}