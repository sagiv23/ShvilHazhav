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
 * This fragment is dynamically loaded into the top bar of the {@link com.example.sagivproject.screens.MainActivity}
 * when an admin user is logged in. It provides a simple way to navigate back to the main admin dashboard.
 * </p>
 */
@AndroidEntryPoint
public class AdminMenuFragment extends Fragment {
    private MenuNavigationListener navigationListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the host activity implements the navigation listener
        if (context instanceof MenuNavigationListener) {
            navigationListener = (MenuNavigationListener) context;
        } else {
            throw new RuntimeException(context + " must implement MenuNavigationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_menu_admin, container, false);

        View btnAdmin = view.findViewById(R.id.btn_menu_admin_back);
        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(v -> {
                if (navigationListener != null) {
                    navigationListener.onNavigate(R.id.adminPageFragment);
                }
            });
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}
