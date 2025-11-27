package com.example.sagivproject.models;

import java.util.Date;

public class Medication {
    private String id;
    private String name;
    private String details;
    private Date date;

    public Medication() {}

    public Medication(String name, String details, Date date, String uid) {
        this.name = name;
        this.details = details;
        this.date = date;
        this.id = uid;
    }

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getDetails() { return this.details; }
    public void setDetails(String details) { this.details = details; }
    public Date getDate() { return this.date; }
    public void setDate(Date date) { this.date = date; }
}