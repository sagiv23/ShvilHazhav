package com.example.sagivproject.bases;

import android.os.Bundle;
import android.view.View;

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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.sagivproject.screens.MainActivity;
import com.example.sagivproject.services.AdapterService;
import com.example.sagivproject.services.DialogService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An abstract base class for all fragments in the application.
 * <p>
 * This class handles common fragment logic, such as dependency injection via Hilt,
 * navigation using the {@link NavController}, requesting permissions, and applying
 * window insets for edge-to-edge support. It also ensures that the top bar/menu
 * is correctly set up in the {@link MainActivity}.
 * </p>
 */
@AndroidEntryPoint
public abstract class BaseFragment extends Fragment {
    /**
     * Launcher for requesting multiple runtime permissions.
     */
    protected final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;
    @Inject
    protected IDatabaseService databaseService;
    @Inject
    protected AdapterService adapterService;
    @Inject
    protected DialogService dialogService;

    /**
     * The NavController used for fragment navigation.
     */
    protected NavController navController;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Apply window insets to handle system bars (status/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Trigger menu setup in MainActivity based on the current fragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupMenu(this);
        }
    }

    /**
     * Helper to request one or more runtime permissions.
     *
     * @param permissions The list of permissions to request.
     */
    protected void requestPermissions(String... permissions) {
        requestPermissionLauncher.launch(permissions);
    }

    /**
     * Callback for permission results. Subclasses can override this to handle
     * specific permission granting or denial logic.
     *
     * @param isGranted A map containing the permission strings and their grant status.
     */
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
    }

    /**
     * Navigates to a specific destination by its resource ID.
     *
     * @param resId The ID of the destination fragment or action.
     */
    protected void navigateTo(int resId) {
        if (navController != null) {
            navController.navigate(resId);
        }
    }

    /**
     * Navigates using Safe Args Directions.
     *
     * @param directions The generated Directions class containing the destination and arguments.
     */
    protected void navigateTo(NavDirections directions) {
        if (navController != null) {
            navController.navigate(directions);
        }
    }

    /**
     * Helper method to retrieve a color from resources in a Fragment-safe way.
     *
     * @param resId The color resource ID.
     * @return The color integer.
     */
    @ColorInt
    protected int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(requireContext(), resId);
    }
}
