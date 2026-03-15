package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IMedicationService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

/**
 * Implementation of the {@link IMedicationService} interface.
 * <p>
 * This class handles all database interactions related to medication management,
 * including CRUD operations for medications and logging of medication intake events.
 * It uses Firebase transactions to ensure that daily statistics (taken vs missed counts)
 * are updated atomically alongside the usage logs.
 * </p>
 */
public class MedicationServiceImpl extends BaseDatabaseService<Medication> implements IMedicationService {
    private static final String USERS_PATH = "users";
    private static final String MEDICATIONS_PATH = "medications";
    private static final String FIELD_DAILY_STATS = "dailyStats";

    /**
     * Constructs a new MedicationServiceImpl.
     */
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
        databaseReference.child(USERS_PATH).child(uid).child(FIELD_DAILY_STATS).child(usage.getDate()).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                DailyStats stats = currentData.getValue(DailyStats.class);
                if (stats == null) stats = new DailyStats();

                // Increment appropriate counter based on status
                if (usage.getStatus() == MedicationStatus.TAKEN) {
                    stats.addMedicationTaken();
                } else if (usage.getStatus() == MedicationStatus.NOT_TAKEN) {
                    stats.addMedicationMissed();
                }

                stats.addMedicationUsageLog(usage);
                currentData.setValue(stats);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (callback != null) {
                    if (error != null) callback.onFailed(error.toException());
                    else callback.onCompleted(null);
                }
            }
        });
    }

    @Override
    public void getMedicationUsageLogs(@NonNull String uid, @NonNull DatabaseCallback<List<MedicationUsage>> callback) {
        databaseReference.child(USERS_PATH).child(uid).child(FIELD_DAILY_STATS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<MedicationUsage> allLogs = new ArrayList<>();
                for (DataSnapshot daySnapshot : task.getResult().getChildren()) {
                    DailyStats stats = daySnapshot.getValue(DailyStats.class);
                    if (stats != null && stats.getMedicationUsageLogs() != null) {
                        allLogs.addAll(stats.getMedicationUsageLogs());
                    }
                }
                callback.onCompleted(allLogs);
            } else {
                callback.onFailed(task.getException());
            }
        });
    }

    @Override
    public void clearMedicationUsageLogs(@NonNull String uid, @Nullable DatabaseCallback<Void> callback) {
        databaseReference.child(USERS_PATH).child(uid).child(FIELD_DAILY_STATS).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (MutableData dayData : currentData.getChildren()) {
                    DailyStats stats = dayData.getValue(DailyStats.class);
                    if (stats != null) {
                        stats.getMedicationUsageLogs().clear();
                        stats.setMedicationsTaken(0);
                        stats.setMedicationsMissed(0);
                        dayData.setValue(stats);
                    }
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (callback != null) {
                    if (error != null) callback.onFailed(error.toException());
                    else callback.onCompleted(null);
                }
            }
        });
    }

    /**
     * Helper to construct the path to a user's medication collection.
     */
    private String getMedicationPath(String uid) {
        return USERS_PATH + "/" + uid + "/" + MEDICATIONS_PATH;
    }

    /**
     * Helper to construct the path to a specific medication item.
     */
    private String getMedicationItemPath(String uid, String medicationId) {
        return getMedicationPath(uid) + "/" + medicationId;
    }
}
