package com.example.sagivproject.services.impl;

import android.content.Context;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IEmergencyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

/**
 * Implementation of the {@link IEmergencyService} interface.
 * <p>
 * This class handles the persistence of emergency contacts within the user's database entry
 * and provides the logic for sending automated emergency SMS alerts. It ensures that
 * phone numbers are unique within a user's contact list.
 * </p>
 */
public class EmergencyServiceImpl extends BaseDatabaseService<EmergencyContact> implements IEmergencyService {
    private static final String USERS_PATH = "users";
    private static final String CONTACTS_PATH = "emergencyContacts";

    /**
     * Constructs a new EmergencyServiceImpl.
     * Initializes the base service with an empty path as full paths are built dynamically.
     */
    @Inject
    public EmergencyServiceImpl() {
        super("", EmergencyContact.class);
    }

    @Override
    public String generateContactId() {
        return super.generateId();
    }

    /**
     * Adds a new emergency contact to the database.
     * First validates that the phone number is not already present in the user's list.
     *
     * @param uid         User ID.
     * @param firstName   Contact's first name.
     * @param lastName    Contact's last name.
     * @param phoneNumber Contact's phone number.
     * @param callback    Result callback.
     */
    @Override
    public void addContact(@NonNull String uid, @NonNull String firstName, @NonNull String lastName, @NonNull String phoneNumber, @Nullable DatabaseCallback<Void> callback) {
        getContacts(uid, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<EmergencyContact> contacts) {
                for (EmergencyContact c : contacts) {
                    if (Objects.equals(c.getPhoneNumber(), phoneNumber)) {
                        if (callback != null)
                            callback.onFailed(new Exception("מספר טלפון זה כבר קיים ברשימת אנשי הקשר"));
                        return;
                    }
                }
                String contactId = generateContactId();
                EmergencyContact contact = new EmergencyContact(contactId, firstName, lastName, phoneNumber);
                writeData(getContactItemPath(uid, contactId), contact, callback);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    @Override
    public void getContacts(@NonNull String uid, @NonNull DatabaseCallback<List<EmergencyContact>> callback) {
        getDataList(getContactsPath(uid), callback);
    }

    @Override
    public void deleteContact(@NonNull String uid, @NonNull String contactId, @Nullable DatabaseCallback<Void> callback) {
        deleteData(getContactItemPath(uid, contactId), callback);
    }

    /**
     * Updates an existing contact record using a database transaction.
     *
     * @param uid      User ID.
     * @param contact  Updated contact object.
     * @param callback Result callback.
     */
    @Override
    public void updateContact(@NonNull String uid, @NonNull EmergencyContact contact, @Nullable DatabaseCallback<Void> callback) {
        UnaryOperator<EmergencyContact> updateFunction = oldContact -> contact;
        runTransaction(getContactItemPath(uid, contact.getId()), updateFunction, new DatabaseCallback<>() {
            @Override
            public void onCompleted(EmergencyContact result) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    /**
     * Sends emergency SMS messages to all provided contacts.
     * Includes location URL if available.
     *
     * @param context     App context.
     * @param contacts    List of contacts to notify.
     * @param locationUrl Google Maps link or null.
     * @param callback    Result callback.
     */
    @Override
    public void sendEmergencyAlert(@NonNull Context context, @NonNull List<EmergencyContact> contacts, @Nullable String locationUrl, @Nullable DatabaseCallback<Void> callback) {
        if (contacts.isEmpty()) {
            if (callback != null)
                callback.onFailed(new Exception(context.getString(R.string.no_emergency_contacts)));
            return;
        }

        SmsManager smsManager = context.getSystemService(SmsManager.class);
        if (smsManager == null) {
            if (callback != null) callback.onFailed(new Exception("לא ניתן לגשת לשירות ה-SMS"));
            return;
        }

        try {
            for (EmergencyContact contact : contacts) {
                String message = "שלום " + contact.getFirstName() + ", " + context.getString(R.string.emergency_sms_content);
                message += Objects.requireNonNullElse(locationUrl, "לא ניתן היה להשיג מיקום מדויק.");

                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(contact.getPhoneNumber(), null, parts, null, null);
            }
            if (callback != null) callback.onCompleted(null);
        } catch (Exception e) {
            if (callback != null) callback.onFailed(e);
        }
    }

    /**
     * Constructs the database path for a user's emergency contacts collection.
     */
    private String getContactsPath(String uid) {
        return USERS_PATH + "/" + uid + "/" + CONTACTS_PATH;
    }

    /**
     * Constructs the database path for a specific emergency contact record.
     */
    private String getContactItemPath(String uid, String contactId) {
        return getContactsPath(uid) + "/" + contactId;
    }
}
