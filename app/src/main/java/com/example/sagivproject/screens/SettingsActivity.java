package com.example.sagivproject.screens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.User;
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
 * This screen provides options for toggling Dark Mode, controlling vibration and notification
 * preferences, enabling or disabling the background Fall Detection service
 * (including necessary permission handling), and logging out of the application.
 * </p>
 */
@AndroidEntryPoint
public class SettingsActivity extends BaseActivity {
    /**
     * Service for starting and stopping background movement monitoring.
     */
    @Inject
    protected IFallDetectionService fallDetectionService;

    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchVibration;
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchCamera;
    private SwitchMaterial switchGallery;
    private SwitchMaterial switchLocation;
    private SwitchMaterial switchSms;
    private SwitchMaterial switchContacts;
    private SwitchMaterial switchFallDetection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_settings, R.id.settingsPage);
        setupMenu();

        Button btnLogout = findViewById(R.id.btn_logout);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchVibration = findViewById(R.id.switch_vibration);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchCamera = findViewById(R.id.switch_camera);
        switchGallery = findViewById(R.id.switch_gallery);
        switchLocation = findViewById(R.id.switch_location);
        switchSms = findViewById(R.id.switch_sms);
        switchContacts = findViewById(R.id.switch_contacts);
        switchFallDetection = findViewById(R.id.switch_fall_detection);

        View separatorLogout = findViewById(R.id.separator_logout);
        View separatorFallDetection = findViewById(R.id.separator_fall_detection);
        View separatorVibration = findViewById(R.id.separator_vibration);
        View separatorNotifications = findViewById(R.id.separator_notifications);
        View separatorCamera = findViewById(R.id.separator_camera);
        View separatorGallery = findViewById(R.id.separator_gallery);
        View separatorLocation = findViewById(R.id.separator_location);
        View separatorSms = findViewById(R.id.separator_sms);

        if (sharedPreferencesUtil.isUserNotLoggedIn()) {
            // Guest User: Hide everything except Dark Mode
            switchVibration.setVisibility(View.GONE);
            switchNotifications.setVisibility(View.GONE);
            switchCamera.setVisibility(View.GONE);
            switchGallery.setVisibility(View.GONE);
            switchLocation.setVisibility(View.GONE);
            switchSms.setVisibility(View.GONE);
            switchContacts.setVisibility(View.GONE);
            switchFallDetection.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);

            if (separatorVibration != null) separatorVibration.setVisibility(View.GONE);
            if (separatorNotifications != null) separatorNotifications.setVisibility(View.GONE);
            if (separatorCamera != null) separatorCamera.setVisibility(View.GONE);
            if (separatorGallery != null) separatorGallery.setVisibility(View.GONE);
            if (separatorLocation != null) separatorLocation.setVisibility(View.GONE);
            if (separatorSms != null) separatorSms.setVisibility(View.GONE);
            if (separatorFallDetection != null) separatorFallDetection.setVisibility(View.GONE);
            if (separatorLogout != null) separatorLogout.setVisibility(View.GONE);

        } else {
            User currentUser = sharedPreferencesUtil.getUser();
            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setOnClickListener(v -> logout());

            if (currentUser != null && currentUser.isAdmin()) {
                // Admin User: Hide Vibration, Notifications, Location, SMS, Contacts, Fall Detection
                switchVibration.setVisibility(View.GONE);
                switchNotifications.setVisibility(View.GONE);
                switchLocation.setVisibility(View.GONE);
                switchSms.setVisibility(View.GONE);
                switchContacts.setVisibility(View.GONE);
                switchFallDetection.setVisibility(View.GONE);

                // Hide corresponding separators to maintain a clean layout
                if (separatorVibration != null) separatorVibration.setVisibility(View.GONE);
                if (separatorNotifications != null) separatorNotifications.setVisibility(View.GONE);
                if (separatorGallery != null) separatorGallery.setVisibility(View.GONE);
                if (separatorLocation != null) separatorLocation.setVisibility(View.GONE);
                if (separatorSms != null) separatorSms.setVisibility(View.GONE);
                if (separatorFallDetection != null) separatorFallDetection.setVisibility(View.GONE);

                // Ensure visible items and their necessary separators are shown
                switchCamera.setVisibility(View.VISIBLE);
                if (separatorCamera != null) separatorCamera.setVisibility(View.VISIBLE);
                switchGallery.setVisibility(View.VISIBLE);
            } else {
                // Regular Logged-in User: Show everything
                switchVibration.setVisibility(View.VISIBLE);
                switchNotifications.setVisibility(View.VISIBLE);
                switchCamera.setVisibility(View.VISIBLE);
                switchGallery.setVisibility(View.VISIBLE);
                switchLocation.setVisibility(View.VISIBLE);
                switchSms.setVisibility(View.VISIBLE);
                switchContacts.setVisibility(View.VISIBLE);
                switchFallDetection.setVisibility(View.VISIBLE);

                if (separatorVibration != null) separatorVibration.setVisibility(View.VISIBLE);
                if (separatorNotifications != null)
                    separatorNotifications.setVisibility(View.VISIBLE);
                if (separatorCamera != null) separatorCamera.setVisibility(View.VISIBLE);
                if (separatorGallery != null) separatorGallery.setVisibility(View.VISIBLE);
                if (separatorLocation != null) separatorLocation.setVisibility(View.VISIBLE);
                if (separatorSms != null) separatorSms.setVisibility(View.VISIBLE);
                if (separatorFallDetection != null)
                    separatorFallDetection.setVisibility(View.VISIBLE);
            }
            if (separatorLogout != null) separatorLogout.setVisibility(View.VISIBLE);
        }

        boolean isDarkMode = sharedPreferencesUtil.isDarkMode();
        switchDarkMode.setChecked(isDarkMode);
        updateDarkModeText(switchDarkMode, isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferencesUtil.setDarkMode(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            updateDarkModeText(switchDarkMode, isChecked);
        });

        // Individual permission toggles
        switchNotifications.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                if (isChecked) {
                    runWithPermission(Manifest.permission.POST_NOTIFICATIONS, this::refreshSwitches);
                } else {
                    v.setChecked(true);
                    showRedirectDialog();
                }
            }
        });

        switchCamera.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                if (isChecked) {
                    runWithPermission(Manifest.permission.CAMERA, this::refreshSwitches);
                } else {
                    v.setChecked(true);
                    showRedirectDialog();
                }
            }
        });

        switchGallery.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                if (isChecked) {
                    runWithPermission(Manifest.permission.READ_MEDIA_IMAGES, this::refreshSwitches);
                } else {
                    v.setChecked(true);
                    showRedirectDialog();
                }
            }
        });

        switchLocation.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                if (isChecked) {
                    runWithPermission(Manifest.permission.ACCESS_FINE_LOCATION, this::refreshSwitches);
                } else {
                    v.setChecked(true);
                    showRedirectDialog();
                }
            }
        });

        switchSms.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                if (isChecked) {
                    runWithPermission(Manifest.permission.SEND_SMS, this::refreshSwitches);
                } else {
                    v.setChecked(true);
                    showRedirectDialog();
                }
            }
        });

        switchContacts.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                if (isChecked) {
                    runWithPermission(Manifest.permission.READ_CONTACTS, this::refreshSwitches);
                } else {
                    v.setChecked(true);
                    showRedirectDialog();
                }
            }
        });

        switchVibration.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed() && !isChecked) {
                v.setChecked(true);
                showRedirectDialog();
            }
        });

        switchFallDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (isChecked) {
                    handleFallDetectionEnable();
                } else {
                    disableFallDetection();
                }
            }
        });
    }

    /**
     * Displays a confirmation dialog redirecting the user to system settings to revoke permissions.
     */
    private void showRedirectDialog() {
        dialogService.showConfirmDialog(getSupportFragmentManager(),
                "ניהול הרשאות",
                "כדי לבטל הרשאות, עליך לעבור להגדרות המערכת של האפליקציה.",
                "להגדרות", "ביטול",
                this::openAppSettings);
    }

    /**
     * Updates the checked state of permission-related switches based on actual system status.
     */
    private void updatePermissionSwitches() {
        if (switchVibration != null)
            switchVibration.setChecked(true); // Vibration is normal permission

        if (switchNotifications != null) {
            switchNotifications.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
        }

        if (switchCamera != null) {
            switchCamera.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        }

        if (switchGallery != null) {
            boolean hasGalleryPerm;
            hasGalleryPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            switchGallery.setChecked(hasGalleryPerm);
        }

        if (switchLocation != null) {
            switchLocation.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        }

        if (switchSms != null) {
            switchSms.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
        }

        if (switchContacts != null) {
            switchContacts.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
        }

        if (switchFallDetection != null) {
            boolean hasActivity = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
            boolean hasBackgroundLoc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean hasFineLoc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean hasSms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
            boolean hasNotifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

            User user = sharedPreferencesUtil.getUser();
            boolean hasContacts = user != null && !user.getEmergencyContacts().isEmpty();
            boolean isEnabledByUser = sharedPreferencesUtil.isFallDetectionEnabled();

            // If it was enabled but there are no contacts, disable it
            if (isEnabledByUser && !hasContacts) {
                sharedPreferencesUtil.setFallDetectionEnabled(false);
                fallDetectionService.stopMonitoring();
                isEnabledByUser = false;
            }

            switchFallDetection.setChecked(hasActivity && hasBackgroundLoc && hasFineLoc && hasSms && hasNotifications && isEnabledByUser);
        }
    }

    /**
     * Refreshes all permission-related switches based on current system state.
     */
    private void refreshSwitches() {
        updatePermissionSwitches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh switches when returning from system settings
        refreshSwitches();
    }

    /**
     * Orchestrates the complex flow of enabling fall detection.
     * Checks and requests multiple required permissions: Activity Recognition,
     * Fine Location, SMS, and Notifications.
     */
    private void handleFallDetectionEnable() {
        runWithPermissions(this::checkBackgroundLocationAndEnable,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.POST_NOTIFICATIONS
        );
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
                    () -> runWithPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, this::checkContactsAndEnableFallDetection));
            switchFallDetection.setChecked(false);
        } else {
            checkContactsAndEnableFallDetection();
        }
    }

    /**
     * Processes the results of the permission request specifically for fall detection.
     *
     * @param isGranted Map of permissions and their grant status.
     */
    @Override
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
        refreshSwitches();
    }

    /**
     * Disables the fall detection background service.
     */
    private void disableFallDetection() {
        fallDetectionService.stopMonitoring();
        sharedPreferencesUtil.setFallDetectionEnabled(false);
        Toast.makeText(this, "זיהוי נפילות הופסק", Toast.LENGTH_SHORT).show();
    }

    /**
     * Final validation step before enabling fall detection: ensures the user has at least one emergency contact.
     */
    private void checkContactsAndEnableFallDetection() {
        User user = sharedPreferencesUtil.getUser();
        if (user == null) return;

        List<EmergencyContact> contacts = new ArrayList<>(user.getEmergencyContacts().values());
        if (!contacts.isEmpty()) {
            fallDetectionService.startMonitoring();
            sharedPreferencesUtil.setFallDetectionEnabled(true);
            Toast.makeText(SettingsActivity.this, "זיהוי נפילות הופעל ברקע", Toast.LENGTH_SHORT).show();
        } else {
            switchFallDetection.setChecked(false);
            sharedPreferencesUtil.setFallDetectionEnabled(false);
            Toast.makeText(SettingsActivity.this, "נא להוסיף אנשי קשר לחירום לפני הפעלת הזיהוי", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updates the text label of the Dark Mode switch.
     *
     * @param switchDarkMode The switch component.
     * @param isDarkMode     Current dark mode state.
     */
    private void updateDarkModeText(SwitchMaterial switchDarkMode, boolean isDarkMode) {
        switchDarkMode.setText(isDarkMode ? R.string.bright_mode : R.string.dark_mode);
    }

    /**
     * Logs the current user out after a confirmation dialog.
     */
    private void logout() {
        Runnable onConfirm = () -> {
            fallDetectionService.stopMonitoring();
            String email = databaseService.getAuthService().logout();
            sharedPreferencesUtil.signOutUser();
            Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.putExtra("userEmail", email);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        };

        dialogService.showConfirmDialog(getSupportFragmentManager(), "התנתקות", "האם ברצונך להתנתק?", "התנתק", "בטל", onConfirm);
    }
}