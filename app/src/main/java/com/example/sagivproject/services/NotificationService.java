package com.example.sagivproject.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.sagivproject.R;

public class NotificationService {
    private static final String CHANNEL_ID = "medication_notifications", CHANNEL_NAME = "תזכורות תרופות";
    private final Context context;
    private final NotificationManager manager;

    public NotificationService(Context context) {
        this.context = context.getApplicationContext();
        this.manager = (NotificationManager)
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannelIfNeeded();
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }
    }

    public void show(String title, String message) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}