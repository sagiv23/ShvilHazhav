package com.example.sagivproject.services;

/**
 * Interface for the Fall Detection service.
 * <p>
 * This service monitors the device's physical movement using accelerometer sensors
 * to detect patterns indicative of a hard fall. It is designed to enhance user safety
 * by providing automated alerts to emergency contacts in case of an accident.
 * </p>
 */
public interface IFallDetectionService {
    /**
     * Starts the fall detection monitoring process.
     * <p>
     * This typically triggers a foreground service that registers sensor listeners
     * to track device acceleration in the background.
     * </p>
     */
    void startMonitoring();

    /**
     * Stops the fall detection monitoring process.
     * <p>
     * Unregisters sensor listeners and terminates the background service to conserve
     * battery life and resources when protection is not required.
     * </p>
     */
    void stopMonitoring();
}