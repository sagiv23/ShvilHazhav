package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The initial splash screen of the application.
 * <p>
 * This activity is displayed for a short duration when the app starts. It then checks
 * the user's login status and navigates to the appropriate screen (Landing, Main, or Admin).
 * </p>
 */
@AndroidEntryPoint
public class SplashActivity extends BaseActivity {
    private static final long SPLASH_DELAY = 3000;

    /**
     * Initializes the activity and schedules the navigation to the next screen after a delay.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, SPLASH_DELAY);
    }

    /**
     * Determines the next screen to navigate to based on the user's login status.
     * It checks for a cached user and verifies their data with the server before navigating.
     */
    private void navigateNext() {
        User cachedUser = sharedPreferencesUtil.getUser();
        if (cachedUser == null || !sharedPreferencesUtil.isUserLoggedIn()) {
            goTo(LandingActivity.class);
            return;
        }

        databaseService.getUserService().getUser(cachedUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    sharedPreferencesUtil.saveUser(user);
                    goTo(user.isAdmin() ? AdminPageActivity.class : MainActivity.class);
                } else {
                    // User might have been deleted from the server
                    sharedPreferencesUtil.signOutUser();
                    goTo(LandingActivity.class);
                }
            }

            @Override
            public void onFailed(Exception e) {
                // In case of network error, etc., log the user out to be safe
                sharedPreferencesUtil.signOutUser();
                goTo(LandingActivity.class);
            }
        });
    }

    /**
     * Navigates to a target activity and clears the activity stack.
     *
     * @param target The class of the activity to navigate to.
     */
    private void goTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
