package com.example.sagivproject.services.notifications;

import android.Manifest;
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
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.screens.MainActivity;
import com.example.sagivproject.screens.MedicationListActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A singleton service responsible for creating and displaying notifications in the application.
 * <p>
 * This service manages specialized notification channels for medication reminders, birthdays,
 * and fall detection alerts. It provides helper methods to build standardized {@link Notification}
 * objects and handles channel registration for Android 8.0+ compatibility.
 * </p>
 */
@Singleton
public class NotificationService {
    /**
     * Channel ID for medication-related notifications.
     */
    public static final String MEDICATIONS_CHANNEL_ID = "medication_notifications";

    /**
     * Channel ID for birthday-related notifications.
     */
    public static final String BIRTHDAYS_CHANNEL_ID = "birthday_notifications";

    /**
     * Channel ID for fall detection service notifications.
     */
    public static final String FALL_DETECTION_CHANNEL_ID = "fall_detection_notifications";

    private static final String MEDICATIONS_GROUP = "com.example.sagivproject.MEDICATIONS_GROUP";
    private static final String BIRTHDAYS_GROUP = "com.example.sagivproject.BIRTHDAYS_GROUP";

    private final Context context;
    private final NotificationManagerCompat manager;

    /**
     * Constructs a new NotificationService.
     *
     * @param context The application context.
     */
    @Inject
    public NotificationService(@ApplicationContext Context context) {
        this.context = context;
        this.manager = NotificationManagerCompat.from(context);

        createChannels();
    }

    /**
     * Creates and registers the required notification channels with the system.
     * Required for Android 8.0 (API level 26) and above.
     */
    private void createChannels() {
        NotificationChannel medChannel = new NotificationChannel(
                MEDICATIONS_CHANNEL_ID,
                context.getString(R.string.medication_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationChannel bdayChannel = new NotificationChannel(
                BIRTHDAYS_CHANNEL_ID,
                context.getString(R.string.birthday_channel_name),
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationChannel fallChannel = new NotificationChannel(
                FALL_DETECTION_CHANNEL_ID,
                "Fall Detection Service",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager systemManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (systemManager != null) {
            systemManager.createNotificationChannel(medChannel);
            systemManager.createNotificationChannel(bdayChannel);
            systemManager.createNotificationChannel(fallChannel);
        }
    }

    /**
     * Builds a persistent notification for the Fall Detection foreground service.
     *
     * @return A {@link Notification} configured for foreground service use.
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
     * Displays an immediate alert notification when a fall is detected.
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
     * Displays a reminder notification for a specific medication dose.
     * <p>
     * Includes interactive actions (Taken, Not Taken, Will Take) that allow the user
     * to log their intake directly from the notification shade.
     * </p>
     *
     * @param medicationId   The unique ID of the medication.
     * @param medicationName The display name of the medication.
     * @param hourStr        The scheduled dose time (HH:mm).
     * @param notificationId The unique ID for this notification instance.
     */
    public void showMedicationNotification(String medicationId, String medicationName, String hourStr, int notificationId) {
        String title = context.getString(R.string.medication_notif_title);
        String message = context.getString(R.string.medication_notif_body, medicationName);

        Intent intent = new Intent(context, MedicationListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

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
     * Helper to create a {@link NotificationCompat.Action} for medication logging.
     *
     * @param medicationId   Medication identifier.
     * @param medicationName Medication name.
     * @param hourStr        Scheduled time.
     * @param notificationId Original notification ID.
     * @param status         The {@link MedicationStatus} to log.
     * @param titleRes       String resource for the action button label.
     * @return A configured notification action.
     */
    private NotificationCompat.Action createAction(String medicationId, String medicationName, String hourStr, int notificationId, MedicationStatus status, int titleRes) {
        Intent intent = new Intent(context, MedicationActionReceiver.class);
        intent.putExtra("medication_id", medicationId);
        intent.putExtra("medication_name", medicationName);
        intent.putExtra("hour_str", hourStr);
        intent.putExtra("notification_id", notificationId);
        intent.putExtra("status", status.name());

        int requestCode = notificationId + status.ordinal();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Action.Builder(0, context.getString(titleRes), pendingIntent).build();
    }

    /**
     * Displays a celebratory birthday notification.
     *
     * @param firstName      The user's first name.
     * @param notificationId A unique ID for the notification.
     */
    public void showBirthdayNotification(String firstName, int notificationId) {
        String title = context.getString(R.string.birthday_notif_title);
        String message = context.getString(R.string.birthday_notif_body, firstName);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        show(BIRTHDAYS_CHANNEL_ID, title, message, notificationId, pendingIntent, BIRTHDAYS_GROUP);
        showSummaryNotification(BIRTHDAYS_CHANNEL_ID, BIRTHDAYS_GROUP);
    }

    /**
     * Displays a summary notification for a group to keep the notification drawer organized.
     *
     * @param channelId The channel ID.
     * @param groupKey  The group identifier.
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

    /**
     * Internal helper to build and display a standard notification.
     *
     * @param channelId      Target channel.
     * @param title          Notification title.
     * @param message        Notification body text.
     * @param notificationId Unique ID.
     * @param pendingIntent  Action to perform on click.
     * @param group          Group key for stacking.
     */
    private void show(String channelId, String title, String message, int notificationId, PendingIntent pendingIntent, String group) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setGroup(group)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(notificationId, builder.build());
        }
    }
}