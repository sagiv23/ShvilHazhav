package com.example.sagivproject.services;

import androidx.annotation.NonNull;

/**
 * An interface that defines the contract for operations related to user statistics.
 * <p>
 * This service currently focuses on statistics for the math problems feature.
 * </p>
 */
public interface IStatsService {
    /**
     * Increments the count of correct answers for a user's math problems statistics.
     *
     * @param uid The UID of the user.
     */
    void addCorrectAnswer(String uid);

    /**
     * Increments the count of wrong answers for a user's math problems statistics.
     *
     * @param uid The UID of the user.
     */
    void addWrongAnswer(String uid);

    /**
     * Resets the math problems statistics (correct and wrong answers) for a user to zero.
     *
     * @param uid The UID of the user whose stats are to be reset.
     */
    void resetMathStats(@NonNull String uid);
}
