package com.example.sagivproject.bases;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.sagivproject.utils.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * The main {@link Application} class for the application.
 * <p>
 * This class is the entry point of the application and is responsible for:
 * <ul>
 * <li>Initializing Hilt for dependency injection (via {@code @HiltAndroidApp}).</li>
 * <li>Applying the user's preferred theme (Dark/Light mode) on startup.</li>
 * </ul>
 * </p>
 */
@HiltAndroidApp
public class MainApplication extends Application {

    /**
     * Utility for accessing persistent application preferences.
     */
    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    public void onCreate() {
        super.onCreate();

        boolean isDarkMode = sharedPreferencesUtil.isDarkMode();
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
