package com.example.sagivproject.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

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
        //אין תרופות ברשימה
        if (medications == null || medications.isEmpty()) return;

        int expiredCount = 0;

        //קבלת התאריך של היום (מאופס לשעה 00:00)
        Calendar calToday = Calendar.getInstance();
        resetTime(calToday);
        Date today = calToday.getTime();

        for (Medication med : medications) {
            if (med.getDate() != null) {
                Calendar expiryLimit = Calendar.getInstance();
                expiryLimit.setTime(med.getDate());
                //הגדרת פג תוקף: התאריך שהוזן + יום אחד
                expiryLimit.add(Calendar.DAY_OF_YEAR, 1);
                resetTime(expiryLimit);

                //אם היום הוא אחרי (או שווה) לתאריך היעד (הזנת המשתמש + 1)
                if (today.after(expiryLimit.getTime()) || today.equals(expiryLimit.getTime())) {
                    expiredCount++;
                    //מחיקה מהדאטה בייס
                    databaseService.deleteMedication(userId, med.getId(), null);
                }
            }
        }

        NotificationService notificationService = new NotificationService(context);

        //שליחת התראות לפי המצב
        if (expiredCount > 0) {
            //התראה על מחיקת תרופות שפגו
            notificationService.show(
                    "עדכון רשימת תרופות",
                    "מחקנו " + expiredCount + " תרופות שפג תוקפן מהרשימה שלך."
            );
        } else {
            //התראה כללית אם יש תרופות ברשימה ולא נמחקו חדשות
            notificationService.show(
                    "תזכורת יומית",
                    "יש לך תרופות ברשימה שממתינות לנטילה."
            );
        }
    }

    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}