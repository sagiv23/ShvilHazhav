package com.example.sagivproject.services.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.screens.MedicationListActivity;
import com.example.sagivproject.screens.SplashActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A singleton service responsible for creating and displaying notifications.
 * <p>
 * This class manages notification channels for different types of alerts (e.g., medications, birthdays)
 * and provides methods to build and show specific notifications to the user.
 * </p>
 */
@Singleton
public class NotificationService {
    public static final String MEDICATIONS_CHANNEL_ID = "medication_notifications";
    public static final String BIRTHDAYS_CHANNEL_ID = "birthday_notifications";
    private static final String MEDICATIONS_CHANNEL_NAME = "תזכורות תרופות";
    private static final String BIRTHDAYS_CHANNEL_NAME = "תזכורות יום הולדת";
    private static final String MEDICATIONS_GROUP = "medications_group";

    private final Context context;
    private final NotificationManager manager;

    /**
     * Constructs a new NotificationService and creates the necessary notification channels.
     *
     * @param context The application context.
     */
    @Inject
    public NotificationService(@ApplicationContext Context context) {
        this.context = context.getApplicationContext();
        this.manager = (NotificationManager)
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        createMedicationChannelIfNeeded();
        createBirthdayChannelIfNeeded();
    }

    /**
     * Creates the notification channel for medication reminders if it doesn't already exist.
     */
    private void createMedicationChannelIfNeeded() {
        NotificationChannel channel = new NotificationChannel(
                MEDICATIONS_CHANNEL_ID,
                MEDICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(channel);
    }

    /**
     * Creates the notification channel for birthday reminders if it doesn't already exist.
     */
    private void createBirthdayChannelIfNeeded() {
        NotificationChannel channel = new NotificationChannel(
                BIRTHDAYS_CHANNEL_ID,
                BIRTHDAYS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        );
        manager.createNotificationChannel(channel);
    }

    /**
     * Displays a notification to remind the user to take a specific medication.
     *
     * @param medicationName The name of the medication.
     * @param notificationId A unique ID for the notification.
     */
    public void showMedicationNotification(String medicationName, int notificationId) {
        String title = "תזכורת תרופה";
        String message = "הגיע הזמן לקחת את התרופה: " + medicationName;

        Intent intent = new Intent(context, MedicationListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        show(MEDICATIONS_CHANNEL_ID, title, message, pendingIntent, notificationId, MEDICATIONS_GROUP);
    }

    /**
     * Displays a birthday notification to the user.
     *
     * @param firstName The first name of the user.
     * @param notificationId A unique ID for the notification.
     */
    public void showBirthdayNotification(String firstName, int notificationId) {
        String title = "מזל טוב!";
        String message = "יום הולדת שמח, " + firstName + "! מאחלים לך בריאות ואושר.";

        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        show(BIRTHDAYS_CHANNEL_ID, title, message, pendingIntent, notificationId, null);
    }

    /**
     * A generic method to build and display a notification.
     *
     * @param channelId      The ID of the channel to post the notification to.
     * @param title          The title of the notification.
     * @param message        The main text of the notification.
     * @param pendingIntent  The intent to fire when the notification is tapped.
     * @param notificationId The ID for this notification.
     * @param group          The group key for stacking notifications.
     */
    private void show(String channelId, String title, String message, PendingIntent pendingIntent, int notificationId, String group) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(group)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(notificationId, builder.build());
    }
}
