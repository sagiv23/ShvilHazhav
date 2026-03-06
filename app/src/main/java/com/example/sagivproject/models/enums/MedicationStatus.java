package com.example.sagivproject.models.enums;

public enum MedicationStatus {
    TAKEN("נטל"),
    NOT_TAKEN("לא נטל"),
    SNOOZED("ינטול בעתיד");

    private final String displayName;

    MedicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
