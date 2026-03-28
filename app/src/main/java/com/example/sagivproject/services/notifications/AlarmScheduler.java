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
 * A singleton service responsible for scheduling and canceling system alarms.
 * <p>
 * This class interacts with the Android {@link AlarmManager} to manage recurring events,
 * such as medication reminders and daily system checks. It handles the complexities of
 * different Android versions, including requesting exact alarm permissions where available.
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
     * @param alarmManager The system AlarmManager service instance.
     */
    @Inject
    public AlarmScheduler(@ApplicationContext Context context, AlarmManager alarmManager) {
        this.context = context;
        this.alarmManager = alarmManager;
    }

    /**
     * Schedules daily recurring alarms for all configured reminder hours of a medication.
     *
     * @param medication The {@link Medication} object containing the list of reminder hours.
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
     * Schedules a single alarm for a specific medication at a designated time.
     * <p>
     * If the time has already passed today, or if {@code forceTomorrow} is true,
     * the alarm is scheduled for the next day. It uses {@code setExactAndAllowWhileIdle}
     * when possible to ensure the reminder triggers even during Doze mode.
     * </p>
     *
     * @param medication    The medication details to include in the alarm intent.
     * @param hourStr       The target time in "HH:mm" format.
     * @param forceTomorrow Whether to skip today and schedule for the following day.
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
     * Cancels all currently scheduled alarms associated with a specific medication.
     *
     * @param medication The medication whose alarms should be terminated.
     */
    public void cancel(Medication medication) {
        if (medication.getReminderHours() == null) {
            return;
        }

        for (String hourStr : medication.getReminderHours()) {
            cancelSpecificTime(medication.getId(), hourStr);
        }
    }

    /**
     * Terminates a specific alarm identified by medication ID and scheduled time.
     *
     * @param medicationId The unique identifier of the medication.
     * @param hourStr      The scheduled hour string.
     */
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
     * Schedules a daily recurring check for birthdays at 9:00 AM.
     * Uses an action-specific Intent handled by {@link AlarmReceiver}.
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
                1001,
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
