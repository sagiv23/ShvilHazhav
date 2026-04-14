package com.example.sagivproject.bases;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.example.sagivproject.utils.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * The main {@link Application} class for the application.
 * <p>
 * This class is the entry point of the application and is responsible for:
 * <ul>
 * <li>Initializing Hilt for dependency injection (via {@code @HiltAndroidApp}).</li>
 * <li>Configuring {@link WorkManager} to support Hilt worker injection.</li>
 * <li>Applying the user's preferred theme (Dark/Light mode) on startup.</li>
 * </ul>
 * </p>
 */
@HiltAndroidApp
public class MainApplication extends Application implements Configuration.Provider {

    /**
     * Factory for creating Hilt-injected WorkManager workers.
     */
    @Inject
    HiltWorkerFactory workerFactory;

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

    /**
     * Provides the WorkManager configuration to the system.
     * This implementation enables Hilt injection for {@link androidx.work.ListenableWorker} subclasses.
     *
     * @return A custom {@link Configuration} instance containing the Hilt worker factory.
     */
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }

}