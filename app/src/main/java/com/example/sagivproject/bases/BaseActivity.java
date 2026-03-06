package com.example.sagivproject.bases;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.sagivproject.models.User;
import com.example.sagivproject.services.AdapterService;
import com.example.sagivproject.services.DialogService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.ui.AdminMenuFragment;
import com.example.sagivproject.ui.LoggedInMenuFragment;
import com.example.sagivproject.ui.LoggedOutMenuFragment;
import com.example.sagivproject.ui.MenuNavigationListener;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.ImageUtil;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.example.sagivproject.utils.Validator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An abstract base class for all activities in the application.
 */
@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity implements MenuNavigationListener {
    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;
    @Inject
    protected IDatabaseService databaseService;
    @Inject
    protected AdapterService adapterService;
    @Inject
    protected DialogService dialogService;
    @Inject
    protected Validator validator;
    @Inject
    protected CalendarUtil calendarUtil;
    @Inject
    protected ImageUtil imageUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this instanceof RequiresPermissions) {
            requestPermissions();
        }
    }

    /**
     * Determines which top menu Fragment to show based on user state and replaces it in the container.
     *
     * @param menuContainer The ViewGroup into which the menu will be inflated.
     */
    protected void setupTopMenu(ViewGroup menuContainer) {
        if (menuContainer == null) return;

        // Check if a fragment is already attached to this container to avoid duplicates on rotation
        if (getSupportFragmentManager().findFragmentById(menuContainer.getId()) != null) {
            return;
        }

        User currentUser = sharedPreferencesUtil.getUser();
        Fragment menuFragment;

        if (currentUser != null) {
            if (currentUser.isAdmin()) {
                menuFragment = new AdminMenuFragment();
            } else {
                menuFragment = new LoggedInMenuFragment();
            }
        } else {
            menuFragment = new LoggedOutMenuFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(menuContainer.getId(), menuFragment)
                .commit();
    }

    /**
     * Implements MenuNavigationListener.
     */
    @Override
    public void onNavigate(Class<?> targetActivity) {
        navigateIfNotCurrent(targetActivity);
    }

    /**
     * Navigates to a target activity, but only if it is not the current activity.
     *
     * @param targetActivity The class of the activity to navigate to.
     */
    public void navigateIfNotCurrent(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) {
            startActivity(new Intent(this, targetActivity));
        }
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

    public interface RequiresPermissions {
    }
}
