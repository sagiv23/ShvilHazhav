package com.example.sagivproject.services.impl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.notifications.NotificationService;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A background {@link Service} that monitors the device's accelerometer for fall detection.
 * <p>
 * This service runs as a foreground service to ensure it remains active even when the app
 * is in the background. It calculates total acceleration magnitude and compares it against
 * a pre-defined G-force threshold. When a potential fall is detected, it:
 * <ul>
 * <li>Triggers a local notification.</li>
 * <li>Attempts to fetch the user's high-accuracy location.</li>
 * <li>Sends an automated SMS alert to all registered emergency contacts.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public class FallDetectionServiceImpl extends Service implements SensorEventListener {
    private static final String TAG = "FallDetectionService";
    /**
     * Unique ID for the foreground service notification.
     */
    private static final int NOTIFICATION_ID = 1001;

    /**
     * The G-force threshold used to detect a fall (approx. 4.0G).
     * Calculated as sqrt(x^2 + y^2 + z^2).
     */
    private static final float FALL_THRESHOLD = 40.0f;

    /**
     * Minimum cooldown time between sending successive emergency alerts (30 seconds).
     * Prevents spamming contacts during multiple minor impacts or sensor noise.
     */
    private static final long MIN_TIME_BETWEEN_ALERTS = 30000;

    @Inject
    protected IDatabaseService databaseService;
    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;
    @Inject
    protected NotificationService notificationService;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private FusedLocationProviderClient fusedLocationClient;
    private long lastAlertTime = 0;
    private boolean isMonitoring = false;

    /**
     * Default constructor required for Android services.
     */
    public FallDetectionServiceImpl() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Called when the service is started via {@code startForegroundService}.
     * Transitions the service to the foreground with a persistent notification.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, notificationService.getFallDetectionForegroundNotification());
        startMonitoring();
        return START_STICKY;
    }

    /**
     * Registers the accelerometer sensor listener with UI delay for responsiveness.
     */
    private void startMonitoring() {
        if (!isMonitoring && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            isMonitoring = true;
            Log.d(TAG, "Fall detection monitoring started");
        }
    }

    /**
     * Unregisters the sensor listener and terminates the service.
     */
    private void stopMonitoring() {
        if (isMonitoring) {
            sensorManager.unregisterListener(this);
            isMonitoring = false;
            Log.d(TAG, "Fall detection monitoring stopped");
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    /**
     * Processes raw sensor data to detect sudden impacts.
     *
     * @param event The sensor event containing X, Y, Z acceleration values.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z);
            if (acceleration > FALL_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAlertTime > MIN_TIME_BETWEEN_ALERTS) {
                    lastAlertTime = currentTime;
                    handleFallDetected();
                }
            }
        }
    }

    /**
     * Coordinates the emergency response flow: Notification -> Location -> SMS.
     */
    private void handleFallDetected() {
        User user = sharedPreferencesUtil.getUser();
        if (user == null) return;

        notificationService.showFallDetectedNotification();

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        String locationUrl = (location != null) ?
                                "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude() : null;
                        sendEmergencyAlert(locationUrl);
                    })
                    .addOnFailureListener(e -> sendEmergencyAlert(null));
        } catch (SecurityException e) {
            sendEmergencyAlert(null);
        }
    }

    /**
     * Triggers the SMS alerting mechanism using contacts from the user object.
     *
     * @param locationUrl The location URL to include in the message.
     */
    private void sendEmergencyAlert(String locationUrl) {
        User user = sharedPreferencesUtil.getUser();
        if (user == null) return;
        List<EmergencyContact> contacts = new ArrayList<>(user.getEmergencyContacts().values());
        if (!contacts.isEmpty()) {
            databaseService.getEmergencyService().sendEmergencyAlert(getApplicationContext(), contacts, locationUrl, null);
        }
    }

    @Override
    public void onDestroy() {
        stopMonitoring();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}