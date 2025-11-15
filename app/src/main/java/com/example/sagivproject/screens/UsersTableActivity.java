package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
import com.example.sagivproject.utils.PagePermissions;
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

        PagePermissions.checkAdminPage(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.usersTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnToAdminPage = findViewById(R.id.btn_UsersTable_to_admin);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(UsersTableActivity.this, AdminPageActivity.class)));
        recyclerView = findViewById(R.id.recycler_UsersTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UsersTableAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        editSearch = findViewById(R.id.edit_UsersTable_search);
        spinnerSearchType = findViewById(R.id.spinner_UsersTable_search_type);

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