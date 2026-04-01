package com.example.sagivproject;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.sagivproject.services.notifications.DailyCheckWorker;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * The main {@link Application} class for the application.
 * <p>
 * This class is the entry point of the application and is responsible for:
 * <ul>
 *     <li>Initializing Hilt for dependency injection (via {@code @HiltAndroidApp}).</li>
 *     <li>Configuring {@link WorkManager} to support Hilt worker injection.</li>
 *     <li>Applying the user's preferred theme (Dark/Light mode) on startup.</li>
 *     <li>Scheduling periodic background tasks for daily health and system checks.</li>
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

        setupDailyChecks();
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

    /**
     * Schedules a unique periodic work request that runs once every 24 hours.
     * <p>
     * The task is scheduled to run at 9:00 AM daily. If the current time is past 9:00 AM,
     * the initial run is deferred to the following morning. The task requires an active
     * network connection to perform external database checks (e.g., birthdays).
     * </p>
     */
    private void setupDailyChecks() {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        dueDate.set(Calendar.HOUR_OF_DAY, 9);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        PeriodicWorkRequest dailyRequest =
                new PeriodicWorkRequest.Builder(DailyCheckWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyChecksWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
        );
    }
}