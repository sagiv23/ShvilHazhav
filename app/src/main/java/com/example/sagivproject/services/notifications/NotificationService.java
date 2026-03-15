package com.example.sagivproject.services.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.sagivproject.R;
import com.example.sagivproject.screens.MainActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A singleton service responsible for creating and displaying notifications in the application.
 * <p>
 * This service manages notification channels for medication reminders and birthdays.
 * It uses {@link NavDeepLinkBuilder} to allow users to navigate directly from a
 * notification to the relevant screen (e.g., Medication List). It also supports
 * notification grouping to avoid cluttering the user's notification drawer.
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
     * Creates the required notification channels for the application (required for Android 8.0+).
     */
    private void createChannels() {
        NotificationChannel medChannel = new NotificationChannel(
                MEDICATIONS_CHANNEL_ID,
                context.getString(R.string.medication_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationChannel bdayChannel = new NotificationChannel(
                BIRTHDAYS_CHANNEL_ID,
                context.getString(R.string.birthday_channel_name),
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager systemManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (systemManager != null) {
            systemManager.createNotificationChannel(medChannel);
            systemManager.createNotificationChannel(bdayChannel);
        }
    }

    /**
     * Displays a notification reminding the user to take a specific medication.
     *
     * @param medicationName The name of the medication.
     * @param notificationId A unique ID for the notification.
     */
    public void showMedicationNotification(String medicationName, int notificationId) {
        String title = context.getString(R.string.medication_notif_title);
        String message = context.getString(R.string.medication_notif_body, medicationName);

        // Create a PendingIntent that triggers deep linking into the MedicationListFragment
        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.medicationListFragment)
                .setComponentName(MainActivity.class)
                .createPendingIntent();

        show(title, message, pendingIntent, notificationId);
        showSummaryNotification(MEDICATIONS_CHANNEL_ID, MEDICATIONS_GROUP);
    }

    /**
     * Displays a birthday greeting notification for the user.
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
     * Shows a summary notification for a specific group to keep the notification drawer organized.
     *
     * @param channelId The channel ID.
     * @param groupKey  The group key.
     */
    private void showSummaryNotification(String channelId, String groupKey) {
        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_medication)
                .setStyle(new NotificationCompat.InboxStyle())
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(groupKey.hashCode(), summaryBuilder.build());
        }
    }

    /**
     * Internal helper to build and display a notification.
     */
    private void show(String channelId, String title, String message, int notificationId, PendingIntent pendingIntent, String group) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_medication)
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

    /**
     * Overloaded show method for medication notifications.
     */
    private void show(String title, String message, PendingIntent pendingIntent, int notificationId) {
        show(NotificationService.MEDICATIONS_CHANNEL_ID, title, message, notificationId, pendingIntent, NotificationService.MEDICATIONS_GROUP);
    }
}
