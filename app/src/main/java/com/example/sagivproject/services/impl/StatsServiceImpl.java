package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.services.IStatsService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Implementation of the {@link IStatsService} interface.
 * <p>
 * This class handles updating daily statistics for the math game in the Firebase database.
 * It uses atomic transactions to ensure that the correct and wrong answer counters
 * for the current day are incremented correctly and that data remains consistent across
 * multiple updates.
 * </p>
 */
public class StatsServiceImpl implements IStatsService {
    private static final String USERS_PATH = "users";
    private static final String FIELD_DAILY_STATS = "dailyStats";

    private final DatabaseReference databaseReference;

    /**
     * Constructs a new StatsServiceImpl.
     * @param databaseReference The root DatabaseReference injected by Hilt.
     */
    @Inject
    public StatsServiceImpl(DatabaseReference databaseReference) { this.databaseReference = databaseReference.child(USERS_PATH); }

    /**
     * Updates the daily math statistics for a specific user using a Firebase transaction.
     * <p>
     * Increments the 'mathCorrect' counter if {@code correct} is true, or 'mathWrong'
     * if false. The transaction handles the case where the {@link DailyStats} object
     * for the current date doesn't exist yet by creating a new instance.
     * </p>
     * @param uid The unique identifier of the user.
     * @param correct true if the answer was correct, false if it was wrong.
     */
    @Override
    public void updateDailyMathStats(@NonNull String uid, boolean correct) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        databaseReference.child(uid).child(FIELD_DAILY_STATS).child(today).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                DailyStats stats = currentData.getValue(DailyStats.class);
                if (stats == null) stats = new DailyStats();
                if (correct) stats.addMathCorrect();
                else stats.addMathWrong();
                currentData.setValue(stats);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        });
    }
}