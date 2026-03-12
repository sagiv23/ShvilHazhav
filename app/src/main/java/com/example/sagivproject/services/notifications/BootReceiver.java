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

@AndroidEntryPoint
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Inject
    AlarmScheduler alarmScheduler;

    @Inject
    IDatabaseService databaseService;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, rescheduling alarms");
            rescheduleAlarms();
        }
    }

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
