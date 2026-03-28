package com.example.sagivproject.models.enums;

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
