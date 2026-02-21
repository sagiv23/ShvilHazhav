package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import com.example.sagivproject.models.enums.MedicationType;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a single medication for a user.
 * <p>
 * This class holds all the information about a medication, including its name, type,
 * dosage details, and the list of reminder times.
 * </p>
 */
public class Medication implements Serializable, Idable {
    private String id;
    private String userId;
    private String name;
    private String details;
    private MedicationType type;
    private List<String> reminderHours;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Medication.class).
     */
    public Medication() {
    }

    /**
     * Constructs a new Medication object.
     *
     * @param id            The unique ID of the medication.
     * @param name          The name of the medication.
     * @param details       Dosage or other details about the medication.
     * @param type          The type of medication (e.g., Pill, Syrup).
     * @param reminderHours A list of reminder times in "HH:mm" format.
     * @param userId        The ID of the user to whom this medication belongs.
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
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public MedicationType getType() {
        return type;
    }

    public void setType(MedicationType type) {
        this.type = type;
    }

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
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", details='" + details + '\'' +
                ", type=" + type +
                ", reminderHours=" + reminderHours +
                '}';
    }
}
