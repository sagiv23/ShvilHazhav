package com.example.sagivproject.models;

import androidx.annotation.NonNull;

public class TipOfTheDay implements Idable {
    private String id;
    private String tip;
    private String date;

    public TipOfTheDay() {
    }

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
