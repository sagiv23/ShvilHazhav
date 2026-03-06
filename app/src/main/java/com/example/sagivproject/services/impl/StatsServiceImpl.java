package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.MemoryGameDayStats;
import com.example.sagivproject.services.IStatsService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

/**
 * An implementation of the {@link IStatsService} interface.
 */
public class StatsServiceImpl implements IStatsService {
    private static final String USERS_PATH = "users";
    private static final String FIELD_MATH_PROBLEMS_STATS = "mathProblemsStats";
    private static final String FIELD_CORRECT_ANSWERS = "correctAnswers";
    private static final String FIELD_WRONG_ANSWERS = "wrongAnswers";
    private static final String FIELD_LAST_UPDATE_DATE = "lastUpdateDate";
    private static final String FIELD_MATH_PROBLEMS_DAY_STATS = "mathProblemsDayStats";

    private final DatabaseReference databaseReference;

    @Inject
    public StatsServiceImpl(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference.child(USERS_PATH);
    }

    @Override
    public void addCorrectAnswer(String uid) {
        addAnswer(uid, FIELD_CORRECT_ANSWERS);
    }

    @Override
    public void addWrongAnswer(String uid) {
        addAnswer(uid, FIELD_WRONG_ANSWERS);
    }

    @Override
    public void resetMathStats(@NonNull String uid, @NonNull String date) {
        DatabaseReference statsRef = databaseReference.child(uid).child(FIELD_MATH_PROBLEMS_STATS);
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_CORRECT_ANSWERS, 0);
        updates.put(FIELD_WRONG_ANSWERS, 0);
        updates.put(FIELD_LAST_UPDATE_DATE, date);
        statsRef.updateChildren(updates);
    }

    @Override
    public void updateDailyMathStats(@NonNull String uid, boolean correct) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        databaseReference.child(uid).child(FIELD_MATH_PROBLEMS_DAY_STATS).child(today).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                MemoryGameDayStats stats = currentData.getValue(MemoryGameDayStats.class);
                if (stats == null) stats = new MemoryGameDayStats();
                if (correct) stats.addCorrect();
                else stats.addWrong();
                currentData.setValue(stats);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
            }
        });
    }

    private void addAnswer(String uid, String key) {
        if (uid == null) return;
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
            }
        });
    }
}
