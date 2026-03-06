package com.example.sagivproject.models;

import java.io.Serializable;

public class MemoryGameDayStats implements Serializable {
    private int correctAnswers;
    private int wrongAnswers;
    private int wins;

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

    public void addCorrect() {
        this.correctAnswers++;
    }

    public void addWrong() {
        this.wrongAnswers++;
    }

    public void addWin() {
        this.wins++;
    }
}
