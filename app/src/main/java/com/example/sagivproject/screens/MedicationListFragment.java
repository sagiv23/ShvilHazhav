package com.example.sagivproject.screens;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationListAdapter;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.DailyStats;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.MedicationUsage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.MedicationStatus;
import com.example.sagivproject.screens.dialogs.MedicationDialog;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.notifications.AlarmScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment for managing and displaying a user's list of medications.
 * <p>
 * This fragment allows users to view their medication schedule, add new medications,
 * edit or delete existing ones, and search through their list by name or type.
 * It also facilitates logging the daily intake status for each medication dose.
 * </p>
 */
@AndroidEntryPoint
public class MedicationListFragment extends BaseFragment {
    private final List<Medication> fullMedicationList = new ArrayList<>();

    /**
     * Helper service to schedule alarms for medication reminders.
     */
    @Inject
    AlarmScheduler alarmScheduler;

    private MedicationListAdapter adapter;
    private User user;
    private String uid;
    private EditText editSearch;
    private Spinner spinnerSearchType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medication_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = sharedPreferencesUtil.getUser();
        uid = Objects.requireNonNull(user).getId();

        view.findViewById(R.id.btn_MedicationList_add_medication).setOnClickListener(v -> openMedicationDialog(null));

        RecyclerView recyclerViewMedications = view.findViewById(R.id.recyclerView_medications);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(getContext()));

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
        recyclerViewMedications.setAdapter(adapter);

        editSearch = view.findViewById(R.id.edit_Medication_search);
        spinnerSearchType = view.findViewById(R.id.spinner_Medication_search_type);

        setupSearch();
        loadMedicationsFromCache();
        fetchMedicationsFromServer();
        fetchTodayUsageLogs();
    }

    /**
     * Sets up the search bar and filter spinner logic.
     */
    private void setupSearch() {
        if (getContext() == null) return;
        ArrayAdapter<String> spinnerAdapter = getStringArrayAdapter();
        spinnerSearchType.setAdapter(spinnerAdapter);

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
     * Fetches medication usage logs for the current day from the database.
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

                // Update local user stats cache for consistency
                DailyStats stats = user.getDailyStats().get(today);
                if (stats == null) {
                    stats = new DailyStats();
                    user.getDailyStats().put(today, stats);
                }
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
     * Logs the intake status of a medication dose and updates the database and local cache.
     *
     * @param medication    The medication whose dose is being logged.
     * @param scheduledTime The scheduled time of the dose.
     * @param status        The intake status (TAKEN, NOT_TAKEN, SNOOZED).
     */
    private void logMedicationStatus(Medication medication, String scheduledTime, MedicationStatus status) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        MedicationUsage usage = new MedicationUsage(medication.getId(), medication.getName(), time, date, scheduledTime, status);

        databaseService.getMedicationService().logMedicationUsage(uid, usage, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                // Update local User cache for immediate UI feedback
                DailyStats stats = user.getDailyStats().get(date);
                if (stats == null) {
                    stats = new DailyStats();
                    user.getDailyStats().put(date, stats);
                }
                stats.addMedicationUsageLog(usage);
                if (status == MedicationStatus.TAKEN) stats.addMedicationTaken();
                else if (status == MedicationStatus.NOT_TAKEN) stats.addMedicationMissed();
                sharedPreferencesUtil.saveUser(user);

                adapter.addLoggedTodayMedication(usage);
                Toast.makeText(requireContext(), "סטטוס עודכן: " + status.getDisplayName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                adapter.setProcessingFinished(medication.getId());
                Toast.makeText(requireContext(), "שגיאה בעדכון סטטוס", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads the medication list from the local cache for quick initial display.
     */
    private void loadMedicationsFromCache() {
        if (user.getMedications() != null) {
            updateMedicationList(new ArrayList<>(user.getMedications().values()));
        }
    }

    /**
     * Fetches the full medication list from the server.
     */
    private void fetchMedicationsFromServer() {
        databaseService.getMedicationService().getUserMedicationList(uid, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Medication> list) {
                updateMedicationList(list);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the user's medication cache in SharedPreferences.
     */
    private void updateUserCache(List<Medication> medicationList) {
        HashMap<String, Medication> updatedMedicationsMap = new HashMap<>();
        for (Medication med : medicationList) {
            updatedMedicationsMap.put(med.getId(), med);
        }
        user.setMedications(updatedMedicationsMap);
        sharedPreferencesUtil.saveUser(user);
    }

    /**
     * Updates the internal list of medications, sorts it, and refreshes the UI.
     */
    private void updateMedicationList(List<Medication> medicationList) {
        fullMedicationList.clear();
        fullMedicationList.addAll(medicationList);
        fullMedicationList.sort(Comparator.comparing(Medication::getName));
        updateUserCache(fullMedicationList);
        filterMedications(editSearch.getText().toString());
    }

    /**
     * Helper to create a styled ArrayAdapter for the search filter spinner.
     */
    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter() {
        String[] searchOptions = {"הכל", "שם תרופה", "סוג תרופה"};
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, searchOptions) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(getColor(R.color.text_color));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(getColor(R.color.text_color));
                tv.setBackgroundColor(getColor(R.color.background_color_buttons));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };
    }

    /**
     * Saves a new medication to the database and schedules its reminders.
     */
    private void saveMedication(Medication medication) {
        String medicationId = databaseService.getMedicationService().generateMedicationId();
        medication.setId(medicationId);
        medication.setUserId(uid);
        databaseService.getMedicationService().createNewMedication(uid, medication, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                alarmScheduler.schedule(medication);
                List<Medication> newList = new ArrayList<>(fullMedicationList);
                newList.add(medication);
                updateMedicationList(newList);
                Toast.makeText(requireContext(), "התרופה נוספה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates an existing medication's details in the database and refreshes its reminders.
     */
    private void updateMedication(Medication med) {
        med.setUserId(uid);
        databaseService.getMedicationService().updateMedication(uid, med, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                alarmScheduler.cancel(med);
                alarmScheduler.schedule(med);
                List<Medication> newList = new ArrayList<>(fullMedicationList);
                for (int i = 0; i < newList.size(); i++) {
                    if (newList.get(i).getId().equals(med.getId())) {
                        newList.set(i, med);
                        break;
                    }
                }
                updateMedicationList(newList);
                Toast.makeText(requireContext(), "התרופה עודכנה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a medication by its ID and cancels its reminders.
     */
    private void deleteMedicationById(Medication medication) {
        databaseService.getMedicationService().deleteMedication(uid, medication.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                alarmScheduler.cancel(medication);
                List<Medication> newList = new ArrayList<>(fullMedicationList);
                newList.removeIf(m -> m.getId().equals(medication.getId()));
                updateMedicationList(newList);
                Toast.makeText(requireContext(), "התרופה נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Opens a dialog to add a new medication or edit an existing one.
     */
    private void openMedicationDialog(Medication medToEdit) {
        dialogService.showMedicationDialog(getParentFragmentManager(), medToEdit, new MedicationDialog.OnMedicationSubmitListener() {
            @Override
            public void onAdd(Medication medication) {
                saveMedication(medication);
            }

            @Override
            public void onEdit(Medication medication) {
                updateMedication(medication);
            }
        });
    }

    /**
     * Filters the medication list based on the search query and selected filter type.
     */
    private void filterMedications(String query) {
        List<Medication> filteredMedications = new ArrayList<>();
        String selectedType = spinnerSearchType.getSelectedItem() != null ? spinnerSearchType.getSelectedItem().toString() : "הכל";
        if (query.isEmpty() && selectedType.equals("הכל")) {
            filteredMedications.addAll(fullMedicationList);
        } else {
            for (Medication med : fullMedicationList) {
                boolean matches = false;
                switch (selectedType) {
                    case "הכל":
                        matches = med.getName().toLowerCase().contains(query.toLowerCase()) ||
                                (med.getType() != null && med.getType().getDisplayName().toLowerCase().contains(query.toLowerCase()));
                        break;
                    case "שם תרופה":
                        matches = med.getName().toLowerCase().contains(query.toLowerCase());
                        break;
                    case "סוג תרופה":
                        matches = med.getType() != null && med.getType().getDisplayName().toLowerCase().contains(query.toLowerCase());
                        break;
                }
                if (matches) filteredMedications.add(med);
            }
        }
        adapter.setMedications(filteredMedications);
    }
}
