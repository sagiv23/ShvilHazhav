package com.example.sagivproject.services.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

/**
 * A {@link Worker} that runs periodically to perform daily background synchronization and checks.
 * <p>
 * This worker is managed by {@link androidx.work.WorkManager} and is responsible for
 * tasks that need to occur once a day, such as checking if it's the user's birthday
 * and triggering a celebratory notification. It uses a {@link CountDownLatch} to
 * bridge the asynchronous database call with the synchronous {@link #doWork()} execution.
 * </p>
 */
@HiltWorker
public class DailyCheckWorker extends Worker {
    /**
     * Central database service for fetching user data.
     */
    protected final IDatabaseService databaseService;

    /**
     * Service for triggering local notifications.
     */
    protected final NotificationService notificationService;

    /**
     * Utility for checking user session state.
     */
    protected final SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Constructs a new DailyCheckWorker.
     *
     * @param context               The application context.
     * @param workerParams          Parameters for the worker.
     * @param databaseService       The database service provider.
     * @param notificationService   The notification manager.
     * @param sharedPreferencesUtil The preference utility.
     */
    @AssistedInject
    public DailyCheckWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            IDatabaseService databaseService,
            NotificationService notificationService,
            SharedPreferencesUtil sharedPreferencesUtil
    ) {
        super(context, workerParams);
        this.databaseService = databaseService;
        this.notificationService = notificationService;
        this.sharedPreferencesUtil = sharedPreferencesUtil;
    }

    /**
     * The main execution logic for the background task.
     * <p>
     * Verifies user session, fetches the latest profile from Firebase, and triggers
     * specific daily logic like birthday notifications.
     * </p>
     *
     * @return {@link androidx.work.ListenableWorker.Result#success()} if finished,
     * or {@link androidx.work.ListenableWorker.Result#retry()} on transient failures.
     */
    @NonNull
    @Override
    public Result doWork() {
        if (sharedPreferencesUtil.isUserNotLoggedIn()) return Result.success();

        String userId = sharedPreferencesUtil.getUserId();
        if (userId == null) return Result.success();

        final CountDownLatch latch = new CountDownLatch(1);

        // Fetch the latest user data from the database to perform checks
        databaseService.getUserService().getUser(userId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    checkAndNotifyBirthday(user);
                }
                latch.countDown();
            }

            @Override
            public void onFailed(Exception e) {
                latch.countDown();
            }
        });

        try {
            // Synchronously wait for the database response (timeout of 1 minute)
            if (!latch.await(1, TimeUnit.MINUTES)) {
                return Result.retry();
            }
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return Result.success();
    }

    /**
     * Checks if today matches the user's birth month and day.
     * Triggers a celebratory notification if true.
     *
     * @param user The {@link User} object to validate.
     */
    private void checkAndNotifyBirthday(User user) {
        if (user.getBirthDateMillis() <= 0) return;

        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTimeInMillis(user.getBirthDateMillis());

        if (today.get(Calendar.DAY_OF_MONTH) == birthDate.get(Calendar.DAY_OF_MONTH) &&
                today.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH)) {

            int notificationId = UUID.randomUUID().hashCode();
            notificationService.showBirthdayNotification(user.getFirstName(), notificationId);
        }
    }
}
