package com.example.sagivproject.bases;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.sagivproject.R;
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
import java.util.Objects;

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
     * Inflates and configures the appropriate top menu based on the user's login status and role.
     *
     * @param menuContainer The ViewGroup into which the menu will be inflated.
     */
    protected void setupTopMenu(ViewGroup menuContainer) {
        boolean isUserLoggedIn = sharedPreferencesUtil.isUserLoggedIn();

        if (isUserLoggedIn) {
            if (Objects.requireNonNull(sharedPreferencesUtil.getUser()).isAdmin()) {
                @LayoutRes int menuLayout = R.layout.top_menu_admin;
                getLayoutInflater().inflate(menuLayout, menuContainer, true);

                Button btnAdmin = findViewById(R.id.btn_menu_admin_back);
                btnAdmin.setOnClickListener(v -> finish());
            } else {
                @LayoutRes int menuLayout = R.layout.top_menu_logged_in;
                getLayoutInflater().inflate(menuLayout, menuContainer, true);

                Button btnMain = findViewById(R.id.btn_menu_main);
                Button btnContact = findViewById(R.id.btn_menu_contact);
                Button btnDetailsAboutUser = findViewById(R.id.btn_menu_details);
                ImageButton btnSettings = findViewById(R.id.btn_menu_settings);

                btnMain.setOnClickListener(v -> navigateIfNotCurrent(MainActivity.class));
                btnContact.setOnClickListener(v -> navigateIfNotCurrent(ContactActivity.class));
                btnDetailsAboutUser.setOnClickListener(v -> navigateIfNotCurrent(DetailsAboutUserActivity.class));
                btnSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.putExtra("isFromLoggedIn", true);
                    startActivity(intent);
                });
            }
        } else {
            @LayoutRes int menuLayout = R.layout.top_menu_logged_out;
            getLayoutInflater().inflate(menuLayout, menuContainer, true);

            Button btnLanding = findViewById(R.id.btn_menu_main);
            Button btnContact = findViewById(R.id.btn_menu_contact);
            Button btnLogin = findViewById(R.id.btn_menu_login);
            Button btnRegister = findViewById(R.id.btn_menu_register);
            ImageButton btnSettings = findViewById(R.id.btn_menu_settings);

            btnLanding.setOnClickListener(v -> navigateIfNotCurrent(LandingActivity.class));
            btnContact.setOnClickListener(v -> navigateIfNotCurrent(ContactActivity.class));
            btnLogin.setOnClickListener(v -> navigateIfNotCurrent(LoginActivity.class));
            btnRegister.setOnClickListener(v -> navigateIfNotCurrent(RegisterActivity.class));
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("isFromLoggedIn", false);
                startActivity(intent);
            });
        }
    }

    /**
     * Navigates to a target activity, but only if it is not the current activity.
     *
     * @param targetActivity The class of the activity to navigate to.
     */
    protected void navigateIfNotCurrent(Class<?> targetActivity) {
        if (this.getClass().equals(targetActivity)) {
            return;
        }
        startActivity(new Intent(this, targetActivity));
    }

    /**
     * Requests a standard set of required permissions for the application.
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
