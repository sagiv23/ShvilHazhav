package com.example.sagivproject.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sagivproject.R;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment that represents the sidebar navigation menu for unauthenticated (logged out) users.
 * <p>
 * This fragment provides links to basic features and onboarding screens, including
 * the landing page, login, registration, contact information, and global application settings.
 * It uses the {@link MenuNavigationListener} to facilitate navigation in the host activity.
 * </p>
 */
@AndroidEntryPoint
public class LoggedOutMenuFragment extends Fragment {
    /**
     * Listener for handling navigation events in the host activity.
     */
    private MenuNavigationListener navigationListener;

    /**
     * Called when the fragment is first attached to its activity.
     * <p>
     * Verifies that the hosting context implements the navigation listener.
     * </p>
     * @param context The host Activity.
     * @throws RuntimeException If the activity does not implement {@link MenuNavigationListener}.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuNavigationListener) {
            navigationListener = (MenuNavigationListener) context;
        } else {
            throw new RuntimeException(context + " must implement MenuNavigationListener");
        }
    }

    /**
     * Initializes the fragment's UI and configures the navigation buttons.
     * @param inflater The LayoutInflater.
     * @param container The parent container view.
     * @param savedInstanceState Fragment's saved state.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_menu_logged_out, container, false);

        setupNavigationButton(view, R.id.btn_menu_main, R.id.landingActivity);
        setupNavigationButton(view, R.id.btn_menu_contact, R.id.contactActivity);
        setupNavigationButton(view, R.id.btn_menu_login, R.id.loginActivity);
        setupNavigationButton(view, R.id.btn_menu_register, R.id.registerActivity);
        setupNavigationButton(view, R.id.btn_menu_settings, R.id.settingsActivity);

        return view;
    }

    /**
     * Helper to set up a navigation button with a standardized click listener.
     * @param root The root view of the fragment.
     * @param buttonId The resource ID of the button.
     * @param destinationId The destination identifier.
     */
    private void setupNavigationButton(View root, int buttonId, int destinationId) {
        View button = root.findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> {
                if (navigationListener != null) {
                    navigationListener.onNavigate(destinationId);
                }
            });
        }
    }

    /**
     * Cleans up the listener reference when the fragment is detached.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}