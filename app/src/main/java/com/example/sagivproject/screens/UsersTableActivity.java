package com.example.sagivproject.screens;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.UsersTableAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.UserRole;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity providing an administrative interface for managing all application users.
 * <p>
 * This screen displays a comprehensive table of registered users and provides tools for:
 * <ul>
 * <li>Filtering users by name, email, or role (Admin/Regular).</li>
 * <li>Adding new user accounts manually.</li>
 * <li>Updating existing user profile information.</li>
 * <li>Toggling administrative privileges for accounts.</li>
 * <li>Deleting user accounts (including automatic logout if self-deleting).</li>
 * </ul>
 * It uses {@link UsersTableAdapter} to render user rows and manages data sorting and filtering.
 * </p>
 */
@AndroidEntryPoint
public class UsersTableActivity extends BaseActivity {
    /** Internal list of all users fetched from the database. */
    private final List<User> usersList = new ArrayList<>();

    private UsersTableAdapter adapter;
    private EditText editSearch;
    private Spinner spinnerSearchType;

    /** The profile of the currently logged-in administrator. */
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_table);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.usersTablePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        currentUser = sharedPreferencesUtil.getUser();

        Button btnAddUser = findViewById(R.id.btn_UsersTable_add_user);
        btnAddUser.setOnClickListener(v -> dialogService.showAddUserDialog(getSupportFragmentManager(), (fName, lName, birthDateMillis, email, password) ->
                databaseService.getAuthService().addUser(fName, lName, birthDateMillis, email, password, new IAuthService.AddUserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        usersList.add(user);
                        refreshList();
                        Toast.makeText(UsersTableActivity.this, "משתמש נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) { Toast.makeText(UsersTableActivity.this, message, Toast.LENGTH_LONG).show(); }
                })));

        adapter = adapterService.getUsersTableAdapter();
        adapter.init(currentUser, new UsersTableAdapter.OnUserActionListener() {
            @Override
            public void onToggleAdmin(User user) { handleToggleAdmin(user); }

            @Override
            public void onDeleteUser(User user) { handleDeleteUser(user); }

            @Override
            public void onUserClicked(User clickedUser) {
                User userCopy = new User(clickedUser);
                dialogService.showEditUserDialog(getSupportFragmentManager(), userCopy, (fName, lName, birthDate, email, password) ->
                        databaseService.getAuthService().updateUser(userCopy, fName, lName, birthDate, email, password, new IAuthService.UpdateUserCallback() {
                            @Override
                            public void onSuccess(User updatedUser) {
                                for (int i = 0; i < usersList.size(); i++) {
                                    if (usersList.get(i).getId().equals(updatedUser.getId())) {
                                        usersList.set(i, updatedUser);
                                        break;
                                    }
                                }
                                if (updatedUser.getId().equals(currentUser.getId())) {
                                    sharedPreferencesUtil.saveUser(updatedUser);
                                    currentUser = updatedUser;
                                }
                                refreshList();

                                Toast.makeText(UsersTableActivity.this, "פרטי המשתמש עודכנו", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) { Toast.makeText(UsersTableActivity.this, "שגיאה בעדכון: " + message, Toast.LENGTH_LONG).show(); }
                        }));
            }

            @Override
            public void onUserImageClicked(User user, ImageView imageView) {
                Drawable drawable = imageView.getDrawable();
                if (drawable != null)
                    dialogService.showFullImageDialog(getSupportFragmentManager(), drawable);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_UsersTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        editSearch = findViewById(R.id.edit_UsersTable_search);
        spinnerSearchType = findViewById(R.id.spinner_UsersTable_search_type);
        setupSearch();
    }

    /** Configures the search UI, including the type filter spinner and text listener. */
    private void setupSearch() {
        spinnerSearchType.setAdapter(getStringArrayAdapter());

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { filterUsers(s.toString().trim()); }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editSearch.setText("");
                    editSearch.setEnabled(false);
                } else {
                    editSearch.setEnabled(true);
                }
                filterUsers(editSearch.getText().toString().trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /** Sorts the user list alphabetically by full name and reapplies the current search filter. */
    private void refreshList() {
        usersList.sort((u1, u2) -> u1.getFullName().compareToIgnoreCase(u2.getFullName()));
        filterUsers(editSearch.getText().toString().trim());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    /** Fetches the complete user database from Firebase. */
    private void loadUsers() {
        databaseService.getUserService().getUserList(new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> list) {
                usersList.clear();
                if (list != null) {
                    usersList.addAll(list.stream().filter(u -> u != null && u.getId() != null).collect(Collectors.toList()));
                }
                refreshList();
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(UsersTableActivity.this, "שגיאה בהעלאת משתמשים", Toast.LENGTH_LONG).show(); }
        });
    }

    /**
     * Toggles the user role between ADMIN and REGULAR and commits the change to the database.
     * @param user The user whose role is being modified.
     */
    private void handleToggleAdmin(User user) {
        UserRole newRole = user.getRole() == UserRole.ADMIN ? UserRole.REGULAR : UserRole.ADMIN;
        databaseService.getUserService().updateUserRole(user.getId(), newRole, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                User updatedUser = new User(user);
                updatedUser.setRole(newRole);

                for (int i = 0; i < usersList.size(); i++) {
                    if (usersList.get(i).getId().equals(updatedUser.getId())) {
                        usersList.set(i, updatedUser);
                        break;
                    }
                }

                if (updatedUser.getId().equals(currentUser.getId())) {
                    sharedPreferencesUtil.saveUser(updatedUser);
                    currentUser = updatedUser;
                }
                refreshList();
                Toast.makeText(UsersTableActivity.this, "הסטטוס עודכן", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(UsersTableActivity.this, "שגיאה בעדכון", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Permanently deletes a user account from the database.
     * <p>
     * If the target user is the currently logged-in account, it triggers a full logout
     * and redirects to the landing screen.
     * </p>
     * @param user The user account to remove.
     */
    private void handleDeleteUser(User user) {
        databaseService.getUserService().deleteUser(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                if (user.getId().equals(currentUser.getId())) {
                    sharedPreferencesUtil.signOutUser();
                    Intent intent = new Intent(UsersTableActivity.this, LandingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }
                usersList.remove(user);
                refreshList();
                Toast.makeText(UsersTableActivity.this, "המשתמש נמחק", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) { Toast.makeText(UsersTableActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show(); }
        });
    }

    /**
     * Filters the user list based on the search query and selected attribute.
     * @param query The search string.
     */
    private void filterUsers(String query) {
        String lowerQuery = query.toLowerCase();
        String selectedType = spinnerSearchType.getSelectedItem() != null ? spinnerSearchType.getSelectedItem().toString() : "הכל";

        List<User> filtered = usersList.stream().filter(user -> {
            if (query.isEmpty() && selectedType.equals("הכל")) return true;
            switch (selectedType) {
                case "שם פרטי":
                    return user.getFirstName() != null && user.getFirstName().toLowerCase().contains(lowerQuery);
                case "שם משפחה":
                    return user.getLastName() != null && user.getLastName().toLowerCase().contains(lowerQuery);
                case "אימייל":
                    return user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery);
                case "מנהלים":
                    return user.isAdmin() && user.getFullName().toLowerCase().contains(lowerQuery);
                case "משתמשים רגילים":
                    return !user.isAdmin() && user.getFullName().toLowerCase().contains(lowerQuery);
                default:
                    return user.getFullName().toLowerCase().contains(lowerQuery) || (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery));
            }
        }).collect(Collectors.toList());

        adapter.setUserList(filtered);
    }

    /** Helper to create a styled adapter for the search filter dropdown. */
    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter() {
        String[] searchOptions = {"הכל", "שם פרטי", "שם משפחה", "אימייל", "מנהלים", "משתמשים רגילים"};
        return new ArrayAdapter<>(UsersTableActivity.this, android.R.layout.simple_spinner_item, searchOptions) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(UsersTableActivity.this, R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(getColor(R.color.text_color));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(UsersTableActivity.this, R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(getColor(R.color.text_color));
                tv.setBackgroundColor(getColor(R.color.background_color_buttons));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };
    }
}