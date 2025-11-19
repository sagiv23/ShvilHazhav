package com.example.sagivproject.screens;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationAdapter;
import com.example.sagivproject.models.LogoutHelper;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.PagePermissions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MedicationListActivity extends AppCompatActivity {

    Button btnToMain, btnToContact, btnToDetailsAboutUser, btnAddMedication, btnToExit;
    RecyclerView recyclerViewMedications;

    MedicationAdapter adapter;
    ArrayList<Medication> medications = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_list);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        PagePermissions.checkUserPage(this);

        loadMedications();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.medicationListPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToMain = findViewById(R.id.btn_MedicationList_to_main);
        btnToContact = findViewById(R.id.btn_MedicationList_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_MedicationList_to_DetailsAboutUser);
        btnAddMedication = findViewById(R.id.bbtn_MedicationList_add_medication);
        btnToExit = findViewById(R.id.btn_MedicationList_to_exit);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, DetailsAboutUserActivity.class)));
        btnAddMedication.setOnClickListener(view -> openAddMedicationDialog());
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(MedicationListActivity.this));

        recyclerViewMedications = findViewById(R.id.recyclerView_medications);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicationAdapter(this, medications, new MedicationAdapter.OnMedicationActionListener() {
            @Override
            public void onEdit(int position) {
                openEditMedicationDialog(position);
            }

            @Override
            public void onDelete(int position) {
                deleteMedication(position);
            }
        });

        recyclerViewMedications.setAdapter(adapter);
    }

    // -------------------------------------------------------------
    // ● קריאת תרופות מה־Database דרך DatabaseService
    // -------------------------------------------------------------
    private void loadMedications() {
        DatabaseService.getInstance().getMedicationList(currentUser.getUid(),
                new DatabaseService.DatabaseCallback<List<Medication>>() {

                    @Override
                    public void onCompleted(List<Medication> list) {

                        medications.clear();
                        Date today = new Date();

                        for (Medication med : list) {
                            if (med == null || med.getId() == null)
                                continue;

                            // מחיקת תרופות שפג תוקפן
                            if (med.getDate() != null && med.getDate().before(today)) {
                                deleteMedicationById(med.getId());
                            } else {
                                medications.add(med);
                            }
                        }

                        sortMedicationsByDate();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(MedicationListActivity.this, "שגיאה בטעינת התרופות", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // -------------------------------------------------------------
    // ● הוספת תרופה חדשה
    // -------------------------------------------------------------
    private void saveMedication(Medication med) {
        DatabaseService.getInstance().addMedication(currentUser.getUid(), med, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                loadMedications();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בהוספת תרופה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------
    // ● עדכון תרופה
    // -------------------------------------------------------------
    private void updateMedication(Medication med) {
        DatabaseService.getInstance().updateMedication(currentUser.getUid(), med, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                loadMedications();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בעדכון התרופה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------
    // ● מחיקת תרופה לפי מיקום ברשימה
    // -------------------------------------------------------------
    private void deleteMedication(int index) {
        Medication med = medications.get(index);
        deleteMedicationById(med.getId());
    }

    private void deleteMedicationById(String id) {
        DatabaseService.getInstance().deleteMedication(currentUser.getUid(), id, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                loadMedications();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה במחיקת תרופה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------
    // ● דיאלוג הוספה / עריכה
    // -------------------------------------------------------------
    private void openAddMedicationDialog() {
        openMedicationDialog(null);
    }

    private void openEditMedicationDialog(int index) {
        openMedicationDialog(medications.get(index));
    }

    private void openMedicationDialog(Medication medToEdit) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_medication);
        dialog.setCancelable(true);

        EditText edtName = dialog.findViewById(R.id.edt_medication_name);
        EditText edtDetails = dialog.findViewById(R.id.edt_medication_details);
        EditText edtDate = dialog.findViewById(R.id.edt_medication_date);
        Button btnConfirm = dialog.findViewById(R.id.btn_add_medication_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_add_medication_cancel);

        if (medToEdit != null) {
            edtName.setText(medToEdit.getName());
            edtDetails.setText(medToEdit.getDetails());
            edtDate.setText(dateFormat.format(medToEdit.getDate()));
        }

        edtDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            if (medToEdit != null && medToEdit.getDate() != null)
                calendar.setTime(medToEdit.getDate());

            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH);
            int d = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, day) -> {
                String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                edtDate.setText(dateStr);
            }, y, m, d);

            picker.getDatePicker().setMinDate(System.currentTimeMillis());
            picker.show();
        });

        btnConfirm.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String details = edtDetails.getText().toString().trim();
            String dateStr = edtDate.getText().toString().trim();

            if (name.isEmpty() || details.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Date date = dateFormat.parse(dateStr);

                if (medToEdit == null) {
                    saveMedication(new Medication(name, details, date));
                } else {
                    medToEdit.setName(name);
                    medToEdit.setDetails(details);
                    medToEdit.setDate(date);
                    updateMedication(medToEdit);
                }

                dialog.dismiss();
            } catch (ParseException e) {
                Toast.makeText(this, "תאריך לא תקין", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // -------------------------------------------------------------
    // ● מיון לפי תאריך
    // -------------------------------------------------------------
    private void sortMedicationsByDate() {
        int i = 0;
        while (i < medications.size() - 1) {
            int j = i + 1;
            while (j < medications.size()) {
                if (medications.get(i).getDate().after(medications.get(j).getDate())) {
                    Medication temp = medications.get(i);
                    medications.set(i, medications.get(j));
                    medications.set(j, temp);
                }
                j++;
            }
            i++;
        }
    }
}
