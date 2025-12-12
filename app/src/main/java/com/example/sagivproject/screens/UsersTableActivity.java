package com.example.sagivproject.screens;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
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
import com.example.sagivproject.utils.SharedPreferencesUtil;

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

        User currentUser = SharedPreferencesUtil.getUser(UsersTableActivity.this);

        adapter = new UsersTableAdapter(filteredList, currentUser, new UsersTableAdapter.OnUserActionListener() {
            @Override
            public void onToggleAdmin(User user) {
                boolean newRole = !user.getIsAdmin();

                DatabaseService.getInstance().updateUserAdminStatus(
                        user.getUid(),
                        newRole,
                        new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Toast.makeText(UsersTableActivity.this,
                                        "הסטטוס עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                                loadUsers(); //רענון
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(UsersTableActivity.this, "שגיאה בעדכון סטטוס", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

            @Override
            public void onDeleteUser(User user) {

                boolean isSelf = user.equals(currentUser);
                DatabaseService.getInstance().deleteUser(user.getUid(), new DatabaseService.DatabaseCallback<Void>() {
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

        /*------ שינוי צבעים בבחירה לחיפוש -----*/
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item, // layout פנימי בסיסי
                getResources().getStringArray(R.array.search_types)
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                Typeface typeface = ResourcesCompat.getFont(UsersTableActivity.this, R.font.text);
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
                Typeface typeface = ResourcesCompat.getFont(UsersTableActivity.this, R.font.text);
                tv.setTypeface(typeface);
                tv.setTextColor(getResources().getColor(R.color.text_color, null));
                tv.setBackgroundColor(getResources().getColor(R.color.background_color_buttons, null));

                tv.setTextSize(22);
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };

        spinnerSearchType.setAdapter(adapter);
        /*------ סוף -----*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
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
            } else if (searchType.equals("מנהלים") &&
                    query.equals("כן") &&
                    user.getIsAdmin()) {
                filteredList.add(user);
            } else if (searchType.equals("משתמשים רגילים") &&
                    query.equals("לא") &&
                    !user.getIsAdmin()) {
                filteredList.add(user);
            }
        }

        adapter.notifyDataSetChanged();
    }
}