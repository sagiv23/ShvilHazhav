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
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MedicationListActivity extends AppCompatActivity {
    Button btnToMain, btnToContact, btnToDetailsAboutUser, btnAddMedication, btnToExit;
    RecyclerView recyclerViewMedications;

    private MedicationAdapter adapter;

    ArrayList<Medication> medications = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_list);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.medicationListPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkUserPage(this);

        btnToMain = findViewById(R.id.btn_MedicationList_to_main);
        btnToContact = findViewById(R.id.btn_MedicationList_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_MedicationList_to_DetailsAboutUser);
        btnAddMedication = findViewById(R.id.btn_MedicationList_add_medication);
        btnToExit = findViewById(R.id.btn_MedicationList_to_exit);

        btnToMain.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, MainActivity.class)));
        btnToContact.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(MedicationListActivity.this, DetailsAboutUserActivity.class)));
        btnAddMedication.setOnClickListener(view -> openMedicationDialog(null));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(MedicationListActivity.this));

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
        String uid = SharedPreferencesUtil.getUserId(this);
        if (uid == null) {
            Toast.makeText(this, "שגיאה: משתמש לא מזוהה", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        DatabaseService.getInstance().getMedicationList(uid, new DatabaseService.DatabaseCallback<List<Medication>>() {
            @Override
            public void onCompleted(List<Medication> list) {
                medications.clear();
                Date today = new Date();

                for (Medication med : list) {
                    if (med == null || med.getId() == null) continue;
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


    private void saveMedication(Medication medication) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "עליך להתחבר כדי להוסיף תרופה", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        String uid = user.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("medications")
                .child(uid);

        String key = ref.push().getKey();
        ref.child(key).setValue(medication)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "התרופה נשמרה", Toast.LENGTH_SHORT).show();
                    loadMedications();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בשמירת תרופה", Toast.LENGTH_SHORT).show();
                });
    }


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

    private void sortMedicationsByDate() {
        medications.sort(Comparator.comparing(Medication::getDate));
    }
}
