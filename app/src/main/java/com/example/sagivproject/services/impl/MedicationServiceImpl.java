package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IMedicationService;

import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

/**
 * An implementation of the {@link IMedicationService} interface.
 */
public class MedicationServiceImpl extends BaseDatabaseService<Medication> implements IMedicationService {
    private static final String USERS_PATH = "users";
    private static final String MEDICATIONS_PATH = "medications";
    private static final String USAGE_LOGS_PATH = "medicationUsageLogs";

    @Inject
    public MedicationServiceImpl() {
        super("", Medication.class);
    }

    @Override
    public String generateMedicationId() {
        return super.generateId();
    }

    @Override
    public void createNewMedication(@NonNull String uid, @NonNull Medication medication, @Nullable DatabaseCallback<Void> callback) {
        writeData(getMedicationItemPath(uid, medication.getId()), medication, callback);
    }

    @Override
    public void getUserMedicationList(@NonNull String uid, @NonNull DatabaseCallback<List<Medication>> callback) {
        getDataList(getMedicationPath(uid), callback);
    }

    @Override
    public void deleteMedication(@NonNull String uid, @NonNull String medicationId, @Nullable DatabaseCallback<Void> callback) {
        deleteData(getMedicationItemPath(uid, medicationId), callback);
    }

    @Override
    public void updateMedication(String uid, Medication medication, @Nullable DatabaseCallback<Void> callback) {
        UnaryOperator<Medication> updateFunction = oldMedication -> medication;
        runTransaction(getMedicationItemPath(uid, medication.getId()), updateFunction, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Medication result) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    @Override
    public void logMedicationUsage(@NonNull String uid, @NonNull MedicationUsage usage, @Nullable DatabaseCallback<Void> callback) {
        String logPath = USERS_PATH + "/" + uid + "/" + USAGE_LOGS_PATH;
        String logId = databaseReference.child(logPath).push().getKey();
        if (logId != null) {
            databaseReference.child(logPath).child(logId).setValue(usage)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (callback != null) callback.onCompleted(null);
                        } else {
                            if (callback != null) callback.onFailed(task.getException());
                        }
                    });
        }
    }

    @Override
    public void getMedicationUsageLogs(@NonNull String uid, @NonNull DatabaseCallback<List<MedicationUsage>> callback) {
        String logPath = USERS_PATH + "/" + uid + "/" + USAGE_LOGS_PATH;
        databaseReference.child(logPath).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                java.util.ArrayList<MedicationUsage> logs = new java.util.ArrayList<>();
                for (com.google.firebase.database.DataSnapshot ds : task.getResult().getChildren()) {
                    MedicationUsage usage = ds.getValue(MedicationUsage.class);
                    if (usage != null) logs.add(usage);
                }
                callback.onCompleted(logs);
            } else {
                callback.onFailed(task.getException());
            }
        });
    }

    private String getMedicationPath(String uid) {
        return USERS_PATH + "/" + uid + "/" + MEDICATIONS_PATH;
    }

    private String getMedicationItemPath(String uid, String medicationId) {
        return getMedicationPath(uid) + "/" + medicationId;
    }
}
