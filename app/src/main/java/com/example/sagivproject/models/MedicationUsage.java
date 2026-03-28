package com.example.sagivproject.models;

import com.example.sagivproject.models.enums.MedicationStatus;

import java.io.Serializable;

/**
 * Represents a single instance of medication usage or a scheduled dose status entry.
 * <p>
 * This class tracks when a medication was supposed to be taken (scheduled time),
 * when the user actually performed an action (actual time), and what that action was
 * (e.g., TAKEN, NOT_TAKEN, SNOOZED). It is used for historical logging and compliance tracking.
 * </p>
 */
public class MedicationUsage implements Serializable, Idable {
    private String medicationId;
    private String medicationName;
    /**
     * The actual time the user recorded the status.
     */
    private String time;
    /**
     * The date of the recording in "yyyy-MM-dd" format.
     */
    private String date;
    /**
     * The originally scheduled time for this dose in "HH:mm" format.
     */
    private String scheduledTime;
    /**
     * The intake status of this specific dose.
     */
    private MedicationStatus status;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public MedicationUsage() {
    }

    /**
     * Constructs a new MedicationUsage without a specific scheduled time (legacy/manual log).
     *
     * @param medicationId   The unique ID of the medication.
     * @param medicationName The display name of the medication.
     * @param time           The actual record time.
     * @param date           The record date.
     * @param status         The resulting status.
     */
    public MedicationUsage(String medicationId, String medicationName, String time, String date, MedicationStatus status) {
        this(medicationId, medicationName, time, date, null, status);
    }

    /**
     * Constructs a full MedicationUsage record.
     *
     * @param medicationId   The unique ID of the medication.
     * @param medicationName The display name of the medication.
     * @param time           The actual record time.
     * @param date           The record date.
     * @param scheduledTime  The time this dose was scheduled for.
     * @param status         The resulting status.
     */
    public MedicationUsage(String medicationId, String medicationName, String time, String date, String scheduledTime, MedicationStatus status) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.time = time;
        this.date = date;
        this.scheduledTime = scheduledTime;
        this.status = status;
    }

    /**
     * @return The name of the medication used.
     */
    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
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
     * @return The scheduled time for this dose.
     */
    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
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
        medicationId = id;
    }
}
