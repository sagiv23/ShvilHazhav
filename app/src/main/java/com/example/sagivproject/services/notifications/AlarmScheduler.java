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
 * <p>
 * This class uses Android's {@link AlarmManager} to set up repeating daily alarms for each
 * reminder time specified in a {@link Medication} object. When an alarm is triggered, it fires
 * an intent that is caught by the {@link AlarmReceiver}.
 * </p>
 */
@Singleton
public class AlarmScheduler {
    private final Context context;
    private final AlarmManager alarmManager;

    /**
     * Constructs a new AlarmScheduler.
     *
     * @param context      The application context.
     * @param alarmManager The AlarmManager provided by Hilt.
     */
    @Inject
    public AlarmScheduler(@ApplicationContext Context context, AlarmManager alarmManager) {
        this.context = context;
        this.alarmManager = alarmManager;
    }

    /**
     * Schedules daily repeating alarms for a given medication.
     *
     * @param medication The medication for which to schedule reminders.
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
            // Create a unique request code for each alarm to avoid conflicts.
            int requestCode = (medication.getId() + hourStr).hashCode();
            intent.putExtra("notification_id", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    /**
     * Cancels all scheduled alarms for a given medication.
     *
     * @param medication The medication for which to cancel reminders.
     */
    public void cancel(Medication medication) {
        if (medication.getReminderHours() == null) {
            return;
        }

        for (String hourStr : medication.getReminderHours()) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            // Recreate the same request code to identify the correct PendingIntent to cancel.
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
