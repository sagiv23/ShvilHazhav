package com.example.sagivproject.screens.base;

import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.dialogs.LogoutDialog;
import com.example.sagivproject.services.AuthService;

public abstract class BaseActivity extends AppCompatActivity {
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
