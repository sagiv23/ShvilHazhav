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
 * This class is annotated with {@code @HiltAndroidApp} to trigger Hilt's code generation,
 * including a base class for the application that serves as the application-level dependency container.
 * It also implements {@link Configuration.Provider} to provide a custom configuration for WorkManager,
 * enabling Hilt injection into {@link androidx.work.ListenableWorker}s.
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

        // Apply the saved theme preference (Dark Mode / Light Mode)
        boolean isDarkMode = sharedPreferencesUtil.isDarkMode();
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Schedule periodic background tasks
        setupDailyChecks();
    }

    /**
     * Provides the WorkManager configuration, allowing Hilt to inject dependencies into Workers.
     *
     * @return The WorkManager {@link Configuration}.
     */
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }

    /**
     * Schedules a periodic work request that runs once a day (at 9:00 AM) to perform daily checks.
     * <p>
     * If the current time is past 9:00 AM, the first run is scheduled for the next day.
     * The task requires a connected network to execute.
     * </p>
     */
    private void setupDailyChecks() {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        dueDate.set(Calendar.HOUR_OF_DAY, 9);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);

        // If 9 AM has already passed today, schedule for tomorrow
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

        // Enqueue the unique work, keeping existing work if it's already scheduled
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyChecksWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
        );
    }
}
