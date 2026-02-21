package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Represents the statistics for a user's performance in the math problems feature.
 * <p>
 * This class holds the count of correct and incorrect answers for a user.
 * </p>
 */
public class MathProblemsStats implements Serializable {
    private int correctAnswers;
    private int wrongAnswers;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(MathProblemsStats.class).
     * Initializes scores to zero.
     */
    public MathProblemsStats() {
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
    }

    /**
     * Constructs a new MathProblemsStats object with specified scores.
     *
     * @param correctAnswers The number of correct answers.
     * @param wrongAnswers   The number of wrong answers.
     */
    public MathProblemsStats(int correctAnswers, int wrongAnswers) {
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    @NonNull
    @Override
    public String toString() {
        return "MathProblemsStats{" +
                "correctAnswers=" + correctAnswers +
                ", wrongAnswers=" + wrongAnswers +
                '}';
    }
}
