package com.example.sagivproject.services.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.sagivproject.models.Medication;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A singleton class responsible for scheduling and canceling medication reminder alarms.
 */
@Singleton
public class AlarmScheduler {
    private final Context context;
    private final AlarmManager alarmManager;

    @Inject
    public AlarmScheduler(@ApplicationContext Context context, AlarmManager alarmManager) {
        this.context = context;
        this.alarmManager = alarmManager;
    }

    /**
     * Schedules daily repeating alarms for a given medication.
     */
    public void schedule(Medication medication) {
        if (medication.getReminderHours() == null) {
            return;
        }

        for (String hourStr : medication.getReminderHours()) {
            String[] time = hourStr.split(":");
            int hour = Integer.parseInt(time[0]);
            int minute = Integer.parseInt(time[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // If the time is in the past, schedule it for the next day.
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("medication_name", medication.getName());
            intent.putExtra("medication_id", medication.getId());
            intent.putExtra("hour_str", hourStr);
            intent.putExtra("medication_object", medication);

            int requestCode = (medication.getId() + hourStr).hashCode();
            intent.putExtra("notification_id", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Checking for exact alarm permission and handling SecurityException
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } catch (SecurityException e) {
                    // Fallback if permission is missing or revoked
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            } else {
                // Use non-exact alarm if permission is not granted
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    public void cancel(Medication medication) {
        if (medication.getReminderHours() == null) {
            return;
        }

        for (String hourStr : medication.getReminderHours()) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            int requestCode = (medication.getId() + hourStr).hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }
    }
}
