package com.example.sagivproject.bases;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.dialogs.LogoutDialog;
import com.example.sagivproject.services.AuthService;
import com.example.sagivproject.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
    protected DatabaseService databaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //קיצור ל - ()DatabaseService.getInstance()
        databaseService = DatabaseService.getInstance();

        checkPermissionsOncePerRun();
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

    //הרשאות
    protected void checkPermissionsOncePerRun() {
        if (permissionsAlreadyRequested()) {
            return;
        }

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        boolean allGranted = true;

        for (int i = 0; i < permissions.size(); i++) {
            String p = permissions.get(i);
            boolean granted =
                    ContextCompat.checkSelfPermission(this, p)
                            == PackageManager.PERMISSION_GRANTED;

            allGranted = allGranted && granted;
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    permissions.toArray(new String[0]),
                    1001
            );
        }

        markPermissionsRequested();
    }

    private boolean permissionsAlreadyRequested() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("permissions_requested_this_run", false);
    }

    private void markPermissionsRequested() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("permissions_requested_this_run", true)
                .apply();
    }
}