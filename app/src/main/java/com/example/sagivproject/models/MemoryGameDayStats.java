package com.example.sagivproject.models;

import java.io.Serializable;

/**
 * Represents daily statistics specific to the online memory game.
 * <p>
 * This class tracks correct and wrong moves made by the user, as well as the
 * total number of games won during a single day.
 * </p>
 */
public class MemoryGameDayStats implements Serializable {
    private int correctAnswers;
    private int wrongAnswers;
    private int wins;

    /**
     * Constructs a new MemoryGameDayStats object with all counters initialized to zero.
     */
    public MemoryGameDayStats() {
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.wins = 0;
    }

    /**
     * @return Number of correct card matches found today.
     */
    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    /**
     * @return Number of failed card match attempts today.
     */
    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    /**
     * @return Total number of memory games won today.
     */
    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    /**
     * Increments the count of correct card matches.
     */
    public void addCorrect() {
        this.correctAnswers++;
    }

    /**
     * Increments the count of wrong card match attempts.
     */
    public void addWrong() {
        this.wrongAnswers++;
    }

    /**
     * Increments the count of total game wins.
     */
    public void addWin() {
        this.wins++;
    }
}
