package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The initial splash fragment of the application.
 * <p>
 * This fragment is responsible for showing a branding screen for a short duration
 * and then navigating the user to the appropriate screen based on their
 * authentication state and role (e.g., Home for regular users, Admin dashboard for admins,
 * or Landing for unauthenticated users).
 * </p>
 */
@AndroidEntryPoint
public class SplashFragment extends BaseFragment {
    /**
     * The delay in milliseconds before navigating away from the splash screen.
     */
    private static final long SPLASH_DELAY = 3000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Start a delayed navigation on a background thread
        new Thread(() -> {
            try {
                Thread.sleep(SPLASH_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::navigateNext);
            }
        }).start();
    }

    /**
     * Determines the next screen to navigate to based on user authentication state.
     * <p>
     * If a user is cached, it attempts to fetch the latest user data from the database
     * to ensure the profile and role are up-to-date.
     * </p>
     */
    private void navigateNext() {
        User cachedUser = sharedPreferencesUtil.getUser();
        if (cachedUser == null || sharedPreferencesUtil.isUserNotLoggedIn()) {
            navigateTo(R.id.action_splashFragment_to_landingFragment);
            return;
        }

        databaseService.getUserService().getUser(cachedUser.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    sharedPreferencesUtil.saveUser(user);
                    if (user.isAdmin()) {
                        navigateTo(R.id.action_splashFragment_to_adminPageFragment);
                    } else {
                        navigateTo(R.id.action_splashFragment_to_mainFragment);
                    }
                } else {
                    // User exists in cache but not in DB, sign out
                    sharedPreferencesUtil.signOutUser();
                    navigateTo(R.id.action_splashFragment_to_landingFragment);
                }
            }

            @Override
            public void onFailed(Exception e) {
                // On error, revert to sign-in
                sharedPreferencesUtil.signOutUser();
                navigateTo(R.id.action_splashFragment_to_landingFragment);
            }
        });
    }
}
