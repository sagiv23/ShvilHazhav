package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The initial landing screen for users who are not logged in.
 * <p>
 * This fragment provides the entry points for new and returning users,
 * with buttons to navigate to the Login, Registration, and Contact/Support screens.
 * </p>
 */
@AndroidEntryPoint
public class LandingFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_landing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnToContact = view.findViewById(R.id.btn_landingBody_to_contact);
        Button btnToLogin = view.findViewById(R.id.btn_landingBody_to_login);
        Button btnToRegister = view.findViewById(R.id.btn_landingBody_to_register);

        btnToContact.setOnClickListener(v -> navigateTo(R.id.action_landingFragment_to_contactFragment));
        btnToLogin.setOnClickListener(v -> navigateTo(R.id.action_landingFragment_to_loginFragment));
        btnToRegister.setOnClickListener(v -> navigateTo(R.id.action_landingFragment_to_registerFragment));
    }
}
