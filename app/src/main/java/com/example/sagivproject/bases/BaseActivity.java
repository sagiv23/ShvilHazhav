package com.example.sagivproject.bases;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.ContactActivity;
import com.example.sagivproject.screens.DetailsAboutUserActivity;
import com.example.sagivproject.screens.LandingActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.MainActivity;
import com.example.sagivproject.screens.RegisterActivity;
import com.example.sagivproject.screens.SettingsActivity;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An abstract base class for all activities in the application.
 * <p>
 * This class provides common functionality that is shared across multiple activities,
 * such as dependency injection for services, and a standardized way to set up a top navigation menu.
 * It also includes a mechanism for requesting necessary permissions.
 * </p>
 */
@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity {
    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;
    @Inject
    protected IDatabaseService databaseService;

    /**
     * Initializes the activity and requests permissions if required.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this instanceof RequiresPermissions) {
            requestPermissions();
        }
    }

    /**
     * Determines which top menu to show based on user state and inflates it.
     *
     * @param menuContainer The ViewGroup into which the menu will be inflated.
     */
    protected void setupTopMenu(ViewGroup menuContainer) {
        User currentUser = sharedPreferencesUtil.getUser();

        if (currentUser != null) {
            if (currentUser.isAdmin()) {
                setupAdminMenu(menuContainer);
            } else {
                setupLoggedInMenu(menuContainer);
            }
        } else {
            setupLoggedOutMenu(menuContainer);
        }
    }

    /**
     * Inflates and configures the top menu for an admin user.
     */
    private void setupAdminMenu(ViewGroup menuContainer) {
        getLayoutInflater().inflate(R.layout.top_menu_admin, menuContainer, true);
        Button btnAdmin = findViewById(R.id.btn_menu_admin_back);
        btnAdmin.setOnClickListener(v -> finish());
    }

    /**
     * Inflates and configures the top menu for a regular (non-admin) logged-in user.
     */
    private void setupLoggedInMenu(ViewGroup menuContainer) {
        getLayoutInflater().inflate(R.layout.top_menu_logged_in, menuContainer, true);
        setupNavigationButton(R.id.btn_menu_main, MainActivity.class);
        setupNavigationButton(R.id.btn_menu_contact, ContactActivity.class);
        setupNavigationButton(R.id.btn_menu_details, DetailsAboutUserActivity.class);
        setupSettingsButton(R.id.btn_menu_settings, true);
    }

    /**
     * Inflates and configures the top menu for a logged-out user.
     */
    private void setupLoggedOutMenu(ViewGroup menuContainer) {
        getLayoutInflater().inflate(R.layout.top_menu_logged_out, menuContainer, true);
        setupNavigationButton(R.id.btn_menu_main, LandingActivity.class);
        setupNavigationButton(R.id.btn_menu_contact, ContactActivity.class);
        setupNavigationButton(R.id.btn_menu_login, LoginActivity.class);
        setupNavigationButton(R.id.btn_menu_register, RegisterActivity.class);
        setupSettingsButton(R.id.btn_menu_settings, false);
    }

    /**
     * Helper method to set up a navigation button's click listener.
     */
    private void setupNavigationButton(int buttonId, Class<?> targetActivity) {
        findViewById(buttonId).setOnClickListener(v -> navigateIfNotCurrent(targetActivity));
    }

    /**
     * Helper method to set up the settings button's click listener with the correct intent extra.
     */
    private void setupSettingsButton(int buttonId, boolean isFromLoggedIn) {
        ImageButton btnSettings = findViewById(buttonId);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("isFromLoggedIn", isFromLoggedIn);
            startActivity(intent);
        });
    }

    /**
     * Navigates to a target activity, but only if it is not the current activity.
     *
     * @param targetActivity The class of the activity to navigate to.
     */
    protected void navigateIfNotCurrent(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) {
            startActivity(new Intent(this, targetActivity));
        }
    }

    /**
     * Requests a standard set of required permissions for the application.
     * NOTE: This does not handle the result of the permission request (e.g., if the user denies them).
     */
    protected void requestPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        permissions.add(Manifest.permission.POST_NOTIFICATIONS);

        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 1001);
    }

    /**
     * A marker interface to indicate that an activity requires special permissions.
     * Activities implementing this interface will have {@link #requestPermissions()} called automatically.
     */
    public interface RequiresPermissions {
    }
}
