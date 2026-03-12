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

@AndroidEntryPoint
public class LoggedOutMenuFragment extends Fragment {
    private MenuNavigationListener navigationListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuNavigationListener) {
            navigationListener = (MenuNavigationListener) context;
        } else {
            throw new RuntimeException(context + " must implement MenuNavigationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_menu_logged_out, container, false);

        setupNavigationButton(view, R.id.btn_menu_main, R.id.landingFragment);
        setupNavigationButton(view, R.id.btn_menu_contact, R.id.contactFragment);
        setupNavigationButton(view, R.id.btn_menu_login, R.id.loginFragment);
        setupNavigationButton(view, R.id.btn_menu_register, R.id.registerFragment);

        view.findViewById(R.id.btn_menu_settings).setOnClickListener(v -> {
            if (navigationListener != null) {
                Bundle args = new Bundle();
                args.putBoolean("isFromLoggedIn", false);
                navigationListener.onNavigate(R.id.settingsFragment, args);
            }
        });

        return view;
    }

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

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}
