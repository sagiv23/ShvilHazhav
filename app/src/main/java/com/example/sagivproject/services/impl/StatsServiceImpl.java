package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.services.IStatsService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import javax.inject.Inject;

/**
 * An implementation of the {@link IStatsService} interface.
 * <p>
 * This service manages user statistics, specifically for the math problems feature.
 * It handles incrementing correct/wrong answer counts and resetting the stats for a user.
 * </p>
 */
public class StatsServiceImpl implements IStatsService {
    private static final String USERS_PATH = "users";
    private static final String FIELD_MATH_PROBLEMS_STATS = "mathProblemsStats";
    private static final String FIELD_CORRECT_ANSWERS = "correctAnswers";
    private static final String FIELD_WRONG_ANSWERS = "wrongAnswers";

    private final DatabaseReference databaseReference;

    /**
     * Constructs a new StatsServiceImpl.
     *
     * @param databaseReference The root database reference.
     */
    @Inject
    public StatsServiceImpl(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference.child(USERS_PATH);
    }

    /**
     * Increments the count of correct answers for a user.
     *
     * @param uid The user's ID.
     */
    @Override
    public void addCorrectAnswer(String uid) {
        addAnswer(uid, FIELD_CORRECT_ANSWERS);
    }

    /**
     * Increments the count of wrong answers for a user.
     *
     * @param uid The user's ID.
     */
    @Override
    public void addWrongAnswer(String uid) {
        addAnswer(uid, FIELD_WRONG_ANSWERS);
    }

    /**
     * Resets the math problem statistics (correct and wrong answers) for a user to zero.
     *
     * @param uid The user's ID.
     */
    @Override
    public void resetMathStats(@NonNull String uid) {
        DatabaseReference statsRef = databaseReference.child(uid).child(FIELD_MATH_PROBLEMS_STATS);
        statsRef.child(FIELD_CORRECT_ANSWERS).setValue(0);
        statsRef.child(FIELD_WRONG_ANSWERS).setValue(0);
    }

    /**
     * A generic helper method to increment a numeric statistic for a user using a transaction.
     *
     * @param uid The user's ID.
     * @param key The specific statistic key to increment (e.g., "correctAnswers").
     */
    private void addAnswer(String uid, String key) {
        if (uid == null) {
            return;
        }

        databaseReference.child(uid).child(FIELD_MATH_PROBLEMS_STATS).child(key).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                currentData.setValue(current == null ? 1 : current + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // Transaction completed. Can add logging here if needed.
            }
        });
    }
}
