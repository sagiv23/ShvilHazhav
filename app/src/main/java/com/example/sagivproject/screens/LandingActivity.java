package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;

/**
 * The initial landing screen for users who are not logged in.
 * <p>
 * This activity provides options to navigate to the Login, Register, or Contact screens.
 * </p>
 */
public class LandingActivity extends BaseActivity implements BaseActivity.RequiresPermissions {

    /**
     * Initializes the activity, sets up the UI, and configures button click listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.landing_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        Button btnToContact = findViewById(R.id.btn_landingBody_to_contact);
        Button btnToLogin = findViewById(R.id.btn_landingBody_to_login);
        Button btnToRegister = findViewById(R.id.btn_landingBody_to_register);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, ContactActivity.class)));
        btnToLogin.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, LoginActivity.class)));
        btnToRegister.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, RegisterActivity.class)));
    }
}
