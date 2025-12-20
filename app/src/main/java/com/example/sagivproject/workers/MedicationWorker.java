package com.example.sagivproject.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.utils.NotificationHelper;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MedicationWorker extends Worker {
    public MedicationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        //שליפת רשימת התרופות מהזיכרון המקומי
        HashMap<String, Medication> medicationsMap = SharedPreferencesUtil.getMedications(context);

        //בדיקה אם קיימות תרופות ברשימה
        if (medicationsMap == null || medicationsMap.isEmpty()) {
            return Result.success(); //אין תרופות, אין צורך בהתראה
        }

        //לוגיקה לבדיקת תוקף ושליחת התראה
        int expiredCount = 0;

        //קבלת התאריך של היום ואיפוס השעה לחצות
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date today = todayCal.getTime();

        for (Medication med : medicationsMap.values()) {
            if (med.getDate() != null) {
                //יצירת לוח שנה עבור תאריך התרופה
                Calendar expiryCal = Calendar.getInstance();
                expiryCal.setTime(med.getDate());

                //הוספת יום אחד - התרופה פגה רק ביום למחרת
                expiryCal.add(Calendar.DAY_OF_YEAR, 1);

                //איפוס השעה של תאריך התרופה לחצות (ליתר ביטחון)
                expiryCal.set(Calendar.HOUR_OF_DAY, 0);
                expiryCal.set(Calendar.MINUTE, 0);
                expiryCal.set(Calendar.SECOND, 0);
                expiryCal.set(Calendar.MILLISECOND, 0);

                //אם "היום" הוא אחרי (או שווה) ליום שאחרי התפוגה - היא פגה
                if (today.after(expiryCal.getTime()) || today.equals(expiryCal.getTime())) {
                    expiredCount++;
                }
            }
        }

        //שליחת התראות (רק אם יש תרופות במערכת)
        if (expiredCount > 0) {
            NotificationHelper.showNotification(context, "תרופות פגות תוקף", "יש לך " + expiredCount + " תרופות שפג תוקפן!");
        }

        NotificationHelper.showNotification(context, "תזכורת יומית", "אל תשכח לבדוק את רשימת התרופות היומית שלך.");

        return Result.success();
    }
}