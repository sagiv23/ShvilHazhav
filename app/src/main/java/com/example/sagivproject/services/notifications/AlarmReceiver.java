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
 * A {@link BroadcastReceiver} that handles scheduled alarms for medication reminders and system checks.
 * <p>
 * This receiver is triggered by the Android {@link android.app.AlarmManager}. It performs the following:
 * <ul>
 *     <li>Validates if a medication reminder is still relevant (i.e., medication hasn't been taken already today).</li>
 *     <li>Triggers local notifications for relevant reminders via {@link NotificationService}.</li>
 *     <li>Automatically reschedules recurring alarms for the following day.</li>
 *     <li>Handles special system actions like the daily birthday check.</li>
 * </ul>
 * </p>
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

    /**
     * Entry point for the broadcast event.
     *
     * @param context The application context.
     * @param intent  The intent containing alarm metadata (medication info or action types).
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_BIRTHDAY_CHECK".equals(intent.getAction())) {
            handleBirthdayCheck();
            // Reschedule the check for the same time tomorrow
            alarmScheduler.scheduleBirthdayAlarm();
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

        // Optimization: Don't show a notification if the user already logged this dose as TAKEN today.
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DailyStats stats = user.getDailyStats().get(today);
        if (stats != null && stats.getMedicationUsageLogs() != null) {
            for (MedicationUsage usage : stats.getMedicationUsageLogs()) {
                if (hourStr != null && usage.getId().equals(medicationId) &&
                        hourStr.equals(usage.getScheduledTime()) &&
                        usage.getStatus() == MedicationStatus.TAKEN) {
                    Log.d(TAG, "Medication " + medicationName + " already taken for " + hourStr + ". Rescheduling only.");

                    // Reschedule for tomorrow without notifying
                    Medication medication = user.getMedications().get(medicationId);
                    if (medication != null) {
                        alarmScheduler.scheduleSpecificTime(medication, hourStr, true);
                    }
                    return;
                }
            }
        }

        Log.d(TAG, "Showing notification for: " + medicationName + " at " + hourStr);
        notificationService.showMedicationNotification(medicationId, medicationName, hourStr, notificationId);

        // Standard reschedule for the next day
        Medication medication = user.getMedications().get(medicationId);
        if (medication != null && hourStr != null) {
            alarmScheduler.scheduleSpecificTime(medication, hourStr, true);
        }
    }

    /**
     * Logic for performing the daily birthday check.
     * If today matches the user's birth month and day, a celebratory notification is triggered.
     */
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
