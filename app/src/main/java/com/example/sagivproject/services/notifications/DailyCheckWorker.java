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
 * A {@link Worker} that runs periodically to perform daily background checks.
 * <p>
 * This worker is managed by {@link androidx.work.WorkManager} and is responsible for
 * tasks that need to occur once a day, such as checking if it's the user's birthday
 * and triggering a celebratory notification. It uses a {@link CountDownLatch} to
 * handle the asynchronous database call within the synchronous {@code doWork} method.
 * </p>
 */
@HiltWorker
public class DailyCheckWorker extends Worker {
    protected final IDatabaseService databaseService;
    protected final NotificationService notificationService;
    protected final SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Constructs a new DailyCheckWorker.
     *
     * @param context               The application context.
     * @param workerParams          Parameters for the worker.
     * @param databaseService       The database service for fetching user data.
     * @param notificationService   The notification service for triggering alerts.
     * @param sharedPreferencesUtil The utility for checking user login state.
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

    @NonNull
    @Override
    public Result doWork() {
        if (sharedPreferencesUtil.isUserNotLoggedIn()) return Result.success();

        String userId = sharedPreferencesUtil.getUserId();
        if (userId == null) return Result.success();

        final CountDownLatch latch = new CountDownLatch(1);

        // Fetch the latest user data to check the birthday
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
            // Wait for the asynchronous database call to complete (up to 1 minute)
            if (!latch.await(1, TimeUnit.MINUTES)) {
                return Result.retry();
            }
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return Result.success();
    }

    /**
     * Checks if today is the user's birthday and triggers a notification if so.
     *
     * @param user The user object to check.
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
