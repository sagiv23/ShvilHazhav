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
     * A generic helper method to increment a numeric statistic for a user using a transaction.
     *
     * @param uid The user's ID.
     * @param key The specific statistic key to increment (e.g., "correctAnswers").
     */
    private void addAnswer(String uid, String key) {
        if (uid == null) {
            return;
        }
        databaseReference.child(uid).child("mathProblemsStats").child(key).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                if (current == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(current + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // Transaction completed. Can add logging here if needed.
            }
        });
    }

    /**
     * Increments the count of correct answers for a user.
     *
     * @param uid The user's ID.
     */
    @Override
    public void addCorrectAnswer(String uid) {
        addAnswer(uid, "correctAnswers");
    }

    /**
     * Increments the count of wrong answers for a user.
     *
     * @param uid The user's ID.
     */
    @Override
    public void addWrongAnswer(String uid) {
        addAnswer(uid, "wrongAnswers");
    }

    /**
     * Resets the math problem statistics (correct and wrong answers) for a user to zero.
     *
     * @param uid The user's ID.
     */
    @Override
    public void resetMathStats(@NonNull String uid) {
        databaseReference.child(uid).child("mathProblemsStats").child("correctAnswers").setValue(0);
        databaseReference.child(uid).child("mathProblemsStats").child("wrongAnswers").setValue(0);
    }
}
