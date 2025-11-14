package com.example.sagivproject.screens.screens;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.screens.adapters.MedicationAdapter;
import com.example.sagivproject.screens.models.LogoutHelper;
import com.example.sagivproject.screens.models.Medication;
import com.example.sagivproject.screens.models.User;
import com.example.sagivproject.screens.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicationListActivity extends AppCompatActivity {
    Button btnToMain, btnToContact, btnToDetailsAboutUser, btnAddMedication, btnToExit;
    RecyclerView recyclerViewMedications;
    MedicationAdapter adapter;
    ArrayList<Medication> medications = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_list);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        User savedUser = SharedPreferencesUtil.getUser(this);
        if (savedUser == null) {
            //לא מחובר - Login
            Toast.makeText(this, "אין לך גישה לדף זה - אתה לא מחובר!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        if (savedUser.getIsAdmin()) {
            //מנהל - HomePage
            Toast.makeText(this, "ניסיון יפה, מנהל! אבל לצערנו, אין לך גישה", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AdminPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid())
                .child("medications");

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

        // מאזין לשינויים ב-Firebase ומוחק תרופות שפג תוקפן
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                medications.clear();
                Date today = new Date();
                int deletedCount = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    Medication med = child.getValue(Medication.class);
                    if (med != null) {
                        med.setId(child.getKey());
                        if (med.getDate() != null && med.getDate().before(today)) {
                            // אם תאריך התרופה לפני היום — נמחק אותה
                            child.getRef().removeValue();
                            deletedCount++;
                        } else {
                            medications.add(med);
                        }
                    }
                }

                sortMedicationsByDate();
                adapter.notifyDataSetChanged();

                if (deletedCount > 0) {
                    Toast.makeText(MedicationListActivity.this,
                            "נמחקו תרופות שפג תוקפן", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MedicationListActivity.this,
                        "שגיאה בטעינת התרופות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddMedicationDialog() {
        openMedicationDialog(null, -1);
    }

    private void openEditMedicationDialog(int index) {
        Medication med = medications.get(index);
        openMedicationDialog(med, index);
    }

    private void openMedicationDialog(Medication medToEdit, int index) {
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

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
                String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, (m + 1), d);
                edtDate.setText(dateStr);
            }, year, month, day);

            picker.getDatePicker().setMinDate(System.currentTimeMillis());
            picker.show();

            boolean isDarkMode =
                    (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                            == android.content.res.Configuration.UI_MODE_NIGHT_YES;

            // ✅ קובע צבע טקסט אחיד לכל האלמנטים בלוח השנה
            int textColor = ContextCompat.getColor(this, isDarkMode ? android.R.color.white : android.R.color.black);

            // ✅ צבע רקע לכותרת העליונה (במקום האפור הכהה במצב לילה)
            int headerColor = ContextCompat.getColor(this, isDarkMode ? R.color.background_color_nav : R.color.background_color_nav);

            // צבע הכפתורים
            picker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(textColor);
            picker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(textColor);

            // שינוי צבעים של תצוגת ה־DatePicker עצמו
            picker.getDatePicker().setCalendarViewShown(false);
            picker.getDatePicker().post(() -> {
                try {
                    // משנה צבע טקסט לכותרת העליונה (שנה + תאריך)
                    int yearId = getResources().getIdentifier("date_picker_header_year", "id", "android");
                    int dateId = getResources().getIdentifier("date_picker_header_date", "id", "android");
                    int headerId = getResources().getIdentifier("date_picker_header", "id", "android");

                    TextView yearView = picker.findViewById(yearId);
                    TextView dateView = picker.findViewById(dateId);
                    View headerView = picker.findViewById(headerId);

                    if (yearView != null) yearView.setTextColor(textColor);
                    if (dateView != null) dateView.setTextColor(textColor);
                    if (headerView != null) headerView.setBackgroundColor(headerColor);

                    // משנה צבע לכל התאריכים בלוח
                    ViewGroup dp = (ViewGroup) picker.getDatePicker();
                    changeAllTextViewsColor(dp, textColor);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
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
                    Medication newMed = new Medication(name, details, date);
                    saveMedicationToFirebase(newMed);
                } else {
                    medToEdit.setName(name);
                    medToEdit.setDetails(details);
                    medToEdit.setDate(date);
                    updateMedicationInFirebase(medToEdit);
                }
                dialog.dismiss();
            } catch (ParseException e) {
                Toast.makeText(this, "תאריך לא תקין", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void changeAllTextViewsColor(ViewGroup viewGroup, int color) {
        int i = 0;
        while (i < viewGroup.getChildCount()) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(color);
            } else if (v instanceof ViewGroup) {
                changeAllTextViewsColor((ViewGroup) v, color);
            }
            i++;
        }
    }

    private void saveMedicationToFirebase(Medication med) {
        if (currentUser == null) return;
        String id = databaseRef.push().getKey();
        med.setId(id);
        if (id != null) databaseRef.child(id).setValue(med);
    }

    private void updateMedicationInFirebase(Medication med) {
        if (currentUser == null || med.getId() == null) return;
        databaseRef.child(med.getId()).setValue(med);
    }

    private void deleteMedication(int index) {
        if (currentUser == null) return;
        Medication med = medications.get(index);
        if (med.getId() != null) databaseRef.child(med.getId()).removeValue();
    }

    private void sortMedicationsByDate() {
        int i = 0;
        while (i < medications.size() - 1) {
            int j = i + 1;
            while (j < medications.size()) {
                if (medications.get(i).getDate() != null && medications.get(j).getDate() != null) {
                    if (medications.get(i).getDate().after(medications.get(j).getDate())) {
                        Medication temp = medications.get(i);
                        medications.set(i, medications.get(j));
                        medications.set(j, temp);
                    }
                }
                j++;
            }
            i++;
        }
    }
}
