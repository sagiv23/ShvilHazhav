package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IStatsService;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Implementation of the {@link IStatsService} interface.
 * <p>
 * This class handles updating daily statistics for the math game and memory game in the Firebase database.
 * It inherits from {@link BaseDatabaseService} to leverage common database operations and ensure
 * consistency across services.
 * </p>
 */
public class StatsServiceImpl extends BaseDatabaseService<DailyStats> implements IStatsService {
    private static final String USERS_PATH = "users";
    private static final String FIELD_DAILY_STATS = "dailyStats";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Constructs a new StatsServiceImpl.
     *
     * @param firebaseDatabase The FirebaseDatabase instance injected by Hilt.
     */
    @Inject
    public StatsServiceImpl(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, USERS_PATH, DailyStats.class);
    }

    /**
     * Generates the database path for a user's daily stats.
     *
     * @param uid  The user's unique identifier.
     * @param date The date string in "yyyy-MM-dd" format.
     * @return The database path string.
     */
    private String getStatsPath(String uid, String date) {
        return USERS_PATH + "/" + uid + "/" + FIELD_DAILY_STATS + "/" + date;
    }

    /**
     * Gets today's date string in the "yyyy-MM-dd" format.
     *
     * @return Today's date string.
     */
    private String getTodayDate() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
    }

    /**
     * Updates the daily math game statistics for a specific user.
     * If no statistics exist for today, a new entry is created.
     *
     * @param uid     The user's unique identifier.
     * @param correct True if the answer was correct, false otherwise.
     */
    @Override
    public void updateDailyMathStats(@NonNull String uid, boolean correct) {
        String today = getTodayDate();
        runTransaction(getStatsPath(uid, today), stats -> {
            DailyStats currentStats = (stats != null) ? stats : new DailyStats();
            currentStats.setId(today);
            if (correct) currentStats.addMathCorrect();
            else currentStats.addMathWrong();
            return currentStats;
        }, null);
    }

    /**
     * Updates the daily memory game statistics for a specific user.
     * Increments the games played count and win count if applicable.
     *
     * @param uid   The user's unique identifier.
     * @param isWin True if the user won the game, false otherwise.
     */
    @Override
    public void updateDailyMemoryStats(@NonNull String uid, boolean isWin) {
        String today = getTodayDate();
        runTransaction(getStatsPath(uid, today), stats -> {
            DailyStats currentStats = (stats != null) ? stats : new DailyStats();
            currentStats.setId(today);
            currentStats.addMemoryGamePlayed();
            if (isWin) {
                currentStats.addMemoryWin();
            }
            return currentStats;
        }, null);
    }

    /**
     * Logs medication usage for a specific user and updates daily statistics.
     * Tracks medication taken or missed and adds the usage log entry.
     *
     * @param uid      The user's unique identifier.
     * @param usage    The medication usage information to log.
     * @param callback Optional callback to be notified when the operation completes or fails.
     */
    @Override
    public void logMedicationUsage(@NonNull String uid, @NonNull MedicationUsage usage, @Nullable DatabaseCallback<Void> callback) {
        runTransaction(getStatsPath(uid, usage.getDate()), stats -> {
            DailyStats currentStats = (stats != null) ? stats : new DailyStats();
            currentStats.setId(usage.getDate());

            if (usage.getStatus() == MedicationStatus.TAKEN) {
                currentStats.addMedicationTaken();
            } else if (usage.getStatus() == MedicationStatus.NOT_TAKEN) {
                currentStats.addMedicationMissed();
            }

            currentStats.addMedicationUsageLog(usage);
            return currentStats;
        }, new DatabaseCallback<>() {
            @Override
            public void onCompleted(DailyStats result) {
                if (callback != null) {
                    callback.onCompleted(null);
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