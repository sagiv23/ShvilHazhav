package com.example.sagivproject.models;

import com.google.firebase.database.Exclude;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an emergency contact for a user.
 * <p>
 * This class stores the personal details and contact information of a person to be notified
 * in case of an emergency, such as a detected fall. It includes helper methods for
 * UI display (e.g., full name).
 * </p>
 */
public class EmergencyContact implements Serializable, Idable {
    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    /** Default constructor required for Firebase deserialization. */
    public EmergencyContact() {
    }

    /**
     * Constructs a new EmergencyContact with the specified details.
     * @param id The unique identifier for this contact record.
     * @param firstName The contact's first name.
     * @param lastName The contact's last name.
     * @param phoneNumber The contact's primary phone number.
     */
    public EmergencyContact(String id, String firstName, String lastName, String phoneNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getId() { return id; }

    @Override
    public void setId(String id) { this.id = id; }

    /** @return The contact's first name. */
    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return The contact's last name. */
    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return The contact's phone number. */
    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Gets the full name of the contact.
     * Annotated with {@code @Exclude} to prevent redundant storage in Firebase.
     * @return A string combining first name and last name.
     */
    @Exclude
    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmergencyContact contact = (EmergencyContact) o;
        return Objects.equals(id, contact.id) &&
                Objects.equals(firstName, contact.firstName) &&
                Objects.equals(lastName, contact.lastName) &&
                Objects.equals(phoneNumber, contact.phoneNumber);
    }

    @Override
    public int hashCode() { return Objects.hash(id, firstName, lastName, phoneNumber); }
}