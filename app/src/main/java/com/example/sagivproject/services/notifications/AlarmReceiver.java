package com.example.sagivproject.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link BroadcastReceiver} that listens for scheduled medication alarms.
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
        String medicationId = intent.getStringExtra("medication_id");
        String medicationName = intent.getStringExtra("medication_name");
        int notificationId = intent.getIntExtra("notification_id", 0);

        // Check if the medication still exists in the user's cache
        User user = sharedPreferencesUtil.getUser();
        if (user == null || user.getMedications() == null || !user.getMedications().containsKey(medicationId)) {
            Log.d(TAG, "Medication " + medicationName + " (ID: " + medicationId + ") no longer exists. Skipping notification and reschedule.");
            return;
        }

        Log.d(TAG, "Alarm received for: " + medicationName);

        // Show the notification
        notificationService.showMedicationNotification(medicationName, notificationId);

        // Reschedule the next alarm for this medication to make it repeating.
        // We fetch the latest medication data from the user object to ensure it's up to date.
        Medication medication = user.getMedications().get(medicationId);

        if (medication != null) {
            Log.d(TAG, "Rescheduling next alarms for: " + medicationName);
            // Calling schedule again will recalculate the next occurrence for each reminder hour.
            // Since the current time is now exactly (or slightly after) the alarm time,
            // the schedule logic will push the alarm for this specific hour to tomorrow.
            alarmScheduler.schedule(medication);
        } else {
            Log.e(TAG, "Failed to reschedule: Medication object is null in user cache.");
        }
    }
}
