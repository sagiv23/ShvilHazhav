package com.example.sagivproject.models;

import com.example.sagivproject.models.enums.MedicationStatus;

import java.io.Serializable;

public class MedicationUsage implements Serializable, Idable {
    private String medicationId;
    private String medicationName;
    private String time; // Actual time taken
    private String date; // yyyy-MM-dd
    private String scheduledTime; // The time this was scheduled for (HH:mm)
    private MedicationStatus status;

    public MedicationUsage() {
    }

    public MedicationUsage(String medicationId, String medicationName, String time, String date, MedicationStatus status) {
        this(medicationId, medicationName, time, date, null, status);
    }

    public MedicationUsage(String medicationId, String medicationName, String time, String date, String scheduledTime, MedicationStatus status) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.time = time;
        this.date = date;
        this.scheduledTime = scheduledTime;
        this.status = status;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

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
