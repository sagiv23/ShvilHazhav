package com.example.sagivproject.models;

import java.io.Serializable;

/**
 * Represents combined daily statistics for memory game, math problems, and medication compliance.
 */
public class DailyStats implements Serializable {
    private int memoryWins;
    private int mathCorrect;
    private int mathWrong;
    private int medicationsTaken;
    private int medicationsMissed;

    public DailyStats() {
        this.memoryWins = 0;
        this.mathCorrect = 0;
        this.mathWrong = 0;
        this.medicationsTaken = 0;
        this.medicationsMissed = 0;
    }

    public int getMemoryWins() {
        return memoryWins;
    }

    public void setMemoryWins(int memoryWins) {
        this.memoryWins = memoryWins;
    }

    public int getMathCorrect() {
        return mathCorrect;
    }

    public void setMathCorrect(int mathCorrect) {
        this.mathCorrect = mathCorrect;
    }

    public int getMathWrong() {
        return mathWrong;
    }

    public void setMathWrong(int mathWrong) {
        this.mathWrong = mathWrong;
    }

    public int getMedicationsTaken() {
        return medicationsTaken;
    }

    public void setMedicationsTaken(int medicationsTaken) {
        this.medicationsTaken = medicationsTaken;
    }

    public int getMedicationsMissed() {
        return medicationsMissed;
    }

    public void setMedicationsMissed(int medicationsMissed) {
        this.medicationsMissed = medicationsMissed;
    }

    public void addMemoryWin() {
        this.memoryWins++;
    }

    public void addMathCorrect() {
        this.mathCorrect++;
    }

    public void addMathWrong() {
        this.mathWrong++;
    }

    public void addMedicationTaken() {
        this.medicationsTaken++;
    }

    public void addMedicationMissed() {
        this.medicationsMissed++;
    }
}
