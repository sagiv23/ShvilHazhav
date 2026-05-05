package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import com.example.sagivproject.models.MedicationUsage.MedicationStatus;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents combined daily statistics for a user.
 * <p>
 * This class tracks performance metrics for cognitive activities (memory game, math problems)
 * and medication compliance (taken vs missed) for a single calendar day.
 * It is used to generate graphs and insights for users and administrators.
 * </p>
 */
public class DailyStats implements Idable {
    private String id;
    private int memoryWins;
    private int memoryGamesPlayed;
    private int mathCorrect;
    private int mathWrong;
    private List<MedicationUsage> medicationUsageLogs;

    /**
     * Constructs a new DailyStats object with all counters initialized to zero.
     */
    public DailyStats() {
        this.memoryWins = 0;
        this.memoryGamesPlayed = 0;
        this.mathCorrect = 0;
        this.mathWrong = 0;
        this.medicationUsageLogs = new ArrayList<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Total number of memory games won on this day.
     */
    public int getMemoryWins() {
        return memoryWins;
    }

    public void setMemoryWins(int memoryWins) {
        this.memoryWins = memoryWins;
    }

    /**
     * @return Total number of memory games played on this day.
     */
    public int getMemoryGamesPlayed() {
        return memoryGamesPlayed;
    }

    public void setMemoryGamesPlayed(int memoryGamesPlayed) {
        this.memoryGamesPlayed = memoryGamesPlayed;
    }

    /**
     * @return Number of correct math problem answers on this day.
     */
    public int getMathCorrect() {
        return mathCorrect;
    }

    public void setMathCorrect(int mathCorrect) {
        this.mathCorrect = mathCorrect;
    }

    /**
     * @return Number of incorrect math problem answers on this day.
     */
    public int getMathWrong() {
        return mathWrong;
    }

    public void setMathWrong(int mathWrong) {
        this.mathWrong = mathWrong;
    }

    /**
     * Calculates the total count of medication doses marked as TAKEN on this day from the logs.
     * Annotated with {@code @Exclude} to prevent redundant storage in Firebase.
     *
     * @return The count of taken medications.
     */
    @Exclude
    public int getMedicationsTaken() {
        if (medicationUsageLogs == null) return 0;
        int count = 0;
        for (MedicationUsage log : medicationUsageLogs) {
            if (log.getStatus() == MedicationStatus.TAKEN) count++;
        }
        return count;
    }

    /**
     * Calculates the total count of medication doses marked as NOT_TAKEN on this day from the logs.
     * Annotated with {@code @Exclude} to prevent redundant storage in Firebase.
     *
     * @return The count of missed medications.
     */
    @Exclude
    public int getMedicationsMissed() {
        if (medicationUsageLogs == null) return 0;
        int count = 0;
        for (MedicationUsage log : medicationUsageLogs) {
            if (log.getStatus() == MedicationStatus.NOT_TAKEN) count++;
        }
        return count;
    }

    /**
     * Gets the list of detailed medication usage logs for the day.
     *
     * @return A list of {@link MedicationUsage} objects.
     */
    public List<MedicationUsage> getMedicationUsageLogs() {
        if (medicationUsageLogs == null) {
            medicationUsageLogs = new ArrayList<>();
        }
        return medicationUsageLogs;
    }

    public void setMedicationUsageLogs(List<MedicationUsage> medicationUsageLogs) {
        this.medicationUsageLogs = medicationUsageLogs;
    }

    /**
     * Increments the count of memory game wins.
     */
    public void addMemoryWin() {
        this.memoryWins++;
    }

    /**
     * Increments the count of memory games played.
     */
    public void addMemoryGamePlayed() {
        this.memoryGamesPlayed++;
    }

    /**
     * Increments the count of correct math answers.
     */
    public void addMathCorrect() {
        this.mathCorrect++;
    }

    /**
     * Increments the count of incorrect math answers.
     */
    public void addMathWrong() {
        this.mathWrong++;
    }

    /**
     * Adds a detailed medication usage log entry to this day's statistics.
     *
     * @param log The {@link MedicationUsage} record to append.
     */
    public void addMedicationUsageLog(MedicationUsage log) {
        getMedicationUsageLogs().add(log);
    }

    @NonNull
    @Override
    public String toString() {
        return "DailyStats{" +
                "memoryWins=" + memoryWins +
                ", memoryGamesPlayed=" + memoryGamesPlayed +
                ", mathCorrect=" + mathCorrect +
                ", mathWrong=" + mathWrong +
                ", medicationsTaken=" + getMedicationsTaken() +
                ", medicationsMissed=" + getMedicationsMissed() +
                ", medicationUsageLogs=" + medicationUsageLogs +
                '}';
    }
}
