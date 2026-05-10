package com.example.sagivproject.services.notifications;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage.MedicationStatus;
import com.example.sagivproject.screens.MainActivity;
import com.example.sagivproject.screens.MedicationListActivity;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A singleton service responsible for creating notifications and managing alarms.
 * <p>
 * This service centralizes both the delivery of notifications and the scheduling
 * of medication reminders using the system {@link AlarmManager}.
 * </p>
 */
@Singleton
public class NotificationService {
    public static final String MEDICATIONS_CHANNEL_ID = "medication_notifications";
    public static final String FALL_DETECTION_CHANNEL_ID = "fall_detection_notifications";
    private static final String MEDICATIONS_GROUP = "com.example.sagivproject.MEDICATIONS_GROUP";

    private final Context context;
    private final NotificationManagerCompat manager;
    private final AlarmManager alarmManager;

    @Inject
    public NotificationService(@ApplicationContext Context context, AlarmManager alarmManager) {
        this.context = context;
        this.manager = NotificationManagerCompat.from(context);
        this.alarmManager = alarmManager;

        createChannels();
    }

    /**
     * Creates and registers notification channels for medications and fall detection.
     * Required for Android 8.0 (API level 26) and above.
     */
    private void createChannels() {
        NotificationChannel medChannel = new NotificationChannel(
                MEDICATIONS_CHANNEL_ID,
                context.getString(R.string.medication_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationChannel fallChannel = new NotificationChannel(
                FALL_DETECTION_CHANNEL_ID,
                "Fall Detection Service",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager systemManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (systemManager != null) {
            systemManager.createNotificationChannel(medChannel);
            systemManager.createNotificationChannel(fallChannel);
        }
    }

    // --- Alarm Scheduling Logic ---

    /**
     * Schedules all reminder alarms for a given medication.
     *
     * @param medication The medication containing reminder hours.
     */
    public void schedule(Medication medication) {
        if (medication.getReminderHours() == null) return;
        for (String hourStr : medication.getReminderHours()) {
            scheduleSpecificTime(medication, hourStr, false);
        }
    }

    /**
     * Schedules a specific reminder time for a medication.
     *
     * @param medication    The medication object.
     * @param hourStr       The time in "HH:mm" format.
     * @param forceTomorrow If true, schedules for the next day regardless of the current time.
     */
    public void scheduleSpecificTime(Medication medication, String hourStr, boolean forceTomorrow) {
        String[] time = hourStr.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (forceTomorrow || calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_MEDICATION_ALARM);
        intent.putExtra("medication_name", medication.getName());
        intent.putExtra("medication_id", medication.getId());
        intent.putExtra("hour_str", hourStr);

        int requestCode = (medication.getId() + hourStr).hashCode();
        intent.putExtra("notification_id", requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager.canScheduleExactAlarms()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } catch (SecurityException e) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    /**
     * Cancels all scheduled alarms for a given medication.
     *
     * @param medication The medication whose alarms should be canceled.
     */
    public void cancel(Medication medication) {
        if (medication.getReminderHours() == null) return;
        for (String hourStr : medication.getReminderHours()) {
            cancelSpecificTime(medication.getId(), hourStr);
        }
    }

    /**
     * Cancels a specific alarm instance.
     *
     * @param medicationId The ID of the medication.
     * @param hourStr      The scheduled hour string.
     */
    public void cancelSpecificTime(String medicationId, String hourStr) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_MEDICATION_ALARM);
        int requestCode = (medicationId + hourStr).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    // --- Notification Display Logic ---

    /**
     * Creates a notification for the Fall Detection foreground service.
     *
     * @return The configured {@link Notification}.
     */
    public Notification getFallDetectionForegroundNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, FALL_DETECTION_CHANNEL_ID)
                .setContentTitle("שביל הזהב - הגנה פעילה")
                .setContentText("זיהוי נפילות פועל ברקע לשמירה על בטיחותך")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    /**
     * Displays a notification when a fall is detected.
     */
    public void showFallDetectedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FALL_DETECTION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("זוהתה נפילה!")
                .setContentText("שולח התראות לאנשי הקשר לשעת חירום...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(2001, builder.build());
        }
    }

    /**
     * Displays a reminder notification for a medication dose with interactive actions.
     *
     * @param medicationId   The medication ID.
     * @param medicationName The name of the medication.
     * @param hourStr        The scheduled time.
     * @param notificationId Unique ID for the notification.
     */
    public void showMedicationNotification(String medicationId, String medicationName, String hourStr, int notificationId) {
        String title = context.getString(R.string.medication_notif_title);
        String message = context.getString(R.string.medication_notif_body, medicationName);

        Intent intent = new Intent(context, MedicationListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MEDICATIONS_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setGroup(MEDICATIONS_GROUP)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        builder.addAction(createAction(medicationId, medicationName, hourStr, notificationId, MedicationStatus.TAKEN, R.string.took));
        builder.addAction(createAction(medicationId, medicationName, hourStr, notificationId, MedicationStatus.NOT_TAKEN, R.string.didnt_take));
        builder.addAction(createAction(medicationId, medicationName, hourStr, notificationId, MedicationStatus.SNOOZED, R.string.will_take));

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(notificationId, builder.build());
        }

        showSummaryNotification(MEDICATIONS_CHANNEL_ID, MEDICATIONS_GROUP);
    }

    /**
     * Helper to create a notification action button for medication logging.
     *
     * @param medicationId   Medication ID.
     * @param medicationName Medication name.
     * @param hourStr        Scheduled time.
     * @param notificationId Notification ID to dismiss.
     * @param status         The status to log.
     * @param titleRes       String resource for the button title.
     * @return A {@link NotificationCompat.Action}.
     */
    private NotificationCompat.Action createAction(String medicationId, String medicationName, String hourStr, int notificationId, MedicationStatus status, int titleRes) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_MEDICATION_LOG);
        intent.putExtra("medication_id", medicationId);
        intent.putExtra("medication_name", medicationName);
        intent.putExtra("hour_str", hourStr);
        intent.putExtra("notification_id", notificationId);
        intent.putExtra("status", status.name());

        int requestCode = notificationId + status.ordinal();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action.Builder(0, context.getString(titleRes), pendingIntent).build();
    }

    /**
     * Displays a summary notification to group multiple medication reminders.
     *
     * @param channelId The channel ID.
     * @param groupKey  The group key.
     */
    private void showSummaryNotification(String channelId, String groupKey) {
        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.InboxStyle())
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(groupKey.hashCode(), summaryBuilder.build());
        }
    }
}
