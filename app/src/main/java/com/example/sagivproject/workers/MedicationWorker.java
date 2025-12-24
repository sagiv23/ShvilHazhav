package com.example.sagivproject.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.NotificationHelper;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MedicationWorker extends Worker {
    private static final String TAG = "MedicationWorker";

    public MedicationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Log.d(TAG, "ה-Worker התחיל לפעול ברקע...");

        // בדיקה שהמשתמש מחובר
        if (!SharedPreferencesUtil.isUserLoggedIn(context)) {
            Log.d(TAG, "משתמש לא מחובר - מבטל התראה.");
            return Result.success();
        }

        String userId = SharedPreferencesUtil.getUserId(context);

        // יצירת "מחסום" לסנכרון מול Firebase
        final CountDownLatch latch = new CountDownLatch(1);

        DatabaseService.getInstance().getUserMedicationList(userId, new DatabaseService.DatabaseCallback<List<Medication>>() {
            @Override
            public void onCompleted(List<Medication> medications) {
                Log.d(TAG, "נתונים התקבלו מ-Firebase.");
                processMedications(context, userId, medications);
                latch.countDown(); // משחרר את המחסום
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "שגיאה במשיכת נתונים: " + e.getMessage());
                latch.countDown(); // משחרר כדי לא לתקוע את המערכת
            }
        });

        try {
            // מחכה עד דקה שהנתונים יחזרו מ-Firebase
            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return Result.success();
    }

    private void processMedications(Context context, String userId, List<Medication> medications) {
        // שלב 1: בדיקה אם יש תרופות ברשימה (דרישת המשתמש)
        if (medications == null || medications.isEmpty()) {
            Log.d(TAG, "רשימת התרופות ריקה - לא תישלח התראה.");
            return;
        }

        int expiredCount = 0;
        Calendar todayCal = Calendar.getInstance();
        // איפוס שעות להשוואה נקייה
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date today = todayCal.getTime();

        for (Medication med : medications) {
            if (med.getDate() != null) {
                Calendar expiryLimit = Calendar.getInstance();
                expiryLimit.setTime(med.getDate());
                // פג תוקף = יום אחרי התאריך שהוזן
                expiryLimit.add(Calendar.DAY_OF_YEAR, 1);

                if (today.after(expiryLimit.getTime()) || today.equals(expiryLimit.getTime())) {
                    expiredCount++;
                    DatabaseService.getInstance().deleteMedication(userId, med.getId(), null);
                    Log.d(TAG, "תרופה פגה תוקף נמחקה: " + med.getName());
                }
            }
        }

        // שלב 2: שליחת ההתראה המתאימה
        if (expiredCount > 0) {
            NotificationHelper.showNotification(context, "עדכון תרופות",
                    "ניקינו מהרשימה " + expiredCount + " תרופות שפג תוקפן.");
        } else {
            // התראה כללית - נשלחת רק כי הרשימה לא ריקה
            NotificationHelper.showNotification(context, "תזכורת תרופות",
                    "בוקר טוב! יש לך תרופות ברשימה שממתינות לנטילה.");
        }
    }
}