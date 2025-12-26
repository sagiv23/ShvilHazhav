package com.example.sagivproject.screens;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.UsersTableAdapter;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.EditUserDialog;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class UsersTableActivity extends BaseActivity {
    private Button btnToAdminPage;
    private RecyclerView recyclerView;
    private UsersTableAdapter adapter;
    private final List<User> usersList = new ArrayList<>();
    private final List<User> filteredList = new ArrayList<>();
    private EditText editSearch;
    private Spinner spinnerSearchType;
    private User currentUser;

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

        btnToAdminPage = findViewById(R.id.btn_UsersTable_to_admin);
        btnToAdminPage.setOnClickListener(view -> startActivity(new Intent(UsersTableActivity.this, AdminPageActivity.class)));

        currentUser = SharedPreferencesUtil.getUser(UsersTableActivity.this);

        adapter = new UsersTableAdapter(filteredList, currentUser, new UsersTableAdapter.OnUserActionListener() {
            @Override
            public void onToggleAdmin(User user) {
                handleToggleAdmin(user);
            }

            @Override
            public void onDeleteUser(User user) {
                handleDeleteUser(user);
            }

            @Override
            public void onUserClicked(User clickedUser) {
                new EditUserDialog(UsersTableActivity.this, clickedUser, () -> { loadUsers(); }).show();
            }
        });

        recyclerView = findViewById(R.id.recycler_UsersTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        editSearch = findViewById(R.id.edit_UsersTable_search);
        spinnerSearchType = findViewById(R.id.spinner_UsersTable_search_type);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().trim());
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.search_types)
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                Typeface typeface = ResourcesCompat.getFont(UsersTableActivity.this, R.font.text_hebrew);
                tv.setTypeface(typeface);
                tv.setTextColor(getResources().getColor(R.color.text_color, null));
                tv.setBackgroundColor(getResources().getColor(R.color.background_color_buttons, null));

                tv.setTextSize(22);
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                Typeface typeface = ResourcesCompat.getFont(UsersTableActivity.this, R.font.text_hebrew);
                tv.setTypeface(typeface);
                tv.setTextColor(getResources().getColor(R.color.text_color, null));
                tv.setBackgroundColor(getResources().getColor(R.color.background_color_buttons, null));

                tv.setTextSize(22);
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };

        spinnerSearchType.setAdapter(adapter);

        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterUsers(editSearch.getText().toString().trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
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

    private void handleToggleAdmin(User user) {
        boolean newRole = !user.getIsAdmin();

        databaseService.updateUserAdminStatus(user.getUid(), newRole, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(UsersTableActivity.this, "הסטטוס עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                loadUsers(); //רענון
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersTableActivity.this, "שגיאה בעדכון סטטוס", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeleteUser(User user) {
        boolean isSelf = user.equals(currentUser);
        databaseService.deleteUser(user.getUid(), new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                if (isSelf) {
                    SharedPreferencesUtil.signOutUser(UsersTableActivity.this);
                    Intent intent = new Intent(UsersTableActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }

                Toast.makeText(UsersTableActivity.this, "המשתמש נמחק", Toast.LENGTH_SHORT).show();
                loadUsers();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersTableActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        String searchType = spinnerSearchType.getSelectedItem().toString();
        String lowerQuery = query.toLowerCase();

        switch (searchType) {

            case "מנהלים":
                for (User user : usersList) {
                    if (user.getIsAdmin() &&
                            user.getFullName().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(user);
                    }
                }
                break;

            case "משתמשים רגילים":
                for (User user : usersList) {
                    if (!user.getIsAdmin() &&
                            user.getFullName().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(user);
                    }
                }
                break;

            case "ניצחונות":
                for (User user : usersList) {
                    if (user.getFullName().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(user);
                    }
                }
                filteredList.sort(
                        (u1, u2) -> Integer.compare(u2.getCountWins(), u1.getCountWins())
                );
                break;

            case "שם פרטי":
                for (User user : usersList) {
                    if (user.getFirstName() != null &&
                            user.getFirstName().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(user);
                    }
                }
                break;

            case "שם משפחה":
                for (User user : usersList) {
                    if (user.getLastName() != null &&
                            user.getLastName().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(user);
                    }
                }
                break;

            case "אימייל":
                for (User user : usersList) {
                    if (user.getEmail() != null &&
                            user.getEmail().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(user);
                    }
                }
                break;
        }

        adapter.notifyDataSetChanged();
    }
}