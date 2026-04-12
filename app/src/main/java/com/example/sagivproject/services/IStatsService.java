package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

/**
 * An interface that defines the contract for operations related to tracking user activity statistics.
 * <p>
 * This service is responsible for updating performance metrics across different application modules,
 * such as correct/wrong answer counts in the math challenge. It ensures statistics are tracked
 * on a daily basis.
 * </p>
 */
public interface IStatsService {
    /**
     * Updates the daily math statistics for a specific user.
     * <p>
     * This method increments either the 'correct' or 'wrong' counter for the current calendar date
     * using an atomic transaction to ensure data consistency.
     * </p>
     *
     * @param uid     The unique identifier of the user.
     * @param correct true if the user's answer was correct, false otherwise.
     */
    void updateDailyMathStats(@NonNull String uid, boolean correct);

    /**
     * Updates the daily memory statistics for a specific user.
     * <p>
     * This method increments the 'memoryGamesPlayed' counter and the 'memoryWins' counter
     * if the user won the game for the current calendar date using an atomic transaction.
     * </p>
     *
     * @param uid   The unique identifier of the user.
     * @param isWin true if the user won the game, false otherwise.
     */
    void updateDailyMemoryStats(@NonNull String uid, boolean isWin);

    /**
     * Logs a medication intake event and updates the daily performance statistics.
     *
     * @param uid      User identifier.
     * @param usage    Usage record details.
     * @param callback Result callback.
     */
    void logMedicationUsage(@NonNull String uid, @NonNull MedicationUsage usage, @Nullable DatabaseCallback<Void> callback);
}