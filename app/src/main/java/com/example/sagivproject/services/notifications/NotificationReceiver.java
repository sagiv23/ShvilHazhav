package com.example.sagivproject.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.MedicationUsage.MedicationStatus;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A unified {@link BroadcastReceiver} that handles all notification-related events.
 * <p>
 * This receiver manages:
 * <ul>
 * <li>System boot completion to restore alarms.</li>
 * <li>Scheduled medication reminders.</li>
 * <li>User interactions (button clicks) from notifications.</li>
 * </ul>
 */
@AndroidEntryPoint
public class NotificationReceiver extends BroadcastReceiver {
    /**
     * Intent action triggered when a scheduled medication alarm expires.
     */
    public static final String ACTION_MEDICATION_ALARM = "com.example.sagivproject.ACTION_MEDICATION_ALARM";

    /**
     * Intent action triggered when a user interacts with a medication notification button.
     */
    public static final String ACTION_MEDICATION_LOG = "com.example.sagivproject.ACTION_MEDICATION_LOG";

    private static final String TAG = "NotificationReceiver";
    @Inject
    protected CalendarUtil calendarUtil;
    @Inject
    NotificationService notificationService;
    @Inject
    IDatabaseService databaseService;
    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * Delegates handling based on the Intent action.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        Log.d(TAG, "Received action: " + action);

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
            case "android.intent.action.QUICKBOOT_POWERON":
                handleBoot();
                break;
            case ACTION_MEDICATION_ALARM:
                handleMedicationAlarm(intent);
                break;
            case ACTION_MEDICATION_LOG:
                handleMedicationLog(context, intent);
                break;
        }
    }

    /**
     * Handles device boot events by rescheduling all active medication reminders.
     */
    private void handleBoot() {
        if (sharedPreferencesUtil.isUserNotLoggedIn()) return;

        User user = sharedPreferencesUtil.getUser();
        if (user == null || user.getId() == null) return;

        databaseService.getMedicationService().getUserMedicationList(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Medication> medications) {
                if (medications != null) {
                    for (Medication medication : medications) {
                        notificationService.schedule(medication);
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to reschedule alarms on boot", e);
            }
        });
    }

    /**
     * Handles triggered medication alarms. Checks if the medication was already taken
     * today before showing a notification.
     *
     * @param intent The alarm intent containing medication metadata.
     */
    private void handleMedicationAlarm(Intent intent) {
        String medicationId = intent.getStringExtra("medication_id");
        String medicationName = intent.getStringExtra("medication_name");
        String hourStr = intent.getStringExtra("hour_str");
        int notificationId = intent.getIntExtra("notification_id", 0);

        User user = sharedPreferencesUtil.getUser();
        if (user == null || user.getMedications() == null || !user.getMedications().containsKey(medicationId)) {
            return;
        }

        String today = calendarUtil.getCurrentDate();
        DailyStats stats = user.getDailyStats().get(today);
        if (stats != null && stats.getMedicationUsageLogs() != null) {
            for (MedicationUsage usage : stats.getMedicationUsageLogs()) {
                if (usage.getMedicationId().equals(medicationId) &&
                        usage.getScheduledTime().equals(hourStr) &&
                        usage.getStatus() == MedicationStatus.TAKEN) {
                    // Already taken today for this scheduled time, just reschedule for tomorrow
                    Medication medication = user.getMedications().get(medicationId);
                    if (medication != null) {
                        notificationService.scheduleSpecificTime(medication, hourStr, true);
                    }
                    return;
                }
            }
        }

        notificationService.showMedicationNotification(medicationId, medicationName, hourStr, notificationId);

        Medication medication = user.getMedications().get(medicationId);
        if (medication != null && hourStr != null) {
            notificationService.scheduleSpecificTime(medication, hourStr, true);
        }
    }

    /**
     * Handles user interaction with notification action buttons (Taken, Snoozed, etc.).
     * Logs the status to the database and updates local cache.
     *
     * @param context The application context.
     * @param intent  The intent containing the selected status and medication metadata.
     */
    private void handleMedicationLog(Context context, Intent intent) {
        String medicationId = intent.getStringExtra("medication_id");
        String hourStr = intent.getStringExtra("hour_str");
        int notificationId = intent.getIntExtra("notification_id", -1);
        String statusStr = intent.getStringExtra("status");

        User user = sharedPreferencesUtil.getUser();
        if (user != null && medicationId != null && statusStr != null) {
            MedicationStatus status = MedicationStatus.valueOf(statusStr);
            String timeNow = calendarUtil.formatDate(System.currentTimeMillis(), "HH:mm");

            String usageId = databaseService.getMedicationService().generateUsageId();
            MedicationUsage usage = new MedicationUsage(usageId, medicationId, timeNow, hourStr, status);

            databaseService.getStatsService().logMedicationUsage(user.getId(), usage, new IDatabaseService.DatabaseCallback<>() {
                @Override
                public void onCompleted(Void object) {
                    user.getTodayStats().addMedicationUsageLog(usage);
                    sharedPreferencesUtil.saveUser(user);
                }

                @Override
                public void onFailed(Exception e) {
                    Log.e(TAG, "Failed to log usage", e);
                }
            });
        }

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId);
        }
    }
}