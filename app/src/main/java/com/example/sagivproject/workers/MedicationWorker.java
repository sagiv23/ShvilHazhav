package com.example.sagivproject.workers;

import android.content.Context;
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

public class MedicationWorker extends Worker {
    public MedicationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        //בדיקה אם המשתמש מחובר - אם לא, לא עושים כלום
        if (!SharedPreferencesUtil.isUserLoggedIn(context)) {
            return Result.success();
        }

        String userId = SharedPreferencesUtil.getUserId(context);

        //שליפת התרופות מה-Firebase כדי לבצע מחיקה אמיתית ברקע
        DatabaseService.getInstance().getUserMedicationList(userId, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Medication> medications) {
                if (medications == null || medications.isEmpty()) return;

                int expiredCount = 0;
                Calendar todayCal = Calendar.getInstance();
                //איפוס שעה לחצות כדי להשוות רק תאריכים
                todayCal.set(Calendar.HOUR_OF_DAY, 0);
                todayCal.set(Calendar.MINUTE, 0);
                todayCal.set(Calendar.SECOND, 0);
                todayCal.set(Calendar.MILLISECOND, 0);
                Date today = todayCal.getTime();

                for (Medication med : medications) {
                    if (med.getDate() != null) {
                        Calendar expiryCal = Calendar.getInstance();
                        expiryCal.setTime(med.getDate());

                        //תרופה פגה ביום שאחרי התאריך הרשום
                        expiryCal.add(Calendar.DAY_OF_YEAR, 1);

                        //אם היום הוא יום אחרי פקיעת התוקף
                        if (today.after(expiryCal.getTime()) || today.equals(expiryCal.getTime())) {
                            expiredCount++;
                            //מחיקה מה-Firebase בזמן אמת
                            DatabaseService.getInstance().deleteMedication(userId, med.getId(), null);
                        }
                    }
                }

                //התראה על מחיקת תרופות פגות תוקף
                if (expiredCount > 0) {
                    NotificationHelper.showNotification(context, "עדכון רשימת תרופות",
                            expiredCount + " תרופות שפג תוקפן נמחקו מהמערכת.");
                }

                //התראה יומית על נטילת תרופות (רק אם נשארו תרופות ברשימה)
                if (medications.size() > expiredCount) {
                    NotificationHelper.showNotification(context, "תזכורת נטילת תרופות",
                            "בוקר טוב! יש לך תרופות ליטול היום. בדוק את הרשימה באפליקציה.");
                }
            }

            @Override
            public void onFailed(Exception e) {
                //שגיאה בתקשורת עם Firebase
            }
        });

        return Result.success();
    }
}