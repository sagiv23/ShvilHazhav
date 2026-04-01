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
 * This service handles the lifecycle of medication prescriptions (CRUD) and provides
 * functionality for logging and retrieving medication intake history (compliance tracking).
 * </p>
 */
public interface IMedicationService {
    /**
     * Generates a new, unique identifier for a medication record.
     * @return A unique medication ID string.
     */
    String generateMedicationId();

    /**
     * Creates a new medication record associated with a specific user.
     * @param uid The unique identifier of the user.
     * @param medication The {@link Medication} object to store.
     * @param callback An optional callback invoked upon completion.
     */
    void createNewMedication(@NonNull String uid, @NonNull Medication medication, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves the complete list of medications for a specific user.
     * @param uid The unique identifier of the user.
     * @param callback The callback invoked with the list of retrieved medications.
     */
    void getUserMedicationList(@NonNull String uid, @NonNull DatabaseCallback<List<Medication>> callback);

    /**
     * Deletes a specific medication record from the database.
     * @param uid The unique identifier of the user.
     * @param medicationId The ID of the medication to remove.
     * @param callback An optional callback invoked upon completion.
     */
    void deleteMedication(@NonNull String uid, @NonNull String medicationId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates the details of an existing medication record.
     * @param uid The unique identifier of the user.
     * @param medication The {@link Medication} object containing updated information.
     * @param callback An optional callback invoked upon completion.
     */
    void updateMedication(String uid, Medication medication, @Nullable DatabaseCallback<Void> callback);

    /**
     * Logs an intake event or status change for a specific medication dose.
     * @param uid The unique identifier of the user.
     * @param usage The {@link MedicationUsage} object representing the event.
     * @param callback An optional callback invoked upon completion.
     */
    void logMedicationUsage(@NonNull String uid, @NonNull MedicationUsage usage, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves all historical medication usage logs for a specific user.
     * @param uid The unique identifier of the user.
     * @param callback The callback invoked with the list of usage logs.
     */
    void getMedicationUsageLogs(@NonNull String uid, @NonNull DatabaseCallback<List<MedicationUsage>> callback);

    /**
     * Clears all historical medication usage logs for a specific user.
     * @param uid The unique identifier of the user.
     * @param callback An optional callback invoked upon completion.
     */
    void clearMedicationUsageLogs(@NonNull String uid, @Nullable DatabaseCallback<Void> callback);
}