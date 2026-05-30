package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseCallback;
import com.example.sagivproject.services.IUserService;

import javax.inject.Inject;

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
    /**
     * Minimum duration in milliseconds to display the splash screen.
     */
    private static final long SPLASH_DELAY = 3000;
    /**
     * UI thread handler for scheduling the transition.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());
    @Inject
    protected IUserService userService;
    /**
     * Reusable runnable for performing the navigation logic.
     */
    private final Runnable navigateRunnable = this::navigateNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_splash, R.id.splashPage);

        startAnimations();
        handler.postDelayed(navigateRunnable, SPLASH_DELAY);
    }

    private void startAnimations() {
        android.view.View logo = findViewById(R.id.img_Splash_logo);
        android.view.View title = findViewById(R.id.tv_Splash_Title);
        android.view.View subtitle = findViewById(R.id.tv_Splash_Subtitle);

        if (logo != null) {
            logo.setAlpha(0f);
            logo.setScaleX(0.9f);
            logo.setScaleY(0.9f);
            logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1500).start();
        }

        if (title != null) {
            title.setAlpha(0f);
            title.animate().alpha(1f).setDuration(1000).setStartDelay(800).start();
        }

        if (subtitle != null) {
            subtitle.setAlpha(0f);
            subtitle.animate().alpha(1f).setDuration(1200).setStartDelay(1200).start();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
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

        userService.getUser(cachedUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    sharedPreferencesUtil.saveUser(user);
                    navigateToUserHome(user);
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

    /**
     * Navigates to the landing screen and passes a flag to trigger the entry animation.
     */
    private void navigateToLandingWithAnimation() {
        onNavigate(new Intent(this, LandingActivity.class)
                .putExtra("shouldAnimate", true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}