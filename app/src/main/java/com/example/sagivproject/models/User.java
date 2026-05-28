package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a user of the application.
 * <p>
 * This class holds comprehensive personal information, account role (Admin/Regular),
 * medication schedules, activity statistics, and emergency contacts.
 * It is the central data model for authentication and personalization.
 * </p>
 */
public class User implements Idable {
    private static final int YEAR = Calendar.YEAR;

    /**
     * Unique identifier for the user (UID from Firebase).
     */
    private String id;

    /**
     * User's unique email address used for login.
     */
    private String email;

    /**
     * The administrative or regular role assigned to the user.
     */
    private UserRole role;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's birthdate in ISO format (yyyy-MM-dd).
     */
    private String birthDate;

    /**
     * User's account password.
     */
    private String password;

    /**
     * Base64 encoded string of the user's profile image.
     */
    private String profileImage;

    /**
     * Collection of the user's medications, indexed by medication ID.
     */
    private HashMap<String, Medication> medications;

    /**
     * Collection of performance and compliance metrics, indexed by date string.
     */
    private HashMap<String, DailyStats> dailyStats;

    /**
     * Collection of the user's emergency contacts, indexed by contact ID.
     */
    private HashMap<String, EmergencyContact> emergencyContacts;

    /**
     * Default constructor required for Firebase deserialization.
     * Initializes role to REGULAR and maps to empty HashMaps.
     */
    public User() {
        this.role = UserRole.REGULAR;
        this.dailyStats = new HashMap<>();
        this.emergencyContacts = new HashMap<>();
    }

    /**
     * Constructs a new User with full details.
     *
     * @param id        The unique identifier for the user.
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param birthDate The user's birthdate (ISO format).
     * @param email     The user's email address.
     * @param password  The user's password.
     * @param role      The user's role (ADMIN or REGULAR).
     */
    public User(String id, String firstName, String lastName, String birthDate, String email, String password, UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profileImage = null;
        this.medications = new HashMap<>();
        this.dailyStats = new HashMap<>();
        this.emergencyContacts = new HashMap<>();
    }

    /**
     * Copy constructor.
     *
     * @param other The user object to copy from.
     */
    public User(User other) {
        if (other == null) return;
        this.id = other.id;
        this.email = other.email;
        this.role = other.role;
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.birthDate = other.birthDate;
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
        if (other.emergencyContacts != null) {
            this.emergencyContacts = new HashMap<>(other.emergencyContacts);
        } else {
            this.emergencyContacts = new HashMap<>();
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

    /**
     * @return The user's first name.
     */
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return The user's last name.
     */
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return The user's birthdate in "yyyy-MM-dd" format.
     */
    public String getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Calculates the user's age based on their birthdate.
     * Annotated with {@code @Exclude} to prevent storage in Firebase.
     *
     * @return The user's current age in years, or -1 if birthdate is invalid.
     */
    @Exclude
    public int getAge() {
        if (birthDate == null || birthDate.length() < 10) return -1;
        try {
            int year = Integer.parseInt(birthDate.substring(0, 4));
            int month = Integer.parseInt(birthDate.substring(5, 7)) - 1; // 0-based
            int day = Integer.parseInt(birthDate.substring(8, 10));

            Calendar birth = Calendar.getInstance();
            birth.set(year, month, day);
            Calendar today = Calendar.getInstance();
            int age = today.get(YEAR) - birth.get(YEAR);

            if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }
            return age;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * @return The user's email address.
     */
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The user's password.
     */
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return The user's role, defaults to REGULAR if null.
     */
    public UserRole getRole() {
        return role == null ? UserRole.REGULAR : role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Checks if the user has an administrative role.
     * Annotated with {@code @Exclude} to prevent storage in Firebase.
     *
     * @return true if the user is an admin.
     */
    @Exclude
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * @return Base64 encoded profile image string.
     */
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * @return Map of medications indexed by ID.
     */
    public HashMap<String, Medication> getMedications() {
        return this.medications;
    }

    public void setMedications(HashMap<String, Medication> medications) {
        this.medications = medications;
    }

    /**
     * @return Map of daily statistics indexed by date (yyyy-MM-dd).
     */
    public HashMap<String, DailyStats> getDailyStats() {
        if (dailyStats == null) dailyStats = new HashMap<>();
        return dailyStats;
    }

    public void setDailyStats(HashMap<String, DailyStats> dailyStats) {
        this.dailyStats = dailyStats;
    }

    /**
     * Helper to get {@link DailyStats} for a specific date. If it doesn't exist, a new entry is created.
     * Annotated with {@code @Exclude} to prevent storage in Firebase.
     *
     * @param date The date string (yyyy-MM-dd).
     * @return The DailyStats for the given date.
     */
    @Exclude
    public DailyStats getDailyStatsForDate(String date) {
        DailyStats stats = getDailyStats().get(date);
        if (stats == null) {
            stats = new DailyStats();
            stats.setId(date);
            getDailyStats().put(date, stats);
        }
        return stats;
    }

    /**
     * @return Map of emergency contacts indexed by ID.
     */
    public HashMap<String, EmergencyContact> getEmergencyContacts() {
        if (emergencyContacts == null) emergencyContacts = new HashMap<>();
        return emergencyContacts;
    }

    public void setEmergencyContacts(HashMap<String, EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    /**
     * Gets the full name of the user.
     * Annotated with {@code @Exclude} to prevent redundant storage in Firebase.
     *
     * @return A string combining first name and last name.
     */
    @Exclude
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(birthDate, user.birthDate) &&
                Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                role == user.role &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, role, firstName, lastName, birthDate);
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", password='" + password + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", medications=" + medications +
                ", dailyStats=" + dailyStats +
                ", emergencyContacts=" + emergencyContacts +
                '}';
    }

    /**
     * Defines the possible roles a user can have within the application.
     * <p>
     * Roles determine the level of access and available features for each user.
     * </p>
     */
    public enum UserRole {
        /**
         * A standard user with regular permissions (Games, Forum, Medications).
         */
        REGULAR,
        /**
         * An administrative user with elevated permissions (User management, moderation).
         */
        ADMIN
    }
}