package com.example.sagivproject.services;

import androidx.annotation.NonNull;

/**
 * An interface that defines the contract for operations related to user statistics.
 */
public interface IStatsService {
    /**
     * Increments daily math statistics for a user.
     *
     * @param uid     The user's ID.
     * @param correct True if the answer was correct, false otherwise.
     */
    void updateDailyMathStats(@NonNull String uid, boolean correct);
}
