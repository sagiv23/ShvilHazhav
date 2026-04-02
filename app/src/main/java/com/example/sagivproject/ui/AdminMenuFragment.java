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
 * A fragment that represents the top navigation menu for administrators.
 * <p>
 * This fragment is dynamically loaded into the top bar of activities when an admin user is logged in.
 * It provides a simplified navigation interface focused on administrative tasks and dashboard access.
 * </p>
 */
@AndroidEntryPoint
public class AdminMenuFragment extends Fragment {
    /**
     * Listener for handling navigation requests back to the host activity.
     */
    private MenuNavigationListener navigationListener;

    /**
     * Called when the fragment is first attached to its context.
     * <p>
     * Verifies that the host context (Activity) implements {@link MenuNavigationListener}
     * to facilitate decoupled navigation.
     * </p>
     *
     * @param context The host context.
     * @throws RuntimeException If the context does not implement {@link MenuNavigationListener}.
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
        View view = inflater.inflate(R.layout.top_menu_admin, container, false);

        View btnAdmin = view.findViewById(R.id.btn_menu_admin_back);
        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(v -> {
                if (navigationListener != null) {
                    navigationListener.onNavigate(R.id.adminPageActivity);
                }
            });
        }

        return view;
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
}