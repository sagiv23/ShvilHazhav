package com.example.sagivproject.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents combined daily statistics for memory game, math problems, and medication compliance.
 */
public class DailyStats implements Serializable {
    private int memoryWins;
    private int memoryGamesPlayed;
    private int mathCorrect;
    private int mathWrong;
    private int medicationsTaken;
    private int medicationsMissed;
    private List<MedicationUsage> medicationUsageLogs;

    public DailyStats() {
        this.memoryWins = 0;
        this.memoryGamesPlayed = 0;
        this.mathCorrect = 0;
        this.mathWrong = 0;
        this.medicationsTaken = 0;
        this.medicationsMissed = 0;
        this.medicationUsageLogs = new ArrayList<>();
    }

    public int getMemoryWins() {
        return memoryWins;
    }

    public void setMemoryWins(int memoryWins) {
        this.memoryWins = memoryWins;
    }

    public int getMemoryGamesPlayed() {
        return memoryGamesPlayed;
    }

    public void setMemoryGamesPlayed(int memoryGamesPlayed) {
        this.memoryGamesPlayed = memoryGamesPlayed;
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

    public List<MedicationUsage> getMedicationUsageLogs() {
        if (medicationUsageLogs == null) {
            medicationUsageLogs = new ArrayList<>();
        }
        return medicationUsageLogs;
    }

    public void setMedicationUsageLogs(List<MedicationUsage> medicationUsageLogs) {
        this.medicationUsageLogs = medicationUsageLogs;
    }

    public void addMemoryWin() {
        this.memoryWins++;
    }

    public void addMemoryGamePlayed() {
        this.memoryGamesPlayed++;
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

    public void addMedicationUsageLog(MedicationUsage log) {
        getMedicationUsageLogs().add(log);
    }
}
