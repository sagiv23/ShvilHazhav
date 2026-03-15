package com.example.sagivproject.models;

import androidx.annotation.NonNull;

/**
 * Represents a daily motivational tip or health advice.
 * <p>
 * This model holds the content of the tip and the date it is assigned to.
 * </p>
 */
public class TipOfTheDay implements Idable {
    private String id;
    private String tip;
    private String date;

    /**
     * Default constructor for Firebase.
     */
    public TipOfTheDay() {
    }

    /**
     * Constructs a new TipOfTheDay.
     *
     * @param tip  The content of the tip.
     * @param date The date string (e.g., "yyyy-MM-dd") for this tip.
     */
    public TipOfTheDay(String tip, String date) {
        this.tip = tip;
        this.date = date;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        return "TipOfTheDay{" +
                "id='" + id + '\'' +
                ", tip='" + tip + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
