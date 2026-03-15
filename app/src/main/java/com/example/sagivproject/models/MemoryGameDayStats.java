package com.example.sagivproject.models;

import java.io.Serializable;

/**
 * Represents daily statistics specifically for the memory game.
 * <p>
 * This class tracks correct/wrong moves and total wins for a single day.
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

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    /**
     * Increments the correct answers counter.
     */
    public void addCorrect() {
        this.correctAnswers++;
    }

    /**
     * Increments the wrong answers counter.
     */
    public void addWrong() {
        this.wrongAnswers++;
    }

    /**
     * Increments the wins counter.
     */
    public void addWin() {
        this.wins++;
    }
}
