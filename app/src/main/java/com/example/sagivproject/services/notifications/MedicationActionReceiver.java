package com.example.sagivproject.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link BroadcastReceiver} that handles direct actions from medication notifications.
 * <p>
 * This receiver captures button clicks (Taken, Not Taken, Snoozed) from the reminder notifications.
 * It performs the necessary database logging and updates the local cache in {@link SharedPreferencesUtil}
 * to ensure the UI remains synchronized without requiring a manual app refresh.
 * </p>
 */
@AndroidEntryPoint
public class MedicationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "MedicationActionReceiver";

    /** Central database service for logging usage. */
    @Inject
    IDatabaseService databaseService;

    /** Utility for keeping local session data in sync. */
    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Triggered when an action button on a medication notification is clicked.
     * <p>
     * Extracts medication metadata and the selected status from the intent,
     * logs the event to Firebase, and updates the today's statistics in the local cache.
     * Finally, dismisses the notification.
     * </p>
     * @param context The application context.
     * @param intent The intent containing medication details and selected status.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationId = intent.getStringExtra("medication_id");
        String medicationName = intent.getStringExtra("medication_name");
        String hourStr = intent.getStringExtra("hour_str");
        int notificationId = intent.getIntExtra("notification_id", -1);
        String statusStr = intent.getStringExtra("status");

        Log.d(TAG, "Action received: " + statusStr + " for " + medicationName + " at " + hourStr);

        User user = sharedPreferencesUtil.getUser();
        if (user != null && medicationId != null && statusStr != null) {
            MedicationStatus status = MedicationStatus.valueOf(statusStr);
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String timeNow = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            MedicationUsage usage = new MedicationUsage(medicationId, medicationName, timeNow, today, hourStr, status);

            databaseService.getMedicationService().logMedicationUsage(user.getId(), usage, new IDatabaseService.DatabaseCallback<>() {
                @Override
                public void onCompleted(Void object) {
                    Log.d(TAG, "Usage (" + status + ") logged successfully from notification");

                    DailyStats stats = user.getTodayStats();
                    stats.addMedicationUsageLog(usage);
                    if (status == MedicationStatus.TAKEN) {
                        stats.addMedicationTaken();
                    } else if (status == MedicationStatus.NOT_TAKEN) { stats.addMedicationMissed(); }
                    sharedPreferencesUtil.saveUser(user);
                }

                @Override
                public void onFailed(Exception e) { Log.e(TAG, "Failed to log usage from notification", e); }
            });
        }

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId);
        }
    }
}