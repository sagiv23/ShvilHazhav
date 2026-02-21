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
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

/**
 * A {@link Worker} that runs periodically to check if it is the user's birthday.
 * <p>
 * This background worker fetches the logged-in user's data, compares their birthdate
 * with the current date, and if they match, it triggers a birthday notification using
 * the {@link NotificationService}.
 * </p>
 */
@HiltWorker
public class BirthdayWorker extends Worker {
    protected final IDatabaseService databaseService;
    protected final NotificationService notificationService;
    protected final SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Constructs a new BirthdayWorker.
     *
     * @param context               The application context.
     * @param workerParams          Parameters to configure the worker.
     * @param databaseService       The database service for fetching user data.
     * @param notificationService   The service for showing notifications.
     * @param sharedPreferencesUtil Utility for accessing shared preferences.
     */
    @AssistedInject
    public BirthdayWorker(
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
     * The main work method. Fetches user data and checks for a birthday.
     *
     * @return The result of the work, indicating success, failure, or retry.
     */
    @NonNull
    @Override
    public Result doWork() {
        if (!sharedPreferencesUtil.isUserLoggedIn()) return Result.success();

        String userId = sharedPreferencesUtil.getUserId();
        final CountDownLatch latch = new CountDownLatch(1); // Used to wait for async database call

        databaseService.getUserService().getUser(Objects.requireNonNull(userId), new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    checkAndNotifyBirthday(user);
                }
                latch.countDown();
            }

            @Override
            public void onFailed(Exception e) {
                latch.countDown(); // Still count down on failure to unblock
            }
        });

        boolean completed;
        try {
            // Wait for the database callback to finish, with a timeout.
            completed = latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            return Result.retry();
        }

        if (!completed) {
            // If timeout was reached, retry the work later.
            return Result.retry();
        }

        return Result.success();
    }

    /**
     * Checks if the user's birthday is today and triggers a notification if it is.
     *
     * @param user The user to check.
     */
    private void checkAndNotifyBirthday(User user) {
        if (user.getBirthDateMillis() <= 0) return;

        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTimeInMillis(user.getBirthDateMillis());

        if (today.get(Calendar.DAY_OF_MONTH) == birthDate.get(Calendar.DAY_OF_MONTH) && today.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH)) {
            notificationService.showBirthdayNotification(user.getFirstName());
        }
    }
}
