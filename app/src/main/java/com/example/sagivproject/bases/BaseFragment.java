package com.example.sagivproject.bases;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.AdapterService;
import com.example.sagivproject.services.DialogService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.ui.AdminMenuFragment;
import com.example.sagivproject.ui.LoggedInMenuFragment;
import com.example.sagivproject.ui.LoggedOutMenuFragment;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.ImageUtil;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.example.sagivproject.utils.Validator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An abstract base class for all fragments in the application.
 */
@AndroidEntryPoint
public abstract class BaseFragment extends Fragment {
    // Register the permissions callback, which handles the user's response to the system permissions dialog.
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                // You can add logic here to handle the result (e.g., showing a message if a permission is denied)
            });
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
    protected NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this instanceof RequiresPermissions) {
            requestPermissions();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = view.findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);
    }

    /**
     * Navigates to a destination.
     *
     * @param resId The action id or destination id.
     */
    protected void navigateTo(int resId) {
        if (navController != null) {
            navController.navigate(resId);
        }
    }

    /**
     * Navigates to a destination with arguments.
     *
     * @param resId The action id or destination id.
     * @param args  The arguments to pass.
     */
    protected void navigateTo(int resId, Bundle args) {
        if (navController != null) {
            navController.navigate(resId, args);
        }
    }

    /**
     * Helper method to get a color from resources in a Fragment-safe way.
     *
     * @param resId The color resource ID.
     * @return The color integer.
     */
    @ColorInt
    protected int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(requireContext(), resId);
    }

    /**
     * Determines which top menu Fragment to show based on user state and replaces it in the container.
     *
     * @param menuContainer The ViewGroup into which the menu will be inflated.
     */
    protected void setupTopMenu(ViewGroup menuContainer) {
        if (menuContainer == null) return;

        // Check if a fragment is already attached to this container to avoid duplicates
        if (getChildFragmentManager().findFragmentById(menuContainer.getId()) != null) {
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

        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(menuContainer.getId(), menuFragment)
                .commit();
    }

    /**
     * Requests a standard set of required permissions for the application.
     * Uses the modern ActivityResultLauncher API to avoid deprecation.
     */
    protected void requestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.POST_NOTIFICATIONS
        };
        requestPermissionLauncher.launch(permissions);
    }

    public interface RequiresPermissions {
    }
}
