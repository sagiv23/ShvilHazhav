package com.example.sagivproject.bases;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.dialogs.LogoutDialog;
import com.example.sagivproject.services.AuthService;
import com.example.sagivproject.services.DatabaseService;

public abstract class BaseActivity extends AppCompatActivity {
    protected DatabaseService databaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //קיצור ל - ()DatabaseService.getInstance()
        databaseService = DatabaseService.getInstance();
    }

    //התנתקות
    protected void logout() {
        AuthService authService = new AuthService(this);

        new LogoutDialog(this, () -> {
            String email = authService.logout();
            Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("userEmail", email);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).show();
    }
}