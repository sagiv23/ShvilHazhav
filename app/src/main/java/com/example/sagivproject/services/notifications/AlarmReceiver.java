package com.example.sagivproject.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link BroadcastReceiver} that listens for scheduled medication alarms.
 * <p>
 * When an alarm is triggered by the Android system, this receiver is activated.
 * It retrieves the medication details from the intent and uses the {@link NotificationService}
 * to display a notification to the user, reminding them to take their medication.
 * </p>
 */
@AndroidEntryPoint
public class AlarmReceiver extends BroadcastReceiver {
    @Inject
    NotificationService notificationService;

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationName = intent.getStringExtra("medication_name");
        int notificationId = intent.getIntExtra("notification_id", 0);
        notificationService.showMedicationNotification(medicationName, notificationId);
    }
}
