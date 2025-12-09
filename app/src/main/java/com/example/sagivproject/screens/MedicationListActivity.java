package com.example.sagivproject.screens;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MedicationAdapter;
import com.example.sagivproject.models.Medication;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.LogoutHelper;
import com.example.sagivproject.utils.PagePermissions;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicationListActivity extends AppCompatActivity {
    Button btnToMain, btnToContact, btnToDetailsAboutUser, btnAddMedication, btnToExit;
    RecyclerView recyclerViewMedications;

    private MedicationAdapter adapter;
    private ArrayList<Medication> medications = new ArrayList<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private User savedUser;
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

        PagePermissions.checkUserPage(this);

        savedUser = SharedPreferencesUtil.getUser(this);
        uid = savedUser.getUid();

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
        DatabaseService.getInstance().getUserMedicationList(uid, new DatabaseService.DatabaseCallback<List<Medication>>() {
            @Override
            public void onCompleted(List<Medication> list) {
                medications.clear();

                Date today = new Date();
                List<String> expiredIds = new ArrayList<>();

                // חלוקה בין תקינים לפגים
                for (Medication med : list) {
                    if (med.getDate() != null && med.getDate().before(today)) {
                        expiredIds.add(med.getId());
                    } else {
                        medications.add(med);
                    }
                }

                // מחיקה של פגי תוקף
                for (String id : expiredIds) {
                    DatabaseService.getInstance().deleteMedication(uid, id, null);
                }

                // סידור רשימת התרופות התקינות
                medications.sort(Comparator.comparing(Medication::getDate));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMedication(Medication medication) {
        medication.setId(DatabaseService.getInstance().generateMedicationId(uid));

        DatabaseService.getInstance().createNewMedication(uid, medication, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
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
        DatabaseService.getInstance().updateMedication(uid, med, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
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
        DatabaseService.getInstance().deleteMedication(uid, id, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                loadMedications();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MedicationListActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMedicationDialog(Medication medToEdit) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_medication);

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

            DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
                edtDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.getDatePicker().setMinDate(System.currentTimeMillis());
            picker.show();
        });

        btnConfirm.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String details = edtDetails.getText().toString().trim();
            String dateString = edtDate.getText().toString().trim();

            if (name.isEmpty() || details.isEmpty() || dateString.isEmpty()) {
                Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Date date = dateFormat.parse(dateString);

                if (medToEdit == null) {
                    saveMedication(new Medication(name, details, date, uid));
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
}