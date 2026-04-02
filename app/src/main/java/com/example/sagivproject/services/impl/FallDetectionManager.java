package com.example.sagivproject.services.impl;

import android.content.Context;
import android.content.Intent;

import com.example.sagivproject.services.IFallDetectionService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Manager class that implements {@link IFallDetectionService} to control the background fall detection service.
 * <p>
 * This class acts as an abstraction layer between the application logic and the Android lifecycle
 * requirements for services. It starts and stops the {@link FallDetectionServiceImpl} as a
 * foreground service to ensure persistent monitoring.
 * </p>
 */
@Singleton
public class FallDetectionManager implements IFallDetectionService {
    private final Context context;

    /**
     * Constructs a new FallDetectionManager.
     *
     * @param context The application context used to start/stop the background service.
     */
    @Inject
    public FallDetectionManager(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Triggers the start of the fall detection foreground service.
     */
    @Override
    public void startMonitoring() {
        Intent intent = new Intent(context, FallDetectionServiceImpl.class);
        context.startForegroundService(intent);
    }

    /**
     * Terminates the fall detection background service.
     */
    @Override
    public void stopMonitoring() {
        Intent intent = new Intent(context, FallDetectionServiceImpl.class);
        context.stopService(intent);
    }
}