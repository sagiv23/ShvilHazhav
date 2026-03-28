package com.example.sagivproject.services.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link BroadcastReceiver} that listens for the device boot completion event.
 * <p>
 * Since scheduled alarms are cleared when the Android device is powered off or restarted,
 * this receiver is responsible for automatically rescheduling all necessary medication
 * reminders once the system has finished booting. It ensures continuous protection
 * without requiring the user to manually launch the application.
 * </p>
 */
@AndroidEntryPoint
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Inject
    AlarmScheduler alarmScheduler;

    @Inject
    IDatabaseService databaseService;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Receives the broadcast intent.
     *
     * @param context The application context.
     * @param intent  The intent being received (expected to be {@link Intent#ACTION_BOOT_COMPLETED}).
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, rescheduling alarms");
            rescheduleAlarms();
        }
    }

    /**
     * Internal logic to fetch the user's medication list and reschedule all active reminders.
     * <p>
     * This method verifies if a user is logged in before making a database request.
     * If a valid session exists, it retrieves all medications associated with that user
     * and passes them to the {@link AlarmScheduler}.
     * </p>
     */
    private void rescheduleAlarms() {
        if (sharedPreferencesUtil.isUserNotLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping reschedule");
            return;
        }

        String userId = sharedPreferencesUtil.getUserId();
        if (userId == null) {
            Log.d(TAG, "User ID is null, skipping reschedule");
            return;
        }

        databaseService.getMedicationService().getUserMedicationList(userId, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Medication> medications) {
                if (medications != null) {
                    Log.d(TAG, "Rescheduling alarms for " + medications.size() + " medications");
                    for (Medication medication : medications) {
                        alarmScheduler.schedule(medication);
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get user medications for rescheduling", e);
            }
        });
    }
}
