package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.models.LogoutHelper;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private Button btnToContact, btnToDetailsAboutUser, btnToMedicationList, btnToForum, btnToAi, btnToGameHomeScreen, btnToExit;
    private TextView txtHomePageTitle;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToContact = findViewById(R.id.btn_main_to_contact);
        btnToDetailsAboutUser = findViewById(R.id.btn_main_to_DetailsAboutUser);
        btnToMedicationList = findViewById(R.id.btn_main_to_MedicationList);
        btnToForum = findViewById(R.id.btn_main_to_Forum);
        btnToAi = findViewById(R.id.btn_main_to_Ai);
        btnToGameHomeScreen = findViewById(R.id.btn_main_to_GameHomeScreen);
        btnToExit = findViewById(R.id.btn_main_to_exit);

        btnToContact.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ContactActivity.class)));
        btnToDetailsAboutUser.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DetailsAboutUserActivity.class)));
        btnToMedicationList.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MedicationListActivity.class)));
        btnToForum.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ForumActivity.class)));
        btnToAi.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AiActivity.class)));
        btnToGameHomeScreen.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, GameHomeScreenActivity.class)));
        btnToExit.setOnClickListener(view -> LogoutHelper.logout(MainActivity.this));

        txtHomePageTitle = findViewById(R.id.txt_main_Title);

        User localUser = SharedPreferencesUtil.getUser(this);

        if (localUser != null) {
            showUserName(localUser);
        } else {
            //אחרת נטען מהשרת
            loadUserFromFirebase();
        }
    }

    private void showUserName(User user) {
        String fullName = user.getFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            txtHomePageTitle.setText("שלום מטופל יקר");
        } else {
            txtHomePageTitle.setText("שלום " + fullName);
        }
    }

    private void loadUserFromFirebase() {
        String uid = mAuth.getCurrentUser().getUid();

        usersRef.child(uid).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                SharedPreferencesUtil.saveUser(this, user);
                showUserName(user);
            } else {
                txtHomePageTitle.setText("שלום מטופל יקר");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בקריאת הנתונים", Toast.LENGTH_SHORT).show();
            txtHomePageTitle.setText("שלום מטופל יקר");
        });
    }
}