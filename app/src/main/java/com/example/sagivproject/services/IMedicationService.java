package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to user medications.
 * <p>
 * This service handles creating, retrieving, updating, and deleting medications for a specific user,
 * as well as logging and retrieving medication usage history.
 * </p>
 */
public interface IMedicationService {
    /**
     * Generates a new unique ID for a medication.
     *
     * @return A unique ID string.
     */
    String generateMedicationId();

    /**
     * Creates a new medication record for a specific user.
     *
     * @param uid        The ID of the user owning the medication.
     * @param medication The medication object to create.
     * @param callback   An optional callback to be invoked upon completion.
     */
    void createNewMedication(@NonNull String uid, @NonNull Medication medication, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves the list of medications for a specific user.
     *
     * @param uid      The ID of the user.
     * @param callback The callback to be invoked with the list of medications.
     */
    void getUserMedicationList(@NonNull String uid, @NonNull DatabaseCallback<List<Medication>> callback);

    /**
     * Deletes a specific medication for a user.
     *
     * @param uid          The ID of the user.
     * @param medicationId The ID of the medication to delete.
     * @param callback     An optional callback to be invoked upon completion.
     */
    void deleteMedication(@NonNull String uid, @NonNull String medicationId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates an existing medication's details.
     *
     * @param uid        The ID of the user.
     * @param medication The updated medication object.
     * @param callback   An optional callback to be invoked upon completion.
     */
    void updateMedication(String uid, Medication medication, @Nullable DatabaseCallback<Void> callback);

    /**
     * Logs the status of a medication for a specific time and date.
     *
     * @param uid      The ID of the user.
     * @param usage    The medication usage data to log.
     * @param callback An optional callback to be invoked upon completion.
     */
    void logMedicationUsage(@NonNull String uid, @NonNull MedicationUsage usage, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves the medication usage logs for a specific user.
     *
     * @param uid      The ID of the user.
     * @param callback The callback to be invoked with the list of usage logs.
     */
    void getMedicationUsageLogs(@NonNull String uid, @NonNull DatabaseCallback<List<MedicationUsage>> callback);

    /**
     * Clears all medication usage logs for a specific user.
     *
     * @param uid      The ID of the user.
     * @param callback An optional callback to be invoked upon completion.
     */
    void clearMedicationUsageLogs(@NonNull String uid, @Nullable DatabaseCallback<Void> callback);
}
