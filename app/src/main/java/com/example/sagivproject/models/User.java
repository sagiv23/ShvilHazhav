package com.example.sagivproject.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.HashMap;

public class User implements Serializable {
    private String uid;
    private String email;
    private boolean isAdmin;
    private String firstName;
    private String lastName;
    private String password;
    private String profileImage;
    private HashMap<String, Medication> medications;
    private int count_wins;

    public User() { }

    public User(String uid, String firstName, String lastName, String email, String password, boolean isAdmin, String profileImage, HashMap<String, Medication> medications, int count_wins) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.profileImage = profileImage;
        this.medications = medications;
        this.count_wins = count_wins;
    }

    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public boolean getIsAdmin() { return this.isAdmin; }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }

    public void toggleAdminStatus() { this.isAdmin = !this.isAdmin; }

    public String getUid() { return this.uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public HashMap<String, Medication> getMedications() { return this.medications; }
    public void setMedications(HashMap<String, Medication> medications) { this.medications = medications; }

    public int getCountWins() { return this.count_wins; }
    public void setCountWins(int count_wins) { this.count_wins = count_wins; }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uid);
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", medications=" + medications +
                ", count_wins=" + count_wins +
                '}';
    }
}
