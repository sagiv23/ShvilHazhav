package com.example.sagivproject.screens;

import android.Manifest;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationListAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.notifications.AlarmScheduler;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for managing and displaying the user's medication list and intake schedule.
 * <p>
 * This activity provides a unified interface for:
 * <ul>
 * <li>Viewing current medications in a tabbed {@link ViewPager2} layout.</li>
 * <li>Searching and filtering medications by name or type.</li>
 * <li>Logging real-time intake status (Taken, Not Taken, Snoozed).</li>
 * <li>Adding, editing, and deleting medication prescriptions.</li>
 * <li>Scheduling and managing local notification reminders via {@link AlarmScheduler}.</li>
 * </ul>
 * It ensures the local UI state is synchronized with both Firebase and the local device cache.
 * </p>
 */
@AndroidEntryPoint
public class MedicationListActivity extends BaseActivity {
    private final Map<String, Medication> medicationMap = new HashMap<>();

    /**
     * Utility for scheduling system alarms for medication reminders.
     */
    @Inject
    AlarmScheduler alarmScheduler;

    private MedicationListAdapter adapter;
    private User user;
    private String uid;
    private EditText editSearch;
    private Spinner spinnerSearchType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_medication_list, R.id.medicationListPage);
        setupMenu();

        user = sharedPreferencesUtil.getUser();
        uid = Objects.requireNonNull(user).getId();

        findViewById(R.id.btn_MedicationList_add_medication).setOnClickListener(v -> openMedicationDialog(null));

        ViewPager2 viewPager_medications = findViewById(R.id.viewPager_medications);

        adapter = adapterService.getMedicationListAdapter();
        adapter.setListener(new MedicationListAdapter.OnMedicationActionListener() {
            @Override
            public void onEdit(Medication medication) {
                openMedicationDialog(medication);
            }

            @Override
            public void onDelete(Medication medication) {
                deleteMedicationById(medication);
            }

            @Override
            public void onStatusChanged(Medication medication, String scheduledTime, MedicationStatus status) {
                logMedicationStatus(medication, scheduledTime, status);
            }
        });
        viewPager_medications.setAdapter(adapter);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.text_hebrew);

        new TabLayoutMediator(findViewById(R.id.tabLayout_medications), viewPager_medications, (tab, position) -> {
            if (position < adapter.getItemList().size()) {
                String name = adapter.getItemList().get(position).getName();
                if (typeface != null) {
                    SpannableString s = new SpannableString(name);
                    s.setSpan(new TypefaceSpan(typeface), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    s.setSpan(new AbsoluteSizeSpan(22, true), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tab.setText(s);
                } else {
                    tab.setText(name);
                }
            }
        }).attach();

        editSearch = findViewById(R.id.edit_Medication_search);
        spinnerSearchType = findViewById(R.id.spinner_Medication_search_type);

        setupSearch();
        loadMedicationsFromCache();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMedicationsFromServer();
        fetchTodayUsageLogs();
    }

    /**
     * Initializes the search bar and filter logic.
     */
    private void setupSearch() {
        String[] searchOptions = {"הכל", "שם תרופה", "סוג תרופה"};
        spinnerSearchType.setAdapter(createStyledSearchAdapter(searchOptions));

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMedications(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editSearch.setEnabled(position != 0);
                if (position == 0) editSearch.setText("");
                filterMedications(editSearch.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Retrieves usage history for the current day to update the "Taken" status rows.
     */
    private void fetchTodayUsageLogs() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        databaseService.getMedicationService().getMedicationUsageLogs(uid, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<MedicationUsage> logs) {
                List<MedicationUsage> todayLogs = new ArrayList<>();
                for (MedicationUsage usage : logs) {
                    if (today.equals(usage.getDate())) {
                        todayLogs.add(usage);
                    }
                }

                DailyStats stats = user.getTodayStats();
                stats.setMedicationUsageLogs(todayLogs);
                sharedPreferencesUtil.saveUser(user);

                adapter.setLoggedTodayMedications(todayLogs);
            }

            @Override
            public void onFailed(Exception e) {
            }
        });
    }

    /**
     * Commits a medication status change to the database and updates daily stats.
     *
     * @param medication    Target medication.
     * @param scheduledTime The time of the dose (HH:mm).
     * @param status        The result (TAKEN, etc.).
     */
    private void logMedicationStatus(Medication medication, String scheduledTime, MedicationStatus status) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        MedicationUsage usage = new MedicationUsage(medication.getId(), medication.getName(), time, date, scheduledTime, status);

        databaseService.getStatsService().logMedicationUsage(uid, usage, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                DailyStats stats = user.getTodayStats();
                stats.addMedicationUsageLog(usage);
                if (status == MedicationStatus.TAKEN) stats.addMedicationTaken();
                else if (status == MedicationStatus.NOT_TAKEN) stats.addMedicationMissed();
                sharedPreferencesUtil.saveUser(user);

                adapter.addLoggedTodayMedication(usage);
                Toast.makeText(MedicationListActivity.this, "סטטוס עודכן: " + status.getDisplayName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                adapter.setProcessingFinished(medication.getId());
                Toast.makeText(MedicationListActivity.this, "שגיאה בעדכון סטטוס", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads medication data from SharedPreferences for immediate UI population.
     */
    private void loadMedicationsFromCache() {
        if (user.getMedications() != null) {
            updateMedicationList(new ArrayList<>(user.getMedications().values()));
        }
    }

    /**
     * Fetches the latest medication prescriptions from Firebase.
     */
    private void fetchMedicationsFromServer() {
        databaseService.getMedicationService().getUserMedicationList(uid, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Medication> list) {
                updateMedicationList(list);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the persistent local cache with the latest data list.
     */
    private void updateUserCache() {
        user.setMedications(new HashMap<>(medicationMap));
        sharedPreferencesUtil.saveUser(user);
    }

    /**
     * Processes a new list of medications: sorts alphabetically and updates UI.
     */
    private void updateMedicationList(List<Medication> medicationList) {
        medicationMap.clear();
        for (Medication med : medicationList) {
            medicationMap.put(med.getId(), med);
        }
        updateUserCache();
        filterMedications(editSearch.getText().toString());
    }

    /**
     * Saves a new medication entry and configures its reminders.
     */
    private void saveMedication(Medication medication) {
        String medicationId = databaseService.getMedicationService().generateMedicationId();
        medication.setId(medicationId);
        databaseService.getMedicationService().createNewMedication(uid, medication, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                checkNotificationPermissionAndSchedule(medication);
                medicationMap.put(medication.getId(), medication);
                updateUserCache();
                filterMedications(editSearch.getText().toString());
                Toast.makeText(MedicationListActivity.this, "התרופה נוספה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Validates notification permissions before scheduling reminders.
     */
    private void checkNotificationPermissionAndSchedule(Medication medication) {
        runWithPermission(Manifest.permission.POST_NOTIFICATIONS, () -> alarmScheduler.schedule(medication));
    }

    @Override
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
        if (Boolean.TRUE.equals(isGranted.get(Manifest.permission.POST_NOTIFICATIONS))) {
            Toast.makeText(this, "הרשאת התראות אושרה", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "נדרשת הרשאת התראות לצורך תזכורות נטילה", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Commits medication updates and refreshes active alarms.
     */
    private void updateMedication(Medication med) {
        databaseService.getMedicationService().updateMedication(uid, med, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                alarmScheduler.cancel(med);
                checkNotificationPermissionAndSchedule(med);
                medicationMap.put(med.getId(), med);
                updateUserCache();
                filterMedications(editSearch.getText().toString());
                Toast.makeText(MedicationListActivity.this, "התרופה עודכנה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Removes a medication record and terminates its scheduled alarms.
     */
    private void deleteMedicationById(Medication medication) {
        databaseService.getMedicationService().deleteMedication(uid, medication.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                alarmScheduler.cancel(medication);
                medicationMap.remove(medication.getId());
                updateUserCache();
                filterMedications(editSearch.getText().toString());
                Toast.makeText(MedicationListActivity.this, "התרופה נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Opens the specialized dialog for adding or modifying medications.
     */
    private void openMedicationDialog(Medication medToEdit) {
        dialogService.showMedicationDialog(getSupportFragmentManager(), medToEdit, medication -> {
            if (medToEdit == null) {
                saveMedication(medication);
            } else {
                updateMedication(medication);
            }
        });
    }

    /**
     * Filters the medication list based on name or type criteria.
     *
     * @param query The search query string.
     */
    private void filterMedications(String query) {
        String selectedType = spinnerSearchType.getSelectedItem() != null ? spinnerSearchType.getSelectedItem().toString() : "הכל";
        String lowerQuery = query.toLowerCase();

        List<Medication> filteredMedications = medicationMap.values().stream()
                .filter(med -> {
                    if (query.isEmpty() && selectedType.equals("הכל")) return true;
                    switch (selectedType) {
                        case "שם תרופה":
                            return med.getName().toLowerCase().contains(lowerQuery);
                        case "סוג תרופה":
                            return med.getType() != null && med.getType().getDisplayName().toLowerCase().contains(lowerQuery);
                        case "הכל":
                        default:
                            return med.getName().toLowerCase().contains(lowerQuery) ||
                                    (med.getType() != null && med.getType().getDisplayName().toLowerCase().contains(lowerQuery));
                    }
                })
                .sorted(Comparator.comparing(Medication::getName))
                .collect(Collectors.toList());

        adapter.setMedications(filteredMedications);
    }
}