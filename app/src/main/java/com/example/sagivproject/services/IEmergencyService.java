package com.example.sagivproject.services;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import java.util.List;

/**
 * An interface that defines the contract for operations related to emergency contacts and alerts.
 * <p>
 * This service provides methods for managing a user's emergency contact list and for triggering
 * automated SMS alerts (optionally including location data) during emergencies like a detected fall.
 * </p>
 */
public interface IEmergencyService {
    /**
     * Generates a new, unique identifier for an emergency contact record.
     * @return A unique contact ID string.
     */
    String generateContactId();

    /**
     * Adds a new emergency contact for a specific user.
     * Checks for phone number uniqueness within the user's contact list before adding.
     * @param uid The unique identifier of the user owning the contact.
     * @param firstName The first name of the contact person.
     * @param lastName The last name of the contact person.
     * @param phoneNumber The primary phone number of the contact.
     * @param callback Optional callback invoked upon completion of the database operation.
     */
    void addContact(@NonNull String uid, @NonNull String firstName, @NonNull String lastName, @NonNull String phoneNumber, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves the complete list of emergency contacts for a specific user.
     * @param uid The unique identifier of the user.
     * @param callback The callback invoked with the list of retrieved contacts.
     */
    void getContacts(@NonNull String uid, @NonNull DatabaseCallback<List<EmergencyContact>> callback);

    /**
     * Deletes an emergency contact record from a user's profile.
     * @param uid The unique identifier of the user.
     * @param contactId The ID of the contact to be removed.
     * @param callback Optional callback invoked upon completion.
     */
    void deleteContact(@NonNull String uid, @NonNull String contactId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates the details of an existing emergency contact.
     * @param uid The unique identifier of the user.
     * @param contact The {@link EmergencyContact} object containing updated information.
     * @param callback Optional callback invoked upon completion.
     */
    void updateContact(@NonNull String uid, @NonNull EmergencyContact contact, @Nullable DatabaseCallback<Void> callback);

    /**
     * Sends an automated emergency alert SMS to all provided contacts.
     * @param context The application context (required for SMS services).
     * @param contacts The list of emergency contacts to notify.
     * @param locationUrl An optional Google Maps URL indicating the user's current location.
     * @param callback Optional callback for handling the operation result.
     */
    void sendEmergencyAlert(@NonNull Context context, @NonNull List<EmergencyContact> contacts, @Nullable String locationUrl, @Nullable DatabaseCallback<Void> callback);
}