package com.example.sagivproject.screens;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.EmergencyContactsAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.EmergencyContact;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IFallDetectionService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for managing emergency contacts and triggering manual emergency alerts.
 * <p>
 * This screen allows users to:
 * <ul>
 * <li>View a list of current emergency contacts.</li>
 * <li>Add new contacts manually via a dialog.</li>
 * <li>Pick contacts directly from the device's address book.</li>
 * <li>Edit or delete existing contact records.</li>
 * <li>Manually trigger an emergency SMS broadcast to all contacts, including current GPS coordinates.</li>
 * <li>Quickly dial emergency services (109).</li>
 * </ul>
 * It also coordinates with the {@link IFallDetectionService} to ensure monitoring is active if contacts are present.
 * </p>
 */
@AndroidEntryPoint
public class EmergencyContactsActivity extends BaseActivity {
    /**
     * Service for background fall monitoring.
     */
    @Inject
    protected IFallDetectionService fallDetectionService;

    private EmergencyContactsAdapter adapter;
    private TextView txtNoContacts;
    private View cardFallDetectionReminder;
    private User user;

    /**
     * Launcher for the system contact picker.
     */
    private final ActivityResultLauncher<Void> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickContact(), uri -> {
                if (uri != null) {
                    retrieveContactDetails(uri);
                }
            });

    /**
     * Client for retrieving device location coordinates.
     */
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_contacts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.emergencyContactsPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        user = sharedPreferencesUtil.getUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        RecyclerView rvContacts = findViewById(R.id.rv_emergency_contacts);
        txtNoContacts = findViewById(R.id.txt_no_contacts);
        cardFallDetectionReminder = findViewById(R.id.card_fall_detection_reminder);

        findViewById(R.id.btn_add_contact_dialog).setOnClickListener(v ->
                dialogService.showEmergencyContactDialog(getSupportFragmentManager(), null, this::addEmergencyContact)
        );

        findViewById(R.id.btn_pick_contact).setOnClickListener(v -> requestPermissions(Manifest.permission.READ_CONTACTS));

        findViewById(R.id.btn_send_emergency_sms).setOnClickListener(v -> checkSmsAndLocationPermissions());
        findViewById(R.id.btn_call_109).setOnClickListener(v -> callEmergency());
        findViewById(R.id.btn_go_to_settings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        Button btnSkip = findViewById(R.id.btn_skip);
        boolean isFromRegistration = getIntent().getBooleanExtra("isFromRegistration", false);

        if (isFromRegistration) {
            btnSkip.setVisibility(View.VISIBLE);
            btnSkip.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        } else {
            btnSkip.setVisibility(View.GONE);
        }

        adapter = adapterService.getEmergencyContactsAdapter();
        adapter.setListener(new EmergencyContactsAdapter.OnContactActionListener() {
            @Override
            public void onEdit(EmergencyContact contact) {
                dialogService.showEmergencyContactDialog(getSupportFragmentManager(), contact, (firstName, lastName, phoneNumber) -> {
                    EmergencyContact updatedContact = new EmergencyContact(contact.getId(), firstName, lastName, phoneNumber);
                    databaseService.getEmergencyService().updateContact(user.getId(), updatedContact, new IDatabaseService.DatabaseCallback<>() {
                        @Override
                        public void onCompleted(Void object) {
                            Toast.makeText(EmergencyContactsActivity.this, "איש הקשר עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                            loadUserFromDatabase();
                        }

                        @Override
                        public void onFailed(Exception e) { Toast.makeText(EmergencyContactsActivity.this, "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show(); }
                    });
                });
            }

            @Override
            public void onDelete(EmergencyContact contact) {
                databaseService.getEmergencyService().deleteContact(user.getId(), contact.getId(), new IDatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void object) {
                        Toast.makeText(EmergencyContactsActivity.this, "איש הקשר נמחק", Toast.LENGTH_SHORT).show();
                        loadUserFromDatabase();
                    }

                    @Override
                    public void onFailed(Exception e) { Toast.makeText(EmergencyContactsActivity.this, "שגיאה במחיקת איש הקשר", Toast.LENGTH_SHORT).show(); }
                });
            }
        });
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        updateFallDetectionUI();
    }

    /**
     * Handles permission results for SMS sending and contact reading.
     * @param isGranted Map of permissions and their results.
     */
    @Override
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
        if (Boolean.TRUE.equals(isGranted.get(Manifest.permission.SEND_SMS))) {
            fetchLocationAndSendSms();
        } else if (Boolean.TRUE.equals(isGranted.get(Manifest.permission.READ_CONTACTS))) { contactPickerLauncher.launch(null); }
    }

    /**
     * Refreshes the visibility of the fall detection prompt based on user settings.
     */
    private void updateFallDetectionUI() {
        if (sharedPreferencesUtil.isFallDetectionEnabled()) {
            cardFallDetectionReminder.setVisibility(View.GONE);
        } else {
            cardFallDetectionReminder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserFromDatabase();
        updateFallDetectionUI();
    }

    /**
     * Fetches the latest user profile from the database to ensure UI consistency.
     */
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
            public void onFailed(Exception e) { Toast.makeText(EmergencyContactsActivity.this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Synchronizes the contact list with the database.
     * Automatically disables fall detection if no contacts remain.
     */
    private void loadContacts() {
        if (user == null) return;
        databaseService.getEmergencyService().getContacts(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<EmergencyContact> contactsList) {
                adapter.setData(contactsList);
                txtNoContacts.setVisibility(contactsList.isEmpty() ? View.VISIBLE : View.GONE);

                if (contactsList.isEmpty() && sharedPreferencesUtil.isFallDetectionEnabled()) {
                    sharedPreferencesUtil.setFallDetectionEnabled(false);
                    fallDetectionService.stopMonitoring();
                    updateFallDetectionUI();
                    Toast.makeText(EmergencyContactsActivity.this, "זיהוי נפילות הופסק כיוון שאין אנשי קשר לחירום", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(EmergencyContactsActivity.this, "שגיאה בטעינת אנשי קשר", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Logic for adding a new contact record.
     */
    private void addEmergencyContact(String firstName, String lastName, String phoneNumber) {
        databaseService.getEmergencyService().addContact(user.getId(), firstName, lastName, phoneNumber, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(EmergencyContactsActivity.this, "איש קשר חדש נוסף", Toast.LENGTH_SHORT).show();
                loadUserFromDatabase();
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(EmergencyContactsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Extracts details from a URI returned by the contact picker and creates a new record.
     * @param contactUri The selected contact's URI.
     */
    private void retrieveContactDetails(Uri contactUri) {
        String[] projection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
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
                    Toast.makeText(this, "לא נמצא מספר טלפון לאיש קשר זה", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Queries the system contacts database for a specific contact's phone number.
     * @param contactId Unique ID of the contact.
     * @return Formatted phone number or null.
     */
    private String fetchPhoneNumber(String contactId) {
        String phone = null;
        try (Cursor cursor = getContentResolver().query(
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
     * Checks for SMS and high-accuracy location permissions before triggering an alert.
     */
    private void checkSmsAndLocationPermissions() {
        requestPermissions(
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        );
    }

    /**
     * Fetches coordinates and proceeds to broadcast the SOS message.
     */
    private void fetchLocationAndSendSms() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
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
     * Orchestrates the final SMS transmission via the emergency service.
     * @param locationUrl Optional Google Maps link.
     */
    private void sendSmsToAll(@Nullable String locationUrl) {
        databaseService.getEmergencyService().getContacts(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<EmergencyContact> contacts) {
                databaseService.getEmergencyService().sendEmergencyAlert(EmergencyContactsActivity.this, contacts, locationUrl, new IDatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void object) { Toast.makeText(EmergencyContactsActivity.this, "הודעות חירום נשלחו בהצלחה", Toast.LENGTH_SHORT).show(); }

                    @Override
                    public void onFailed(Exception e) { Toast.makeText(EmergencyContactsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); }
                });
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(EmergencyContactsActivity.this, "שגיאה בטעינת אנשי קשר לשליחת SMS", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Launches the system dialer with the emergency number pre-filled.
     */
    private void callEmergency() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:109"));
        startActivity(intent);
    }
}