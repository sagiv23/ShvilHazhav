package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.utils.PagePermissions;

public class LandingActivity extends AppCompatActivity {
    Button btnToContact, btnToLogin, btnToRegister, btnNavToContact, btnNavToLogin, btnNavToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);

        PagePermissions.redirectIfLoggedIn(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.landing_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToContact = findViewById(R.id.btn_landingBody_to_contact);
        btnToLogin = findViewById(R.id.btn_landingBody_to_login);
        btnToRegister = findViewById(R.id.btn_landingBody_to_register);
        btnNavToContact = findViewById(R.id.btn_landing_to_contact);
        btnNavToLogin = findViewById(R.id.btn_landing_to_login);
        btnNavToRegister = findViewById(R.id.btn_landing_to_register);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, ContactActivity.class)));
        btnNavToContact.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, ContactActivity.class)));
        btnToLogin.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, LoginActivity.class)));
        btnNavToLogin.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, LoginActivity.class)));
        btnToRegister.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, RegisterActivity.class)));
        btnNavToRegister.setOnClickListener(view -> startActivity(new Intent(LandingActivity.this, RegisterActivity.class)));
    }
}