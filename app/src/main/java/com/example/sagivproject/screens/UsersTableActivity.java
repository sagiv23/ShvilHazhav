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
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.PagePermissions;

import java.util.ArrayList;
import java.util.List;

public class UsersTableActivity extends AppCompatActivity {
    private Button btnToAdminPage;
    private RecyclerView recyclerView;
    private UsersTableAdapter adapter;
    private final List<User> usersList = new ArrayList<>();
    private final List<User> filteredList = new ArrayList<>();
    private EditText editSearch;
    private Spinner spinnerSearchType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_table);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.usersTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PagePermissions.checkAdminPage(this);

        btnToAdminPage = findViewById(R.id.btn_UsersTable_to_admin);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(UsersTableActivity.this, AdminPageActivity.class)));

        recyclerView = findViewById(R.id.recycler_UsersTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersTableAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        editSearch = findViewById(R.id.edit_UsersTable_search);
        spinnerSearchType = findViewById(R.id.spinner_UsersTable_search_type);

        loadUsers();

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().trim());
            }
        });
    }

    private void loadUsers() {
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> list) {
                usersList.clear();

                for (User user : list) {
                    if (user != null && user.getUid() != null) {
                        usersList.add(user);
                    }
                }

                filteredList.clear();
                filteredList.addAll(usersList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersTableActivity.this, "שגיאה בהעלאת משתמשים", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        String searchType = spinnerSearchType.getSelectedItem().toString();

        for (User user : usersList) {
            if (query.isEmpty()) {
                filteredList.add(user);
            } else if (searchType.equals("שם פרטי") &&
                    user.getFirstName() != null &&
                    user.getFirstName().contains(query)) {
                filteredList.add(user);
            } else if (searchType.equals("שם משפחה") &&
                    user.getLastName() != null &&
                    user.getLastName().contains(query)) {
                filteredList.add(user);
            } else if (searchType.equals("אימייל") &&
                    user.getEmail() != null &&
                    user.getEmail().contains(query)) {
                filteredList.add(user);
            }
        }

        adapter.notifyDataSetChanged();
    }
}