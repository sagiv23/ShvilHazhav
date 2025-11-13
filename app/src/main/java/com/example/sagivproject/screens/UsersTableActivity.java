package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.UsersTableAdapter;
import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UsersTableActivity extends AppCompatActivity {
    private Button btnToAdminPage;
    private RecyclerView recyclerView;
    private UsersTableAdapter adapter;
    private final List<User> usersList = new ArrayList<>();
    private final List<User> filteredList = new ArrayList<>();
    private DatabaseReference usersRef;
    private EditText editSearch;
    private Spinner spinnerSearchType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_table);

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
            Intent intent = new Intent(this, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.usersTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToAdminPage = findViewById(R.id.btnUsersTablePageToAdminPage);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(UsersTableActivity.this, AdminPageActivity.class)));
        recyclerView = findViewById(R.id.recyclerUsersTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UsersTableAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        editSearch = findViewById(R.id.editSearch);
        spinnerSearchType = findViewById(R.id.spinnerSearchTypeUsersTable);

        loadUsers();

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void loadUsers () {
        usersRef.get().addOnSuccessListener(snapshot -> {
            usersList.clear();
            for (var child : snapshot.getChildren()) {
                User user = child.getValue(User.class);
                if (user != null) {
                    user.setUid(child.getKey());
                    usersList.add(user);
                }
            }
            filteredList.clear();
            filteredList.addAll(usersList);
            adapter.notifyDataSetChanged();
        });
    }

    private void filterUsers (String query){
        filteredList.clear();
        String searchType = spinnerSearchType.getSelectedItem().toString();

        for (User user : usersList) {
            if (query.isEmpty()) {
                filteredList.add(user);
            } else if (searchType.equals("שם פרטי") && user.getFirstName() != null && user.getFirstName().contains(query)) {
                filteredList.add(user);
            } else if (searchType.equals("שם משפחה") && user.getLastName() != null && user.getLastName().contains(query)) {
                filteredList.add(user);
            } else if (searchType.equals("אימייל") && user.getEmail() != null && user.getEmail().contains(query)) {
                filteredList.add(user);
            }
        }
        adapter.notifyDataSetChanged();
    }
}