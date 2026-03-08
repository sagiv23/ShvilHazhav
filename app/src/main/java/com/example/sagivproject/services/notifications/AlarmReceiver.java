package com.example.sagivproject.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link BroadcastReceiver} that listens for scheduled medication alarms and daily birthday checks.
 */
@AndroidEntryPoint
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Inject
    NotificationService notificationService;

    @Inject
    AlarmScheduler alarmScheduler;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_BIRTHDAY_CHECK".equals(intent.getAction())) {
            handleBirthdayCheck();
            alarmScheduler.scheduleBirthdayAlarm(); // Reschedule for tomorrow
            return;
        }

        String medicationId = intent.getStringExtra("medication_id");
        String medicationName = intent.getStringExtra("medication_name");
        String hourStr = intent.getStringExtra("hour_str");
        int notificationId = intent.getIntExtra("notification_id", 0);

        User user = sharedPreferencesUtil.getUser();
        if (user == null || user.getMedications() == null || !user.getMedications().containsKey(medicationId)) {
            Log.d(TAG, "Medication " + medicationName + " no longer exists. Skipping.");
            return;
        }

        // Check if already taken today for this scheduled time
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyStats stats = user.getDailyStats().get(today);
        if (stats != null && stats.getMedicationUsageLogs() != null) {
            for (MedicationUsage usage : stats.getMedicationUsageLogs()) {
                if (hourStr != null && usage.getId().equals(medicationId) &&
                        hourStr.equals(usage.getScheduledTime()) &&
                        usage.getStatus() == MedicationStatus.TAKEN) {
                    Log.d(TAG, "Medication " + medicationName + " already taken for " + hourStr + ". Skipping notification.");
                    // Still need to schedule for tomorrow
                    Medication medication = user.getMedications().get(medicationId);
                    if (medication != null) {
                        alarmScheduler.scheduleSpecificTime(medication, hourStr, true);
                    }
                    return;
                }
            }
        }

        Log.d(TAG, "Showing notification for: " + medicationName + " at " + hourStr);
        notificationService.showMedicationNotification(medicationName, notificationId);

        Medication medication = user.getMedications().get(medicationId);
        if (medication != null) {
            if (hourStr != null) {
                alarmScheduler.scheduleSpecificTime(medication, hourStr, true);
            }
        }
    }

    private void handleBirthdayCheck() {
        User user = sharedPreferencesUtil.getUser();
        if (user == null || user.getBirthDateMillis() <= 0) return;

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
