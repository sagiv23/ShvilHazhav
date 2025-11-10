package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.models.LogoutHelper;
import com.example.sagivproject.R;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;

public class ContactActivity extends AppCompatActivity {
    //כפתורים למשתמש מחובר
    private Button btnToHomePage, btnToDetailsAboutUser, btnToExit, btnToContactPage1;
    //כפתורים למשתמש לא מחובר
    private Button btnToMainPage, btnToLoginPage, btnToRegisterPage, btnToContactPage2;

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
        btnToHomePage = findViewById(R.id.btnContactPageToHomePage);
        btnToContactPage1 = findViewById(R.id.btnContactPageToContactPage1);
        btnToDetailsAboutUser = findViewById(R.id.btnContactPageToDetailsAboutUserPage);
        btnToExit = findViewById(R.id.btnContactPageToExit);

        //משתמש לא מחובר
        btnToMainPage = findViewById(R.id.btnContactPageToMainPage);
        btnToLoginPage = findViewById(R.id.btnContactPageToLoginPage);
        btnToRegisterPage = findViewById(R.id.btnContactPageToRegisterPage);
        btnToContactPage2 = findViewById(R.id.btnContactPageToContactPage2);

        //בדיקה אם המשתמש מחובר
        boolean isLoggedIn = (SharedPreferencesUtil.getUser(ContactActivity.this) != null || FirebaseAuth.getInstance().getCurrentUser() != null);

        if (isLoggedIn) {
            //הופך את כפתורי המשתמש המחובר ל-VISIBLE
            btnToHomePage.setVisibility(View.VISIBLE);
            btnToContactPage1.setVisibility(View.VISIBLE);
            btnToDetailsAboutUser.setVisibility(View.VISIBLE);
            btnToExit.setVisibility(View.VISIBLE);
        }
        else {
            //הופך את כפתורי המשתמש הלא מחובר ל-VISIBLE
            btnToMainPage.setVisibility(View.VISIBLE);
            btnToLoginPage.setVisibility(View.VISIBLE);
            btnToRegisterPage.setVisibility(View.VISIBLE);
            btnToContactPage2.setVisibility(View.VISIBLE);
        }

        btnToHomePage.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, HomePageActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, DetailsAboutUserActivity.class)));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(ContactActivity.this));

        btnToMainPage.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, MainActivity.class)));
        btnToLoginPage.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, LoginActivity.class)));
        btnToRegisterPage.setOnClickListener(view -> startActivity(new Intent(ContactActivity.this, RegisterActivity.class)));
    }
}