package com.example.sagivproject.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.example.sagivproject.bases.BaseWorkerActivity;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.services.NotificationService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MedicationWorker extends BaseWorkerActivity {
    private static final String TAG = "MedicationWorker";

    public MedicationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (!SharedPreferencesUtil.isUserLoggedIn(context)) {
            return Result.success();
        }

        String userId = SharedPreferencesUtil.getUserId(context);
        final CountDownLatch latch = new CountDownLatch(1);

        databaseService.getUserMedicationList(userId, new DatabaseService.DatabaseCallback<List<Medication>>() {
            @Override
            public void onCompleted(List<Medication> medications) {
                processMedications(context, userId, medications);
                latch.countDown();
            }

            @Override
            public void onFailed(Exception e) {
                latch.countDown();
            }
        });

        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return Result.success();
    }

    private void processMedications(Context context, String userId, List<Medication> medications) {
        if (medications == null || medications.isEmpty()) return;

        int expiredCount = 0;
        int remainingCount = 0;

        Date today = new Date();

        for (Medication med : medications) {
            if (med.getDate() != null) {
                Calendar expiryLimit = Calendar.getInstance();
                expiryLimit.setTime(med.getDate());
                expiryLimit.add(Calendar.DAY_OF_YEAR, 1);

                if (today.after(expiryLimit.getTime())) {
                    // תרופה פגת תוקף
                    expiredCount++;
                    databaseService.deleteMedication(userId, med.getId(), null);
                } else {
                    // תרופה תקינה שנותרה ברשימה
                    remainingCount++;
                }
            } else {
                // תרופה ללא תאריך נחשבת כתרופה שנותרה
                remainingCount++;
            }
        }

        NotificationService notificationService = new NotificationService(context);

        // התראה 1: רק אם יש תרופות שנמחקו
        if (expiredCount > 0) {
            notificationService.show(
                    "עדכון רשימת תרופות",
                    "מחקנו " + expiredCount + " תרופות שפג תוקפן מהרשימה שלך."
            );
        }

        // התראה 2: רק אם נשארו תרופות לנטילה (אחרי המחיקה)
        if (remainingCount > 0) {
            notificationService.show(
                    "תזכורת יומית",
                    "יש לך " + remainingCount + " תרופות ברשימה שממתינות לנטילה."
            );
        }
    }
}