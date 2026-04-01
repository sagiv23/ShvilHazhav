package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import com.example.sagivproject.models.enums.MedicationType;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a single medication prescription for a user.
 * <p>
 * This class stores all details related to a medication, including its name, type,
 * dosage/description, and a list of reminder times (HH:mm format). It is used to
 * schedule alarms and track intake compliance.
 * </p>
 */
public class Medication implements Serializable, Idable {
    private String id;
    private String userId;
    private String name;
    private String details;
    private MedicationType type;
    private List<String> reminderHours;

    /** Default constructor required for Firebase deserialization. */
    public Medication() {
    }

    /**
     * Constructs a new Medication object.
     * @param id The unique ID of the medication record.
     * @param name The user-friendly name of the medication.
     * @param details Additional details (e.g., dosage, instructions).
     * @param type The physical form of the medication (e.g., Pill, Syrup).
     * @param reminderHours A list of strings representing daily reminder times in "HH:mm".
     * @param userId The ID of the user who owns this medication record.
     */
    public Medication(String id, String name, String details, MedicationType type, List<String> reminderHours, String userId) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.type = type;
        this.reminderHours = reminderHours;
        this.userId = userId;
    }

    @Override
    public String getId() { return this.id; }

    @Override
    public void setId(String id) { this.id = id; }

    /** @return The UID of the user who owns this medication. */
    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    /** @return The name of the medication. */
    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    /** @return Dosage or other specific instructions for the user. */
    public String getDetails() { return this.details; }

    public void setDetails(String details) { this.details = details; }

    /** @return The {@link MedicationType} enum representing the form. */
    public MedicationType getType() { return type; }

    public void setType(MedicationType type) { this.type = type; }

    /** @return The list of daily reminder hours in "HH:mm" format. */
    public List<String> getReminderHours() { return reminderHours; }

    public void setReminderHours(List<String> reminderHours) { this.reminderHours = reminderHours; }

    @NonNull
    @Override
    public String toString() {
        return "Medication{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}