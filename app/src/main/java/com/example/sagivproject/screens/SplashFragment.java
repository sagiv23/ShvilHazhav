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
 */
@AndroidEntryPoint
public class SplashFragment extends BaseFragment {
    private static final long SPLASH_DELAY = 3000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                    sharedPreferencesUtil.signOutUser();
                    navigateTo(R.id.action_splashFragment_to_landingFragment);
                }
            }

            @Override
            public void onFailed(Exception e) {
                sharedPreferencesUtil.signOutUser();
                navigateTo(R.id.action_splashFragment_to_landingFragment);
            }
        });
    }
}
