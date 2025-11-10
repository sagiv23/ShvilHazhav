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
import com.example.sagivproject.models.AuthHelper;

public class MainActivity extends AppCompatActivity {
    Button btnToContact, btnToLogin, btnToRegister, btnNavToContact, btnNavToLogin, btnNavToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (!AuthHelper.checkUserLoggedInFromspecialActivities(MainActivity.this)) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToContact = findViewById(R.id.btnMainPageToContactPage);
        btnToLogin = findViewById(R.id.btnMainPageToLoginPage);
        btnToRegister = findViewById(R.id.btnMainPageToRegisterPage);
        btnNavToContact = findViewById(R.id.btnMainPageNavToContactPage);
        btnNavToLogin = findViewById(R.id.btnMainPageNavToLoginPage);
        btnNavToRegister = findViewById(R.id.btnMainPageNavToRegisterPage);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ContactActivity.class)));
        btnNavToContact.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ContactActivity.class)));
        btnToLogin.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
        btnNavToLogin.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
        btnToRegister.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        btnNavToRegister.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
    }
}