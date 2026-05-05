package com.example.sagivproject.models;

import androidx.annotation.NonNull;

/**
 * Represents a single instance of medication usage or a scheduled dose status entry.
 * <p>
 * This class tracks when the user actually performed an action (actual time),
 * and what that action was (e.g., TAKEN, NOT_TAKEN, SNOOZED).
 * </p>
 */
public class MedicationUsage implements Idable {
    private String medicationId;
    private String time;
    private String date;
    private MedicationStatus status;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public MedicationUsage() {
    }

    /**
     * Constructs a full MedicationUsage record.
     *
     * @param medicationId The unique ID of the medication.
     * @param time         The actual record time.
     * @param date         The record date.
     * @param status       The resulting status.
     */
    public MedicationUsage(String medicationId, String time, String date, MedicationStatus status) {
        this.medicationId = medicationId;
        this.time = time;
        this.date = date;
        this.status = status;
    }

    /**
     * @return The unique ID of the medication.
     */
    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    /**
     * @return The time of action in "HH:mm".
     */
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    /**
     * @return The date of action in "yyyy-MM-dd".
     */
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return The {@link MedicationStatus} resulting from the action.
     */
    public MedicationStatus getStatus() {
        return status;
    }

    public void setStatus(MedicationStatus status) {
        this.status = status;
    }

    @Override
    public String getId() {
        return medicationId;
    }

    @Override
    public void setId(String id) {
        this.medicationId = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "MedicationUsage{" +
                "medicationId='" + medicationId + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", status=" + status +
                '}';
    }

    /**
     * Defines the possible intake statuses for a scheduled medication dose.
     * <p>
     * This enum is used to track user compliance with their medication schedule.
     * Each status has a user-friendly display name in Hebrew for UI purposes.
     * </p>
     */
    public enum MedicationStatus {
        /**
         * The medication was successfully taken by the user.
         */
        TAKEN("נטל"),
        /**
         * The medication was explicitly marked as not taken.
         */
        NOT_TAKEN("לא נטל"),
        /**
         * The medication intake was postponed to a later time.
         */
        SNOOZED("בהמשך היום");

        private final String displayName;

        /**
         * Constructs a new MedicationStatus.
         *
         * @param displayName The Hebrew display name for the status.
         */
        MedicationStatus(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Gets the user-friendly display name of the status.
         *
         * @return The Hebrew display name.
         */
        public String getDisplayName() {
            return displayName;
        }
    }
}
