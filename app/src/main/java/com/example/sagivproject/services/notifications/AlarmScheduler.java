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
            scheduleSpecificTime(medication, hourStr, false);
        }
    }

    /**
     * Schedules an alarm for a specific time.
     *
     * @param medication    The medication.
     * @param hourStr       The time in "HH:mm" format.
     * @param forceTomorrow If true, the alarm will be scheduled for tomorrow even if the time hasn't passed yet.
     */
    public void scheduleSpecificTime(Medication medication, String hourStr, boolean forceTomorrow) {
        String[] time = hourStr.split(":");
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time is in the past, or we force tomorrow, schedule it for the next day.
        if (forceTomorrow || calendar.before(Calendar.getInstance())) {
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

        if (alarmManager.canScheduleExactAlarms()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } catch (SecurityException e) {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    /**
     * Cancels all scheduled alarms for a given medication.
     */
    public void cancel(Medication medication) {
        if (medication.getReminderHours() == null) {
            return;
        }

        for (String hourStr : medication.getReminderHours()) {
            cancelSpecificTime(medication.getId(), hourStr);
        }
    }

    public void cancelSpecificTime(String medicationId, String hourStr) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        int requestCode = (medicationId + hourStr).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    /**
     * Schedules the daily birthday check alarm at 9:00 AM.
     */
    public void scheduleBirthdayAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("ACTION_BIRTHDAY_CHECK");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1001, // Unique ID for birthday alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}
