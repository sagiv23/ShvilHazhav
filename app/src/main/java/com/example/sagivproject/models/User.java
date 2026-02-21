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
 * <p>
 * This class holds all the data associated with a user, including their personal details,
 * credentials, role, game statistics, and medication list. It is a central model in the application.
 * </p>
 */
public class User implements Serializable, Idable {
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

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class).
     */
    public User() {
        this.role = UserRole.REGULAR;
    }

    /**
     * Constructs a new User object.
     *
     * @param id              The unique ID of the user.
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @param email           The user's email address.
     * @param password        The user's password.
     * @param role            The user's role (REGULAR or ADMIN).
     * @param profileImage    The user's profile image as a Base64 string.
     * @param medications     A map of the user's medications.
     */
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

    /**
     * Calculates the user's age based on their birthdate.
     *
     * @return The user's current age in years.
     */
    @Exclude
    public int getAge() {
        Calendar birth = Calendar.getInstance();
        birth.setTimeInMillis(birthDateMillis);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
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

    /**
     * A convenience method to check if the user has an ADMIN role.
     *
     * @return True if the user is an admin, false otherwise.
     */
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

    /**
     * A convenience method to get the user's full name.
     *
     * @return The user's first name and last name concatenated.
     */
    @Exclude
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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
                ", birthDateMillis=" + birthDateMillis +
                ", password='" + password + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", medications=" + medications +
                ", countWins=" + countWins +
                ", mathProblemsStats=" + mathProblemsStats +
                '}';
    }
}
