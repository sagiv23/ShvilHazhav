package com.example.sagivproject.models.enums;

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
     * @param displayName The Hebrew display name for the status.
     */
    MedicationStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the user-friendly display name of the status.
     * @return The Hebrew display name.
     */
    public String getDisplayName() {
        return displayName;
    }
}