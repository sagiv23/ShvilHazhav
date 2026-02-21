package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IMedicationService;

import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

/**
 * An implementation of the {@link IMedicationService} interface.
 * <p>
 * This service manages medication data for specific users. It handles creating, retrieving,
 * updating, and deleting medications, which are stored in the database nested under each user's profile.
 * It extends {@link BaseDatabaseService} but uses custom paths to target the correct data.
 * </p>
 */
public class MedicationServiceImpl extends BaseDatabaseService<Medication> implements IMedicationService {
    private static final String USERS_PATH = "users";
    private static final String MEDICATIONS_PATH = "medications";

    /**
     * Constructs a new MedicationServiceImpl.
     * Note: The base path is empty as this service constructs paths dynamically.
     */
    @Inject
    public MedicationServiceImpl() {
        super("", Medication.class);
    }

    /**
     * Constructs the full database path for a user's medication list.
     *
     * @param uid The user's ID.
     * @return The full path to the user's medications.
     */
    private String getMedicationPath(String uid) {
        return USERS_PATH + "/" + uid + "/" + MEDICATIONS_PATH;
    }

    /**
     * Generates a new unique ID for a medication.
     *
     * @return A new unique ID string.
     */
    @Override
    public String generateMedicationId() {
        return super.generateId();
    }

    /**
     * Creates a new medication record for a specific user.
     *
     * @param uid        The user's ID.
     * @param medication The medication to create.
     * @param callback   The callback to be invoked upon completion.
     */
    @Override
    public void createNewMedication(@NonNull String uid, @NonNull Medication medication, @Nullable DatabaseCallback<Void> callback) {
        writeData(getMedicationPath(uid) + "/" + medication.getId(), medication, callback);
    }

    /**
     * Retrieves the list of all medications for a specific user.
     *
     * @param uid      The user's ID.
     * @param callback The callback to be invoked with the list of medications.
     */
    @Override
    public void getUserMedicationList(@NonNull String uid, @NonNull DatabaseCallback<List<Medication>> callback) {
        getDataList(getMedicationPath(uid), callback);
    }

    /**
     * Deletes a specific medication for a user.
     *
     * @param uid          The user's ID.
     * @param medicationId The ID of the medication to delete.
     * @param callback     The callback to be invoked upon completion.
     */
    @Override
    public void deleteMedication(@NonNull String uid, @NonNull String medicationId, @Nullable DatabaseCallback<Void> callback) {
        deleteData(getMedicationPath(uid) + "/" + medicationId, callback);
    }

    /**
     * Updates an existing medication for a user.
     *
     * @param uid        The user's ID.
     * @param medication The updated medication object.
     * @param callback   The callback to be invoked upon completion.
     */
    @Override
    public void updateMedication(String uid, Medication medication, @Nullable DatabaseCallback<Void> callback) {
        // This defines the update logic. It simply replaces the old medication with the new one.
        UnaryOperator<Medication> updateFunction = oldMedication -> medication;

        runTransaction(getMedicationPath(uid) + "/" + medication.getId(), updateFunction, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Medication result) {
                if (callback != null) {
                    callback.onCompleted(null); // The outer callback expects Void, not Medication
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }
}
