package com.example.sagivproject.models.enums;

/**
 * Defines the possible intake statuses for a scheduled medication dose.
 */
public enum MedicationStatus {
    /**
     * The medication was taken by the user.
     */
    TAKEN("נטל"),
    /**
     * The medication was not taken by the user.
     */
    NOT_TAKEN("לא נטל"),
    /**
     * The medication intake was postponed to later in the day.
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
