package com.example.sagivproject.screens;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class LandingActivity extends BaseActivity {
    Button btnToContact, btnToLogin, btnToRegister, btnNavToContact, btnNavToLogin, btnNavToRegister;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                checkAndNavigate();
            });

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

        startPermissionsFlow();

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

    private void startPermissionsFlow() {
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
        for (String p : permissions) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, p)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            checkAndNavigate();
        } else {
            requestPermissionLauncher.launch(permissions.toArray(new String[0]));
        }
    }

    private void checkAndNavigate() {
        if (SharedPreferencesUtil.isUserLoggedIn(this)) {
            User current = SharedPreferencesUtil.getUser(this);
            if (current != null) {
                databaseService.getUser(current.getUid(), new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        if (user != null) {
                            Intent intent;
                            if (user.getIsAdmin()) {
                                intent = new Intent(LandingActivity.this, AdminPageActivity.class);
                            } else {
                                intent = new Intent(LandingActivity.this, MainActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                    }
                });
            }
        }
    }
}