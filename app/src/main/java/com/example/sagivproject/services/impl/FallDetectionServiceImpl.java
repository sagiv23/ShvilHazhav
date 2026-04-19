package com.example.sagivproject.services.impl;

import android.content.Context;
import android.content.Intent;

import com.example.sagivproject.services.IFallDetectionService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Implementation of {@link IFallDetectionService} that controls the background fall detection service.
 * <p>
 * This class acts as an abstraction layer between the application logic and the Android lifecycle
 * requirements for services. It starts and stops the {@link FallDetectionManager} as a
 * foreground service to ensure persistent monitoring.
 * </p>
 */
@Singleton
public class FallDetectionServiceImpl implements IFallDetectionService {
    private final Context context;

    /**
     * Constructs a new FallDetectionServiceImpl.
     *
     * @param context The application context used to start/stop the background service.
     */
    @Inject
    public FallDetectionServiceImpl(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Triggers the start of the fall detection foreground service.
     */
    @Override
    public void startMonitoring() {
        Intent intent = new Intent(context, FallDetectionManager.class);
        context.startForegroundService(intent);
    }

    /**
     * Terminates the fall detection background service.
     */
    @Override
    public void stopMonitoring() {
        Intent intent = new Intent(context, FallDetectionManager.class);
        context.stopService(intent);
    }
}