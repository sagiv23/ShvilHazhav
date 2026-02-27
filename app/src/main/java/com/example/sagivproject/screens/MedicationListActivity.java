package com.example.sagivproject.screens;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationListAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.MedicationDialog;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.notifications.AlarmScheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity for managing a user's list of medications.
 * <p>
 * This screen displays a list of the user's medications, allowing them to add, edit,
 * delete, and search for medications. It also handles scheduling and canceling
 * medication reminders (alarms).
 * </p>
 */
@AndroidEntryPoint
public class MedicationListActivity extends BaseActivity {
    private final List<Medication> fullMedicationList = new ArrayList<>();
    @Inject
    AlarmScheduler alarmScheduler;
    private MedicationListAdapter adapter;
    private User user;
    private String uid;
    private EditText editSearch;
    private Spinner spinnerSearchType;

    /**
     * Initializes the activity, sets up the UI, RecyclerView, search/filter functionality,
     * and loads medication data.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.medicationListPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = sharedPreferencesUtil.getUser();
        uid = Objects.requireNonNull(user).getId();

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        findViewById(R.id.btn_MedicationList_add_medication).setOnClickListener(view -> openMedicationDialog(null));

        RecyclerView recyclerViewMedications = findViewById(R.id.recyclerView_medications);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicationListAdapter(this, new MedicationListAdapter.OnMedicationActionListener() {
            @Override
            public void onEdit(Medication medication) {
                openMedicationDialog(medication);
            }

            @Override
            public void onDelete(Medication medication) {
                deleteMedicationById(medication);
            }
        });
        recyclerViewMedications.setAdapter(adapter);

        editSearch = findViewById(R.id.edit_Medication_search);
        spinnerSearchType = findViewById(R.id.spinner_Medication_search_type);

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
                filterMedications(editSearch.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadMedicationsFromCache();
        fetchMedicationsFromServer();
    }

    /**
     * Loads the initial list of medications from the local user cache (SharedPreferences).
     */
    private void loadMedicationsFromCache() {
        if (user.getMedications() != null) {
            updateMedicationList(new ArrayList<>(user.getMedications().values()));
        }
    }

    /**
     * Fetches the most up-to-date medication list from the server (Firebase) and updates the UI.
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
     * Updates the local user cache in SharedPreferences with the latest medication list.
     *
     * @param medicationList The list of medications to cache.
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
     * Updates the main medication list, sorts it, and applies the current filter.
     *
     * @param medicationList The new list of medications.
     */
    private void updateMedicationList(List<Medication> medicationList) {
        fullMedicationList.clear();
        fullMedicationList.addAll(medicationList);
        fullMedicationList.sort(Comparator.comparing(Medication::getName));
        updateUserCache(fullMedicationList);
        filterMedications(editSearch.getText().toString());
    }

    /**
     * Creates and configures the ArrayAdapter for the search filter spinner.
     *
     * @return A customized ArrayAdapter for the spinner.
     */
    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter() {
        String[] searchOptions = {"הכל", "שם תרופה", "סוג תרופה"};

        return new ArrayAdapter<>(MedicationListActivity.this, android.R.layout.simple_spinner_item, searchOptions) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(MedicationListActivity.this, R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(getColor(R.color.text_color));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(MedicationListActivity.this, R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(getColor(R.color.text_color));
                tv.setBackgroundColor(getColor(R.color.background_color_buttons));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };
    }

    /**
     * Saves a new medication to the database and schedules its alarms.
     *
     * @param medication The new medication to save.
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
                Toast.makeText(MedicationListActivity.this, "התרופה נוספה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates an existing medication in the database and reschedules its alarms.
     *
     * @param med The medication to update.
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
                Toast.makeText(MedicationListActivity.this, "התרופה עודכנה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a medication from the database and cancels its alarms.
     *
     * @param medication The medication to delete.
     */
    private void deleteMedicationById(Medication medication) {
        databaseService.getMedicationService().deleteMedication(uid, medication.getId(), new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                alarmScheduler.cancel(medication);
                List<Medication> newList = new ArrayList<>(fullMedicationList);
                newList.removeIf(m -> m.getId().equals(medication.getId()));
                updateMedicationList(newList);
                Toast.makeText(MedicationListActivity.this, "התרופה נמחקה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Opens a dialog to add a new medication or edit an existing one.
     *
     * @param medToEdit The medication to edit, or null to add a new one.
     */
    private void openMedicationDialog(Medication medToEdit) {
        new MedicationDialog(this, medToEdit, new MedicationDialog.OnMedicationSubmitListener() {
            @Override
            public void onAdd(Medication medication) {
                saveMedication(medication);
            }

            @Override
            public void onEdit(Medication medication) {
                updateMedication(medication);
            }
        }).show();
    }

    /**
     * Filters the medication list based on the search query and selected filter type.
     * Updates the RecyclerView using DiffUtil for efficient updates.
     *
     * @param query The search text entered by the user.
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

                if (matches) {
                    filteredMedications.add(med);
                }
            }
        }

        adapter.setMedications(filteredMedications);
    }
}
