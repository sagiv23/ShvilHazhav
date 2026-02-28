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
 * A {@link Worker} that runs periodically to perform daily checks, such as birthdays.
 */
@HiltWorker
public class DailyCheckWorker extends Worker {
    protected final IDatabaseService databaseService;
    protected final NotificationService notificationService;
    protected final SharedPreferencesUtil sharedPreferencesUtil;

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
        if (!sharedPreferencesUtil.isUserLoggedIn()) return Result.success();

        String userId = sharedPreferencesUtil.getUserId();
        if (userId == null) return Result.success();

        final CountDownLatch latch = new CountDownLatch(1);

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
            if (!latch.await(1, TimeUnit.MINUTES)) {
                return Result.retry();
            }
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return Result.success();
    }

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
