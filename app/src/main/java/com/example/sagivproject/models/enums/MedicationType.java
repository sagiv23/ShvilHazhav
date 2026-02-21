package com.example.sagivproject.models.enums;

/**
 * Defines the different types or forms of medication.
 * <p>
 * Each type has a user-friendly display name in Hebrew.
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
     * A liquid medication.
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
     * A vitamin supplement.
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
