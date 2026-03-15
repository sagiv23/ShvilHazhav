package com.example.sagivproject.screens;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.EmergencyContactsAdapter;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment for managing emergency contacts and sending emergency alerts.
 * <p>
 * This fragment allows users to add emergency contacts manually or by picking from the device's
 * contacts list. It also provides functionality to send emergency SMS messages to all
 * contacts, including the user's current location, and a quick dial for emergency services.
 * </p>
 */
@AndroidEntryPoint
public class EmergencyContactsFragment extends BaseFragment {
    private EmergencyContactsAdapter adapter;
    private TextView txtNoContacts;
    private User user;

    /**
     * Launcher for picking a contact from the device's contact list.
     */
    private final ActivityResultLauncher<Void> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickContact(), uri -> {
                if (uri != null) {
                    retrieveContactDetails(uri);
                }
            });

    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Launcher for requesting multiple permissions (SMS and Location).
     */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (Boolean.TRUE.equals(result.get(Manifest.permission.SEND_SMS))) {
                    fetchLocationAndSendSms();
                } else if (Boolean.TRUE.equals(result.get(Manifest.permission.READ_CONTACTS))) {
                    contactPickerLauncher.launch(null);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = sharedPreferencesUtil.getUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        RecyclerView rvContacts = view.findViewById(R.id.rv_emergency_contacts);
        txtNoContacts = view.findViewById(R.id.txt_no_contacts);

        view.findViewById(R.id.btn_add_contact_dialog).setOnClickListener(v ->
                dialogService.showEmergencyContactDialog(getChildFragmentManager(), null, this::addEmergencyContact)
        );

        view.findViewById(R.id.btn_pick_contact).setOnClickListener(v -> permissionLauncher.launch(new String[]{Manifest.permission.READ_CONTACTS}));

        view.findViewById(R.id.btn_send_emergency_sms).setOnClickListener(v -> checkSmsAndLocationPermissions());
        view.findViewById(R.id.btn_call_109).setOnClickListener(v -> callEmergency());

        adapter = adapterService.getEmergencyContactsAdapter();
        adapter.setListener(new EmergencyContactsAdapter.OnContactActionListener() {
            @Override
            public void onEdit(EmergencyContact contact) {
                dialogService.showEmergencyContactDialog(getChildFragmentManager(), contact, (firstName, lastName, phoneNumber) -> {
                    contact.setFirstName(firstName);
                    contact.setLastName(lastName);
                    contact.setPhoneNumber(phoneNumber);
                    databaseService.getEmergencyService().updateContact(user.getId(), contact, new IDatabaseService.DatabaseCallback<>() {
                        @Override
                        public void onCompleted(Void object) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "איש הקשר עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                                loadContacts();
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                });
            }

            @Override
            public void onDelete(EmergencyContact contact) {
                databaseService.getEmergencyService().deleteContact(user.getId(), contact.getId(), new IDatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void object) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "איש הקשר נמחק", Toast.LENGTH_SHORT).show();
                            loadContacts();
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "שגיאה במחיקת איש הקשר", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvContacts.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContacts();
    }

    /**
     * Loads the emergency contacts from the database into the adapter.
     */
    private void loadContacts() {
        if (user == null) return;
        databaseService.getEmergencyService().getContacts(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<EmergencyContact> contactsList) {
                if (isAdded()) {
                    adapter.setData(contactsList);
                    txtNoContacts.setVisibility(contactsList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "שגיאה בטעינת אנשי קשר", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Adds a new emergency contact to the user's profile and updates the database.
     */
    private void addEmergencyContact(String firstName, String lastName, String phoneNumber) {
        databaseService.getEmergencyService().addContact(user.getId(), firstName, lastName, phoneNumber, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "איש קשר חדש נוסף", Toast.LENGTH_SHORT).show();
                    loadContacts();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Retrieves name and phone number from a picked contact URI.
     *
     * @param contactUri The URI of the picked contact.
     */
    private void retrieveContactDetails(Uri contactUri) {
        String[] projection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        try (Cursor cursor = requireContext().getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNumber = fetchPhoneNumber(id);

                if (phoneNumber != null) {
                    String[] nameParts = name.split(" ", 2);
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";
                    addEmergencyContact(firstName, lastName, phoneNumber);
                } else {
                    Toast.makeText(getContext(), "לא נמצא מספר טלפון לאיש קשר זה", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Fetches the primary phone number for a given contact ID.
     *
     * @param contactId The ID of the contact.
     * @return The phone number string, or null if not found.
     */
    private String fetchPhoneNumber(String contactId) {
        String phone = null;
        try (Cursor cursor = requireContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
        }
        return phone;
    }

    /**
     * Requests necessary permissions for sending SMS and accessing location.
     */
    private void checkSmsAndLocationPermissions() {
        permissionLauncher.launch(new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    /**
     * Attempts to fetch the current location and then send an emergency SMS to all contacts.
     */
    private void fetchLocationAndSendSms() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(requireActivity(), location -> {
                        String locationUrl = null;
                        if (location != null) {
                            locationUrl = "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
                        }
                        sendSmsToAll(locationUrl);
                    })
                    .addOnFailureListener(e -> sendSmsToAll(null));
        } catch (SecurityException e) {
            sendSmsToAll(null);
        }
    }

    /**
     * Sends an emergency SMS message to all emergency contacts.
     *
     * @param locationUrl The Google Maps URL of the current location, or null if unavailable.
     */
    private void sendSmsToAll(@Nullable String locationUrl) {
        databaseService.getEmergencyService().getContacts(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<EmergencyContact> contacts) {
                databaseService.getEmergencyService().sendEmergencyAlert(requireContext(), contacts, locationUrl, new IDatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void object) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "הודעות חירום נשלחו בהצלחה", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "שגיאה בטעינת אנשי קשר לשליחת SMS", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Initiates a dialer intent with the emergency services number.
     */
    private void callEmergency() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:109"));
        startActivity(intent);
    }
}
