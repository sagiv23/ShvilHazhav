package com.example.sagivproject.services;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to emergency contacts and alerts.
 */
public interface IEmergencyService {
    /**
     * Generates a new unique ID for an emergency contact.
     *
     * @return A unique ID string.
     */
    String generateContactId();

    /**
     * Adds a new emergency contact for a specific user, ensuring the phone number is unique.
     *
     * @param uid         The ID of the user.
     * @param firstName   The first name of the contact.
     * @param lastName    The last name of the contact.
     * @param phoneNumber The phone number of the contact.
     * @param callback    Optional callback.
     */
    void addContact(@NonNull String uid, @NonNull String firstName, @NonNull String lastName, @NonNull String phoneNumber, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves the list of emergency contacts for a specific user.
     *
     * @param uid      The ID of the user.
     * @param callback The callback to be invoked with the list of contacts.
     */
    void getContacts(@NonNull String uid, @NonNull DatabaseCallback<List<EmergencyContact>> callback);

    /**
     * Deletes an emergency contact for a user.
     *
     * @param uid       The ID of the user.
     * @param contactId The ID of the contact to delete.
     * @param callback  Optional callback.
     */
    void deleteContact(@NonNull String uid, @NonNull String contactId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates an emergency contact's details.
     *
     * @param uid      The ID of the user.
     * @param contact  The updated contact object.
     * @param callback Optional callback.
     */
    void updateContact(@NonNull String uid, @NonNull EmergencyContact contact, @Nullable DatabaseCallback<Void> callback);

    /**
     * Sends an emergency alert (SMS) to all provided contacts.
     *
     * @param context     The application context.
     * @param contacts    The list of contacts to notify.
     * @param locationUrl The Google Maps URL of the current location.
     * @param callback    Optional callback for the operation result.
     */
    void sendEmergencyAlert(@NonNull Context context, @NonNull List<EmergencyContact> contacts, @Nullable String locationUrl, @Nullable DatabaseCallback<Void> callback);
}
