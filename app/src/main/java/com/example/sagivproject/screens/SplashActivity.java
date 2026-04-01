package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * The initial entry activity of the application that manages the splash screen experience.
 * <p>
 * This activity handles the transition from app launch to the initial functional screen.
 * It performs an authentication check against the database to determine if a cached user session
 * is still valid. Based on the result, it redirects the user to the Admin Dashboard,
 * the User Main Dashboard, or the Landing screen for unauthenticated users.
 * </p>
 */
@AndroidEntryPoint
public class SplashActivity extends BaseActivity {
    /** The duration for which the splash screen is displayed, in milliseconds. */
    private static final long SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Thread(() -> {
            try {
                Thread.sleep(SPLASH_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            runOnUiThread(this::navigateNext);
        }).start();
    }

    /**
     * Determines the next destination activity based on the current authentication state.
     * <p>
     * If a cached user is found, it verifies their status with the database.
     * If authentication fails or no user is found, it redirects to the landing screen with an animation.
     * </p>
     */
    private void navigateNext() {
        User cachedUser = sharedPreferencesUtil.getUser();
        if (cachedUser == null || sharedPreferencesUtil.isUserNotLoggedIn()) {
            navigateToLandingWithAnimation();
            return;
        }

        databaseService.getUserService().getUser(cachedUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    sharedPreferencesUtil.saveUser(user);
                    Intent intent;
                    if (user.isAdmin()) {
                        intent = new Intent(SplashActivity.this, AdminPageActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    }
                    startActivity(intent);
                } else {
                    sharedPreferencesUtil.signOutUser();
                    navigateToLandingWithAnimation();
                }
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                sharedPreferencesUtil.signOutUser();
                navigateToLandingWithAnimation();
                finish();
            }
        });
    }

    /** Navigates to the landing screen and passes a flag to trigger the entry animation. */
    private void navigateToLandingWithAnimation() {
        Intent intent = new Intent(this, LandingActivity.class);
        intent.putExtra("shouldAnimate", true);
        startActivity(intent);
    }
}