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

import com.example.sagivproject.services.notifications.BirthdayWorker;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * The main {@link Application} class for the application.
 * <p>
 * This class is the entry point of the application. It initializes Hilt for dependency injection,
 * sets the initial dark mode state, and schedules the daily birthday notification worker.
 * </p>
 */
@HiltAndroidApp
public class MainApplication extends Application implements Configuration.Provider {
    @Inject
    HiltWorkerFactory workerFactory;
    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        // Set the app's theme based on the saved preference.
        boolean isDarkMode = sharedPreferencesUtil.isDarkMode();
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setupBirthdayNotification();
    }

    /**
     * Provides a custom WorkManager configuration that uses the Hilt worker factory.
     *
     * @return The WorkManager configuration.
     */
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }

    /**
     * Schedules a periodic background worker to check for the user's birthday each day.
     */
    private void setupBirthdayNotification() {
        // Calculate the initial delay to the next 9:00 AM.
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        dueDate.set(Calendar.HOUR_OF_DAY, 9);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        // Create a periodic work request.
        PeriodicWorkRequest birthdayRequest =
                new PeriodicWorkRequest.Builder(BirthdayWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                        .build();

        // Enqueue the unique periodic work.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "BirthdayDailyWork",
                ExistingPeriodicWorkPolicy.KEEP,
                birthdayRequest
        );
    }
}
