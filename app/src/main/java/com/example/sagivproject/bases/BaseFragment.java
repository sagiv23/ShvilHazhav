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
 */
@AndroidEntryPoint
public abstract class BaseFragment extends Fragment {
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

    protected NavController navController;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupMenu(this);
        }
    }

    /**
     * Helper to request permissions.
     */
    protected void requestPermissions(String... permissions) {
        requestPermissionLauncher.launch(permissions);
    }

    /**
     * Callback for permission results. Can be overridden by subclasses.
     */
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
    }

    protected void navigateTo(int resId) {
        if (navController != null) {
            navController.navigate(resId);
        }
    }

    /**
     * Navigates using Safe Args Directions.
     *
     * @param directions The generated Directions class.
     */
    protected void navigateTo(NavDirections directions) {
        if (navController != null) {
            navController.navigate(directions);
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
}
