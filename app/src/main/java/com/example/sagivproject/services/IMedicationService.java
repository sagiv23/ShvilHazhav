package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to user medications.
 */
public interface IMedicationService {
    String generateMedicationId();

    void createNewMedication(@NonNull String uid, @NonNull Medication medication, @Nullable DatabaseCallback<Void> callback);

    void getUserMedicationList(@NonNull String uid, @NonNull DatabaseCallback<List<Medication>> callback);

    void deleteMedication(@NonNull String uid, @NonNull String medicationId, @Nullable DatabaseCallback<Void> callback);

    void updateMedication(String uid, Medication medication, @Nullable DatabaseCallback<Void> callback);

    /**
     * Logs the status of a medication for a specific time and date.
     */
    void logMedicationUsage(@NonNull String uid, @NonNull MedicationUsage usage, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves the medication usage logs for a specific user.
     */
    void getMedicationUsageLogs(@NonNull String uid, @NonNull DatabaseCallback<List<MedicationUsage>> callback);
}
