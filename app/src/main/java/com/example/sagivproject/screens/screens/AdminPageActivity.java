package com.example.sagivproject.screens.screens;

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
import com.example.sagivproject.screens.models.LogoutHelper;
import com.example.sagivproject.screens.models.User;
import com.example.sagivproject.screens.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminPageActivity extends AppCompatActivity {
    private Button btnToUserTable, btnLogout;
    private TextView txtAdminTitle;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);

        User savedUser = SharedPreferencesUtil.getUser(this);
        if (savedUser == null) {
            //לא מחובר - Login
            Toast.makeText(this, "אין לך גישה לדף זה - אתה לא מחובר!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        if (!savedUser.getIsAdmin()) {
            //מחובר אבל לא מנהל - HomePage
            Toast.makeText(this, "אין לך גישה לדף זה", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToUserTable = findViewById(R.id.btn_admin_to_UsersTablePage);
        btnLogout = findViewById(R.id.btn_admin_to_exit);
        txtAdminTitle = findViewById(R.id.txt_admin_title);
        btnToUserTable.setOnClickListener(view -> startActivity(new Intent(AdminPageActivity.this, UsersTableActivity.class)));
        btnLogout.setOnClickListener(view -> LogoutHelper.logout(this));

        User localUser = SharedPreferencesUtil.getUser(this);
        if (localUser != null) {
            showUserName(localUser);
        } else {
            loadUserFromFirebase();
        }
    }

    private void showUserName(User user) {
        String fullName = user.getFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            txtAdminTitle.setText("שלום מנהל יקר");
        } else {
            txtAdminTitle.setText("שלום " + fullName);
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
                txtAdminTitle.setText("שלום מנהל יקר");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בטעינת נתוני המשתמש", Toast.LENGTH_SHORT).show();
            txtAdminTitle.setText("שלום מנהל יקר");
        });
    }
}