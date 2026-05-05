package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Represents a single medication prescription for a user.
 * <p>
 * This class stores all details related to a medication, including its name, type,
 * dosage/description, and a list of reminder times (HH:mm format). It is used to
 * schedule alarms and track intake compliance.
 * </p>
 */
public class Medication implements Idable {
    private String id;
    private String name;
    private String details;
    private MedicationType type;
    private List<String> reminderHours;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Medication() {
    }

    /**
     * Constructs a new Medication object.
     *
     * @param id            The unique ID of the medication record.
     * @param name          The user-friendly name of the medication.
     * @param details       Additional details (e.g., dosage, instructions).
     * @param type          The physical form of the medication (e.g., Pill, Syrup).
     * @param reminderHours A list of strings representing daily reminder times in "HH:mm".
     */
    public Medication(String id, String name, String details, MedicationType type, List<String> reminderHours) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.type = type;
        this.reminderHours = reminderHours;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The name of the medication.
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Dosage or other specific instructions for the user.
     */
    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * @return The {@link MedicationType} enum representing the form.
     */
    public MedicationType getType() {
        return type;
    }

    public void setType(MedicationType type) {
        this.type = type;
    }

    /**
     * @return The list of daily reminder hours in "HH:mm" format.
     */
    public List<String> getReminderHours() {
        return reminderHours;
    }

    public void setReminderHours(List<String> reminderHours) {
        this.reminderHours = reminderHours;
    }

    @NonNull
    @Override
    public String toString() {
        return "Medication{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", details='" + details + '\'' +
                ", type=" + type +
                ", reminderHours=" + reminderHours +
                '}';
    }

    /**
     * Defines the different physical forms or administration types of medication.
     * <p>
     * This enum helps in categorizing medications for better organization and display.
     * Each type includes a Hebrew display name for use in the UI dropdowns.
     * </p>
     */
    public enum MedicationType {
        /**
         * A solid, oral dosage form.
         */
        PILL("כדור"),
        /**
         * Half of a solid pill.
         */
        HALF_TABLET("חצי כדור"),
        /**
         * A liquid medication for oral use.
         */
        SYRUP("סירופ"),
        /**
         * A topical medication.
         */
        CREAM("משחה"),
        /**
         * An injectable medication.
         */
        INJECTION("זריקה"),
        /**
         * A vitamin or mineral supplement.
         */
        VITAMIN("ויטמין"),
        /**
         * Liquid drops for oral administration.
         */
        DROPS_FOR_SWALLOWING("טיפות לבליעה"),
        /**
         * Liquid drops for ocular administration.
         */
        EYE_DROPS("טיפות עיניים"),
        /**
         * Liquid drops for otic administration.
         */
        EAR_DROPS("טיפות אוזניים");

        private final String displayName;

        /**
         * Constructs a new MedicationType.
         *
         * @param displayName The Hebrew display name for the medication type.
         */
        MedicationType(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Gets the user-friendly display name of the medication type.
         *
         * @return The Hebrew display name.
         */
        public String getDisplayName() {
            return displayName;
        }
    }
}