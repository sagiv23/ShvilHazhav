package com.example.sagivproject.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sagivproject.R;
import com.example.sagivproject.screens.ContactActivity;
import com.example.sagivproject.screens.LandingActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.RegisterActivity;
import com.example.sagivproject.screens.SettingsActivity;
import com.google.android.material.button.MaterialButton;

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

        setupNavigationButton(view, R.id.btn_menu_main, LandingActivity.class);
        setupNavigationButton(view, R.id.btn_menu_contact, ContactActivity.class);
        setupNavigationButton(view, R.id.btn_menu_login, LoginActivity.class);
        setupNavigationButton(view, R.id.btn_menu_register, RegisterActivity.class);
        setupSettingsButton(view, R.id.btn_menu_settings);

        return view;
    }

    private void setupNavigationButton(View root, int buttonId, Class<?> targetActivity) {
        root.findViewById(buttonId).setOnClickListener(v -> {
            if (navigationListener != null) {
                navigationListener.onNavigate(targetActivity);
            }
        });
    }

    private void setupSettingsButton(View root, int buttonId) {
        MaterialButton btnSettings = root.findViewById(buttonId);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            intent.putExtra("isFromLoggedIn", false);
            startActivity(intent);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }
}
