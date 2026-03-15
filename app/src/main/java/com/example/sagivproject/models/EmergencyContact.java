package com.example.sagivproject.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an emergency contact for a user.
 * <p>
 * This class stores the name and phone number of a person to be contacted in case of emergency.
 * </p>
 */
public class EmergencyContact implements Serializable, Idable {
    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    /**
     * Default constructor for Firebase.
     */
    public EmergencyContact() {
    }

    /**
     * Constructs a new EmergencyContact with the specified details.
     *
     * @param id          The unique identifier for the contact.
     * @param firstName   The first name of the contact.
     * @param lastName    The last name of the contact.
     * @param phoneNumber The phone number of the contact.
     */
    public EmergencyContact(String id, String firstName, String lastName, String phoneNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the full name of the contact.
     *
     * @return A string combining first name and last name.
     */
    @Exclude
    public String getFullName() {
        return firstName + " " + lastName;
    }

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
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, phoneNumber);
    }
}
