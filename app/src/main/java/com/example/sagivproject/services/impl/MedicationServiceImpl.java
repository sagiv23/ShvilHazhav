package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IMedicationService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
     * Initializes the base service with an empty path as full paths are built dynamically.
     *
     * @param firebaseDatabase The {@link FirebaseDatabase} instance.
     */
    @Inject
    public MedicationServiceImpl(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, "", Medication.class);
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

    /**
     * Updates an existing medication record using a database transaction.
     *
     * @param uid        User identifier.
     * @param medication Updated medication object.
     * @param callback   Result callback.
     */
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

    /**
     * Fetches all usage logs across all dates for a specific user.
     *
     * @param uid      User identifier.
     * @param callback Callback invoked with the full list of logs.
     */
    @Override
    public void getMedicationUsageLogs(@NonNull String uid, @NonNull DatabaseCallback<List<MedicationUsage>> callback) {
        databaseReference.child(USERS_PATH).child(uid).child(FIELD_DAILY_STATS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<MedicationUsage> allLogs = StreamSupport.stream(task.getResult().getChildren().spliterator(), false)
                        .map(daySnapshot -> daySnapshot.getValue(DailyStats.class))
                        .filter(Objects::nonNull)
                        .flatMap(stats -> Objects.requireNonNull(stats).getMedicationUsageLogs().stream())
                        .collect(Collectors.toList());
                callback.onCompleted(allLogs);
            } else {
                callback.onFailed(task.getException());
            }
        });
    }

    /**
     * Resets the usage logs and compliance counters for all dates in a user's history.
     *
     * @param uid      User identifier.
     * @param callback Result callback.
     */
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
     * Constructs path to user's medications.
     */
    private String getMedicationPath(String uid) {
        return USERS_PATH + "/" + uid + "/" + MEDICATIONS_PATH;
    }

    /**
     * Constructs path to specific medication.
     */
    private String getMedicationItemPath(String uid, String medicationId) {
        return getMedicationPath(uid) + "/" + medicationId;
    }
}