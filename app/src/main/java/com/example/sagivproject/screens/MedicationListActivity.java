package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.MedicationDialog;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MedicationListActivity extends BaseActivity {
    Button btnToMain, btnToContact, btnToDetailsAboutUser, btnAddMedication, btnToExit;
    RecyclerView recyclerViewMedications;

    private MedicationAdapter adapter;
    private ArrayList<Medication> medications = new ArrayList<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private User user;
    private String uid;

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

        user = SharedPreferencesUtil.getUser(this);
        uid = user.getUid();

        btnToMain = findViewById(R.id.btn_MedicationList_to_main);
        btnToContact = findViewById(R.id.btn_MedicationList_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_MedicationList_to_DetailsAboutUser);
        btnAddMedication = findViewById(R.id.btn_MedicationList_add_medication);
        btnToExit = findViewById(R.id.btn_MedicationList_to_exit);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, DetailsAboutUserActivity.class)));
        btnAddMedication.setOnClickListener(view -> openMedicationDialog(null));
        btnToExit.setOnClickListener(view -> logout());

        recyclerViewMedications = findViewById(R.id.recyclerView_medications);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicationAdapter(this, medications, new MedicationAdapter.OnMedicationActionListener() {
            @Override
            public void onEdit(int position) {
                openMedicationDialog(medications.get(position));
            }

            @Override
            public void onDelete(int position) {
                deleteMedicationById(medications.get(position).getId());
            }
        });

        recyclerViewMedications.setAdapter(adapter);

        loadMedications();
    }

    private void loadMedications() {
        if(user.getMedications() != null) {
            List<Medication> cachedList = new ArrayList<>(user.getMedications().values());

            medications.clear();
            medications.addAll(cachedList);

            medications.sort(Comparator.comparing(Medication::getDate));
            adapter.notifyDataSetChanged();
        }

        databaseService.getUserMedicationList(uid, new DatabaseService.DatabaseCallback<List<Medication>>() {
            @Override
            public void onCompleted(List<Medication> list) {
                medications.clear();

                HashMap<String, Medication> updatedMedicationsMap = new HashMap<>();

                Date today = new Date();
                List<String> expiredIds = new ArrayList<>();

                for (Medication med : list) {
                    if (med.getDate() != null) {
                        Calendar expiryCal = Calendar.getInstance();
                        expiryCal.setTime(med.getDate());
                        expiryCal.add(Calendar.DAY_OF_YEAR, 1);

                        if (today.after(expiryCal.getTime())) {
                            expiredIds.add(med.getId());
                        } else {
                            medications.add(med);
                            updatedMedicationsMap.put(med.getId(), med);
                        }
                    } else {
                        medications.add(med);
                        updatedMedicationsMap.put(med.getId(), med);
                    }
                }

                //מחיקה של פגי תוקף
                for (String id : expiredIds) {
                    databaseService.deleteMedication(uid, id, null);
                }

                //אם נמחקו תרופות, להציג Toast
                if (!expiredIds.isEmpty()) {
                    Toast.makeText(MedicationListActivity.this, "נמחקו תרופות שפגו תוקפן", Toast.LENGTH_SHORT).show();
                }

                // סידור רשימת התרופות התקינות
                medications.sort(Comparator.comparing(Medication::getDate));

                adapter.notifyDataSetChanged();

                user.setMedications(updatedMedicationsMap);
                SharedPreferencesUtil.saveUser(MedicationListActivity.this, user);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMedication(Medication medication) {
        medication.setId(databaseService.generateMedicationId(uid));
        databaseService.createNewMedication(uid, medication, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                HashMap<String, Medication> medsMap = user.getMedications();
                if (medsMap == null) {
                    medsMap = new HashMap<>();
                }
                medsMap.put(medication.getId(), medication);
                user.setMedications(medsMap);
                SharedPreferencesUtil.saveUser(MedicationListActivity.this, user);
                Toast.makeText(MedicationListActivity.this, "התרופה נוספה", Toast.LENGTH_SHORT).show();
                loadMedications();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMedication(Medication med) {
        databaseService.updateMedication(uid, med, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                HashMap<String, Medication> medsMap = user.getMedications();
                if (medsMap != null) {
                    medsMap.put(med.getId(), med);
                    user.setMedications(medsMap);
                    SharedPreferencesUtil.saveUser(MedicationListActivity.this, user);
                }
                Toast.makeText(MedicationListActivity.this, "התרופה עודכנה", Toast.LENGTH_SHORT).show();
                loadMedications();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMedicationById(String id) {
        databaseService.deleteMedication(uid, id, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                HashMap<String, Medication> medsMap = user.getMedications();
                if (medsMap != null) {
                    medsMap.remove(id);
                    user.setMedications(medsMap);
                    SharedPreferencesUtil.saveUser(MedicationListActivity.this, user);
                }
                loadMedications();
                Toast.makeText(MedicationListActivity.this, "התרופה נמחקה בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMedicationDialog(Medication medToEdit) {
        new MedicationDialog(this, medToEdit, uid, dateFormat, new MedicationDialog.OnMedicationSubmitListener() {
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
}