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

public class StatsServiceImpl implements IStatsService {
    private static final String USERS_PATH = "users";
    private static final String FIELD_DAILY_STATS = "dailyStats";

    private final DatabaseReference databaseReference;

    @Inject
    public StatsServiceImpl(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference.child(USERS_PATH);
    }

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
