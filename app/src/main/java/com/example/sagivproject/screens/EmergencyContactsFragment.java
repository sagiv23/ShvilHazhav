package com.example.sagivproject.screens;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmergencyContactsFragment extends BaseFragment {
    private EmergencyContactsAdapter adapter;
    private TextView txtNoContacts;
    private User user;
    private final ActivityResultLauncher<Void> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickContact(), uri -> {
                if (uri != null) {
                    retrieveContactDetails(uri);
                }
            });
    private FusedLocationProviderClient fusedLocationClient;
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
                    EmergencyContact original = user.getEmergencyContacts().get(contact.getId());
                    if (original != null) {
                        original.setFirstName(firstName);
                        original.setLastName(lastName);
                        original.setPhoneNumber(phoneNumber);
                        updateUserInDb("איש הקשר עודכן בהצלחה");
                    }
                });
            }

            @Override
            public void onDelete(EmergencyContact contact) {
                deleteContact(contact);
            }
        });
        rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvContacts.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserFromDatabase();
    }

    private void loadUserFromDatabase() {
        if (user == null) return;
        databaseService.getUserService().getUser(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(User dbUser) {
                if (dbUser != null) {
                    user = dbUser;
                    sharedPreferencesUtil.saveUser(user);
                    loadContacts();
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadContacts() {
        HashMap<String, EmergencyContact> contactsMap = user.getEmergencyContacts();
        List<EmergencyContact> contactsList = new ArrayList<>();

        for (EmergencyContact c : contactsMap.values()) {
            contactsList.add(new EmergencyContact(c.getId(), c.getFirstName(), c.getLastName(), c.getPhoneNumber()));
        }

        adapter.setData(contactsList);
        txtNoContacts.setVisibility(contactsList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addEmergencyContact(String firstName, String lastName, String phoneNumber) {
        EmergencyContact contact = new EmergencyContact(phoneNumber, firstName, lastName, phoneNumber);
        user.getEmergencyContacts().put(phoneNumber, contact);
        updateUserInDb("איש קשר חדש נוסף");
    }

    private void deleteContact(EmergencyContact contact) {
        if (user.getEmergencyContacts().remove(contact.getId()) != null) {
            updateUserInDb("איש הקשר נמחק");
        }
    }

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

    private void updateUserInDb(String successMessage) {
        databaseService.getUserService().updateUser(user, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                sharedPreferencesUtil.saveUser(user);
                if (isAdded()) {
                    if (successMessage != null) {
                        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
                    }
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
    }

    private void checkSmsAndLocationPermissions() {
        permissionLauncher.launch(new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

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

    private void sendSmsToAll(@Nullable String locationUrl) {
        List<EmergencyContact> contacts = new ArrayList<>(user.getEmergencyContacts().values());
        if (contacts.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_emergency_contacts, Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = requireContext().getSystemService(SmsManager.class);
        if (smsManager == null) return;

        for (EmergencyContact contact : contacts) {
            String message = "שלום " + contact.getFirstName() + ", " + getString(R.string.emergency_sms_content);
            message += Objects.requireNonNullElse(locationUrl, "לא ניתן היה להשיג מיקום מדויק.");

            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(contact.getPhoneNumber(), null, parts, null, null);
        }
        Toast.makeText(getContext(), "הודעות חירום נשלחו בהצלחה", Toast.LENGTH_SHORT).show();
    }

    private void callEmergency() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:109"));
        startActivity(intent);
    }
}
