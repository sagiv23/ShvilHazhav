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
    private int countWins;
    private MathProblemsStats mathProblemsStats;
    private HashMap<String, MemoryGameDayStats> memoryGameDayStats; // Date string -> Stats
    private HashMap<String, MemoryGameDayStats> mathProblemsDayStats; // Date string -> Stats

    public User() {
        this.role = UserRole.REGULAR;
        this.memoryGameDayStats = new HashMap<>();
        this.mathProblemsDayStats = new HashMap<>();
    }

    public User(String id, String firstName, String lastName, long birthDateMillis, String email, String password, UserRole role, String profileImage, HashMap<String, Medication> medications) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDateMillis = birthDateMillis;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profileImage = profileImage;
        this.medications = medications;
        this.countWins = 0;
        this.mathProblemsStats = new MathProblemsStats();
        this.memoryGameDayStats = new HashMap<>();
        this.mathProblemsDayStats = new HashMap<>();
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
        this.countWins = other.countWins;
        if (other.medications != null) {
            this.medications = new HashMap<>(other.medications);
        }
        if (other.mathProblemsStats != null) {
            this.mathProblemsStats = new MathProblemsStats(other.mathProblemsStats.getCorrectAnswers(), other.mathProblemsStats.getWrongAnswers(), other.mathProblemsStats.getLastUpdateDate());
        }
        if (other.memoryGameDayStats != null) {
            this.memoryGameDayStats = new HashMap<>(other.memoryGameDayStats);
        } else {
            this.memoryGameDayStats = new HashMap<>();
        }
        if (other.mathProblemsDayStats != null) {
            this.mathProblemsDayStats = new HashMap<>(other.mathProblemsDayStats);
        } else {
            this.mathProblemsDayStats = new HashMap<>();
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

    public int getCountWins() {
        return this.countWins;
    }

    public void setCountWins(int countWins) {
        this.countWins = countWins;
    }

    public MathProblemsStats getMathProblemsStats() {
        if (mathProblemsStats == null) mathProblemsStats = new MathProblemsStats();
        return mathProblemsStats;
    }

    public void setMathProblemsStats(MathProblemsStats mathProblemsStats) {
        this.mathProblemsStats = mathProblemsStats;
    }

    public HashMap<String, MemoryGameDayStats> getMemoryGameDayStats() {
        if (memoryGameDayStats == null) memoryGameDayStats = new HashMap<>();
        return memoryGameDayStats;
    }

    public void setMemoryGameDayStats(HashMap<String, MemoryGameDayStats> memoryGameDayStats) {
        this.memoryGameDayStats = memoryGameDayStats;
    }

    public HashMap<String, MemoryGameDayStats> getMathProblemsDayStats() {
        if (mathProblemsDayStats == null) mathProblemsDayStats = new HashMap<>();
        return mathProblemsDayStats;
    }

    public void setMathProblemsDayStats(HashMap<String, MemoryGameDayStats> mathProblemsDayStats) {
        this.mathProblemsDayStats = mathProblemsDayStats;
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
                countWins == user.countWins &&
                Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                role == user.role &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, role, firstName, lastName, birthDateMillis, countWins);
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" + "id='" + id + '\'' + ", email='" + email + '\'' + ", role=" + role + ", fullName='" + getFullName() + '\'' + '}';
    }
}
