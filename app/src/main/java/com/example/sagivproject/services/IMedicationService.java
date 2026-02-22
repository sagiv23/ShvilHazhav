package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to user medications.
 * <p>
 * This service manages creating, retrieving, updating, and deleting medication records,
 * which are stored under a specific user's data in the database.
 * </p>
 */
public interface IMedicationService {
    /**
     * Generates a new, unique ID for a medication.
     *
     * @return A new unique ID string for the medication.
     */
    String generateMedicationId();

    /**
     * Creates a new medication record in the database for a specific user.
     *
     * @param uid        The ID of the user to whom the medication belongs.
     * @param medication The medication object to create.
     * @param callback   An optional callback to be invoked upon completion.
     */
    void createNewMedication(@NonNull String uid, @NonNull Medication medication, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves the list of all medications for a specific user.
     *
     * @param uid      The ID of the user whose medications are to be retrieved.
     * @param callback The callback to be invoked with the list of medications.
     */
    void getUserMedicationList(@NonNull String uid, @NonNull DatabaseCallback<List<Medication>> callback);

    /**
     * Deletes a medication from the database.
     *
     * @param uid          The ID of the user to whom the medication belongs.
     * @param medicationId The ID of the medication to delete.
     * @param callback     An optional callback to be invoked upon completion.
     */
    void deleteMedication(@NonNull String uid, @NonNull String medicationId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates an existing medication in the database.
     *
     * @param uid        The ID of the user to whom the medication belongs.
     * @param medication The medication object containing the updated information.
     * @param callback   An optional callback to be invoked upon completion.
     */
    void updateMedication(String uid, Medication medication, @Nullable DatabaseCallback<Void> callback);
}
