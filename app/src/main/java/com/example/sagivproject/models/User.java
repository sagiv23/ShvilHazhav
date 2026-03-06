package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import com.example.sagivproject.models.enums.UserRole;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a user of the application.
 */
public class User implements Serializable, Idable {
    private static final int YEAR = Calendar.YEAR;
    private static final int DAY_OF_YEAR = Calendar.DAY_OF_YEAR;
    private String id;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
    private long birthDateMillis;
    private String password;
    private String profileImage;
    private HashMap<String, Medication> medications;
    private HashMap<String, DailyStats> dailyStats;

    public User() {
        this.role = UserRole.REGULAR;
        this.dailyStats = new HashMap<>();
    }

    public User(String id, String firstName, String lastName, long birthDateMillis, String email, String password, UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDateMillis = birthDateMillis;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profileImage = null;
        this.medications = new HashMap<>();
        this.dailyStats = new HashMap<>();
    }

    public User(User other) {
        if (other == null) return;
        this.id = other.id;
        this.email = other.email;
        this.role = other.role;
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.birthDateMillis = other.birthDateMillis;
        this.password = other.password;
        this.profileImage = other.profileImage;
        if (other.medications != null) {
            this.medications = new HashMap<>(other.medications);
        }
        if (other.dailyStats != null) {
            this.dailyStats = new HashMap<>(other.dailyStats);
        } else {
            this.dailyStats = new HashMap<>();
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getBirthDateMillis() {
        return this.birthDateMillis;
    }

    public void setBirthDateMillis(long birthDateMillis) {
        this.birthDateMillis = birthDateMillis;
    }

    @Exclude
    public int getAge() {
        Calendar birth = Calendar.getInstance();
        birth.setTimeInMillis(birthDateMillis);
        Calendar today = Calendar.getInstance();
        int age = today.get(YEAR) - birth.get(YEAR);
        if (today.get(DAY_OF_YEAR) < birth.get(DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role == null ? UserRole.REGULAR : role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Exclude
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public HashMap<String, Medication> getMedications() {
        return this.medications;
    }

    public void setMedications(HashMap<String, Medication> medications) {
        this.medications = medications;
    }

    public HashMap<String, DailyStats> getDailyStats() {
        if (dailyStats == null) dailyStats = new HashMap<>();
        return dailyStats;
    }

    public void setDailyStats(HashMap<String, DailyStats> dailyStats) {
        this.dailyStats = dailyStats;
    }

    @Exclude
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return birthDateMillis == user.birthDateMillis &&
                Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                role == user.role &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, role, firstName, lastName, birthDateMillis);
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" + "id='" + id + '\'' + ", email='" + email + '\'' + ", role=" + role + ", fullName='" + getFullName() + '\'' + '}';
    }
}
