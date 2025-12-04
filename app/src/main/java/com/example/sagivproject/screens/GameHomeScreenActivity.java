package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;

public class GameHomeScreenActivity extends AppCompatActivity {
    Button btnToMain, btnToContact, btnToDetailsAboutUser, btnFindEnemy, btnCancelFindEnemy, btnToExit;
    TextView TVStatusOfFindingEnemy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_home_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameHomeScreenPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkUserPage(this);

        btnToMain = findViewById(R.id.btn_GameHomeScreen_to_main);
        btnToContact = findViewById(R.id.btn_GameHomeScreen_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_GameHomeScreen_to_DetailsAboutUser);
        btnFindEnemy = findViewById(R.id.btn_GameHomeScreen_find_enemy);
        btnCancelFindEnemy = findViewById(R.id.btn_GameHomeScreen_cancel_find_enemy);
        btnToExit = findViewById(R.id.btn_GameHomeScreen_to_exit);
        TVStatusOfFindingEnemy = findViewById(R.id.tv_GameHomeScreen_status_of_finding_enemy);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(GameHomeScreenActivity.this, DetailsAboutUserActivity.class)));
        //btnFindEnemy.setOnClickListener(view -> startSearching());
        //btnCancelFindEnemy.setOnClickListener(view -> cancelSearching());
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(GameHomeScreenActivity.this));

        //loadWins();
    }
}