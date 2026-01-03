package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

public class SplashActivity extends AppCompatActivity {
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Thread splashThread = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            } finally {
                if (SharedPreferencesUtil.isUserLoggedIn(this)) {
                    User current = SharedPreferencesUtil.getUser(this);
                    if (current != null) {
                        DatabaseService.getInstance().getUser(current.getUid(), new DatabaseService.DatabaseCallback<User>() {
                            @Override
                            public void onCompleted(User user) {
                                if (user != null) {
                                    SharedPreferencesUtil.saveUser(SplashActivity.this, user);
                                    if (user.getIsAdmin()) {
                                        intent = new Intent(SplashActivity.this, AdminPageActivity.class);
                                    } else {
                                        intent = new Intent(SplashActivity.this, MainActivity.class);
                                    }
                                }
                                else {
                                    SharedPreferencesUtil.signOutUser(SplashActivity.this);
                                    intent = new Intent(SplashActivity.this, LandingActivity.class);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                SharedPreferencesUtil.signOutUser(SplashActivity.this);
                                intent = new Intent(SplashActivity.this, LandingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
                    }
                }
                else {
                    intent = new Intent(SplashActivity.this, LandingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
        splashThread.start();
    }
}