package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Represents the statistics for a user's performance in the math problems feature.
 * <p>
 * This class holds the count of correct and incorrect answers for a user,
 * along with the last date these stats were updated to facilitate daily resets.
 * </p>
 */
public class MathProblemsStats implements Serializable {
    private int correctAnswers;
    private int wrongAnswers;
    private String lastUpdateDate; // Format: yyyy-MM-dd

    /**
     * Default constructor required for calls to DataSnapshot.getValue(MathProblemsStats.class).
     * Initializes scores to zero.
     */
    public MathProblemsStats() {
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.lastUpdateDate = "";
    }

    /**
     * Constructs a new MathProblemsStats object with specified scores.
     *
     * @param correctAnswers The number of correct answers.
     * @param wrongAnswers   The number of wrong answers.
     * @param lastUpdateDate The date of the last update.
     */
    public MathProblemsStats(int correctAnswers, int wrongAnswers, String lastUpdateDate) {
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.lastUpdateDate = lastUpdateDate;
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

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @NonNull
    @Override
    public String toString() {
        return "MathProblemsStats{" +
                "correctAnswers=" + correctAnswers +
                ", wrongAnswers=" + wrongAnswers +
                ", lastUpdateDate='" + lastUpdateDate + '\'' +
                '}';
    }
}
