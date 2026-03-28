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
 * A fragment that represents the sidebar navigation menu for authenticated regular users.
 * <p>
 * This fragment provides links to core features available to logged-in users, including
 * the main dashboard, user profile details, contact information, and account settings.
 * It uses the {@link MenuNavigationListener} to notify the host activity of navigation requests.
 * </p>
 */
@AndroidEntryPoint
public class LoggedInMenuFragment extends Fragment {
    /**
     * Listener for handling navigation events in the host activity.
     */
    private MenuNavigationListener navigationListener;

    /**
     * Called when the fragment is first attached to its context.
     * <p>
     * Ensures the host context implements {@link MenuNavigationListener} for decoupled communication.
     * </p>
     *
     * @param context The host context (Activity).
     * @throws RuntimeException If the context does not implement the required listener interface.
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
     * Initializes the fragment's UI view and sets up navigation buttons.
     *
     * @param inflater           The LayoutInflater object to inflate views.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_menu_logged_in, container, false);

        setupNavigationButton(view, R.id.btn_menu_main, R.id.mainActivity);
        setupNavigationButton(view, R.id.btn_menu_contact, R.id.contactActivity);
        setupNavigationButton(view, R.id.btn_menu_details, R.id.detailsAboutUserActivity);
        setupNavigationButton(view, R.id.btn_menu_settings, R.id.settingsActivity);

        return view;
    }

    /**
     * Helper to set up a navigation button with a click listener.
     *
     * @param root          The root view containing the button.
     * @param buttonId      The resource ID of the button.
     * @param destinationId The navigation destination ID.
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
     * Called when the fragment is no longer attached to its activity.
     * Cleans up the listener reference to avoid memory leaks.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}
