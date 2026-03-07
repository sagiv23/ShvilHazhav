package com.example.sagivproject.services;

import androidx.annotation.NonNull;

/**
 * An interface that defines the contract for operations related to tracking user statistics.
 * <p>
 * This service is responsible for updating daily statistics based on the user's activity
 * in various modules of the application, such as the math game.
 * </p>
 */
public interface IStatsService {
    /**
     * Updates the daily math statistics for a specific user.
     * <p>
     * This method increments the count of correct or wrong answers for the current date.
     * </p>
     *
     * @param uid     The unique ID of the user.
     * @param correct True if the answer was correct, false if it was wrong.
     */
    void updateDailyMathStats(@NonNull String uid, boolean correct);
}
