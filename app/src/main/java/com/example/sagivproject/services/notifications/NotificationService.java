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

import com.example.sagivproject.R;
import com.example.sagivproject.screens.MedicationListActivity;
import com.example.sagivproject.screens.SplashActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A singleton service responsible for creating and displaying notifications.
 */
@Singleton
public class NotificationService {
    public static final String MEDICATIONS_CHANNEL_ID = "medication_notifications";
    public static final String BIRTHDAYS_CHANNEL_ID = "birthday_notifications";
    private static final String MEDICATIONS_GROUP = "com.example.sagivproject.MEDICATIONS_GROUP";
    private static final String BIRTHDAYS_GROUP = "com.example.sagivproject.BIRTHDAYS_GROUP";

    private final Context context;
    private final NotificationManagerCompat manager;

    @Inject
    public NotificationService(@ApplicationContext Context context) {
        this.context = context;
        this.manager = NotificationManagerCompat.from(context);

        createChannels();
    }

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

    public void showMedicationNotification(String medicationName, int notificationId) {
        String title = context.getString(R.string.medication_notif_title);
        String message = context.getString(R.string.medication_notif_body, medicationName);

        Intent intent = new Intent(context, MedicationListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        show(MEDICATIONS_CHANNEL_ID, title, message, pendingIntent, notificationId, MEDICATIONS_GROUP);
        showSummaryNotification(MEDICATIONS_CHANNEL_ID, MEDICATIONS_GROUP);
    }

    public void showBirthdayNotification(String firstName, int notificationId) {
        String title = context.getString(R.string.birthday_notif_title);
        String message = context.getString(R.string.birthday_notif_body, firstName);

        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        show(BIRTHDAYS_CHANNEL_ID, title, message, pendingIntent, notificationId, BIRTHDAYS_GROUP);
        showSummaryNotification(BIRTHDAYS_CHANNEL_ID, BIRTHDAYS_GROUP);
    }

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

    private void show(String channelId, String title, String message, PendingIntent pendingIntent, int notificationId, String group) {
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
}
