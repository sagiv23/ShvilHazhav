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
 * A unified fragment for all navigation menus (Admin, Logged In, Logged Out).
 * The menu type is determined by the {@code ARG_MENU_TYPE} argument.
 */
@AndroidEntryPoint
public class AppMenuFragment extends Fragment {
    private static final String ARG_MENU_TYPE = "menu_type";
    private OnNavigationListener navigationListener;
    private MenuType menuType;

    /**
     * Creates a new instance of the fragment with the specified menu type.
     */
    public static AppMenuFragment newInstance(MenuType type) {
        AppMenuFragment fragment = new AppMenuFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MENU_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is first attached to its context.
     * <p>
     * Verifies that the host context (Activity) implements {@link OnNavigationListener}
     * to facilitate decoupled navigation.
     * </p>
     *
     * @param context The host context.
     * @throws RuntimeException If the context does not implement {@link OnNavigationListener}.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationListener) {
            navigationListener = (OnNavigationListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnNavigationListener");
        }
    }

    /**
     * Initializes the fragment and retrieves the menu type from arguments.
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            menuType = getArguments().getSerializable(ARG_MENU_TYPE, MenuType.class);
        }
    }

    /**
     * Initializes the fragment's UI view.
     *
     * @param inflater           The LayoutInflater object to inflate views.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId;
        if (menuType == MenuType.ADMIN) {
            layoutId = R.layout.top_menu_admin;
        } else if (menuType == MenuType.LOGGED_IN) {
            layoutId = R.layout.top_menu_logged_in;
        } else {
            layoutId = R.layout.top_menu_logged_out;
        }

        View view = inflater.inflate(layoutId, container, false);

        if (menuType == MenuType.ADMIN) {
            setupNavigationButton(view, R.id.btn_menu_admin_back, R.id.adminPageActivity);
        } else if (menuType == MenuType.LOGGED_IN) {
            setupNavigationButton(view, R.id.btn_menu_main, R.id.mainActivity);
            setupNavigationButton(view, R.id.btn_menu_contact, R.id.contactActivity);
            setupNavigationButton(view, R.id.btn_menu_details, R.id.detailsAboutUserActivity);
            setupNavigationButton(view, R.id.btn_menu_settings, R.id.settingsActivity);
        } else {
            setupNavigationButton(view, R.id.btn_menu_main, R.id.landingActivity);
            setupNavigationButton(view, R.id.btn_menu_contact, R.id.contactActivity);
            setupNavigationButton(view, R.id.btn_menu_login, R.id.loginActivity);
            setupNavigationButton(view, R.id.btn_menu_register, R.id.registerActivity);
            setupNavigationButton(view, R.id.btn_menu_settings, R.id.settingsActivity);
        }

        return view;
    }

    /**
     * Helper to set up a navigation button with a standardized click listener.
     *
     * @param root          The root view of the fragment.
     * @param buttonId      The resource ID of the button.
     * @param destinationId The destination identifier (resource ID).
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
     * Cleans up the navigation listener reference.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }

    /**
     * @return The current menu type of this fragment instance.
     */
    public MenuType getMenuType() {
        return menuType;
    }

    public enum MenuType {ADMIN, LOGGED_IN, LOGGED_OUT}

    /**
     * Interface for communication between the menu fragment and the hosting activity.
     */
    public interface OnNavigationListener {
        /**
         * Navigates to a specific destination in the navigation graph.
         *
         * @param resId The resource ID of the destination or action.
         */
        void onNavigate(int resId);

        /**
         * Navigates to a specific destination in the navigation graph with arguments.
         *
         * @param resId The resource ID of the destination or action.
         * @param args  The arguments to pass to the destination fragment.
         */
        void onNavigate(int resId, Bundle args);
    }
}
