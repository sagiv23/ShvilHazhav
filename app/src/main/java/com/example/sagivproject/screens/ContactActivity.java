package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.screens.base.BaseActivity;
import com.example.sagivproject.utils.SharedPreferencesUtil;

public class ContactActivity extends BaseActivity {
    //כפתורים למשתמש מחובר
    private Button btnToMain, btnToDetailsAboutUser, btnToExit, btnToContactPage1;
    //כפתורים למשתמש לא מחובר
    private Button btnToLanding, btnToLoginPage, btnToRegisterPage, btnToContactPage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contactPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //משתמש מחובר
        btnToMain = findViewById(R.id.btn_contact_to_main);
        btnToContactPage1 = findViewById(R.id.btn_contact_to_contact1);
        btnToDetailsAboutUser = findViewById(R.id.btn_contact_to_DetailsAboutUser);
        btnToExit = findViewById(R.id.btn_contact_to_exit);

        //משתמש לא מחובר
        btnToLanding = findViewById(R.id.btn_contact_to_landing);
        btnToContactPage2 = findViewById(R.id.btn_contact_to_contact2);
        btnToLoginPage = findViewById(R.id.btn_contact_to_login);
        btnToRegisterPage = findViewById(R.id.btn_contact_to_register);

        //בדיקה אם המשתמש מחובר
        boolean isLoggedIn = SharedPreferencesUtil.isUserLoggedIn(ContactActivity.this);

        if (isLoggedIn) {
            //הופך את כפתורי המשתמש המחובר ל-VISIBLE
            btnToMain.setVisibility(View.VISIBLE);
            btnToContactPage1.setVisibility(View.VISIBLE);
            btnToDetailsAboutUser.setVisibility(View.VISIBLE);
            btnToExit.setVisibility(View.VISIBLE);
        }
        else {
            //הופך את כפתורי המשתמש הלא מחובר ל-VISIBLE
            btnToLanding.setVisibility(View.VISIBLE);
            btnToLoginPage.setVisibility(View.VISIBLE);
            btnToRegisterPage.setVisibility(View.VISIBLE);
            btnToContactPage2.setVisibility(View.VISIBLE);
        }

        btnToMain.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, MainActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, DetailsAboutUserActivity.class)));
        btnToExit.setOnClickListener(view -> logout());

        btnToLanding.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, LandingActivity.class)));
        btnToLoginPage.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, LoginActivity.class)));
        btnToRegisterPage.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, RegisterActivity.class)));
    }
}