package com.example.sagivproject.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sagivproject.models.Medication;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationName = intent.getStringExtra("medication_name");
        int notificationId = intent.getIntExtra("notification_id", 0);
        
        Log.d(TAG, "Alarm received for: " + medicationName);
        
        // Show the notification
        notificationService.showMedicationNotification(medicationName, notificationId);

        // Reschedule the next alarm for this medication to make it repeating
        // Since setExactAndAllowWhileIdle is one-shot, we schedule it again for tomorrow.
        // Using the non-deprecated version of getSerializableExtra for API 33+
        Medication medication = intent.getSerializableExtra("medication_object", Medication.class);

        if (medication != null) {
            Log.d(TAG, "Rescheduling next alarm for: " + medicationName);
            alarmScheduler.schedule(medication);
        }
    }
}
