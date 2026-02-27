package com.example.sagivproject.screens;

import android.content.Intent;
import android.graphics.Typeface;
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
import com.example.sagivproject.screens.dialogs.AddUserDialog;
import com.example.sagivproject.screens.dialogs.EditUserDialog;
import com.example.sagivproject.screens.dialogs.FullImageDialog;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An admin activity for managing the list of all users in the application.
 * <p>
 * This screen displays a table of users, allowing administrators to add, edit, delete,
 * and search for users. It also provides the functionality to promote or demote users
 * to/from admin status.
 * </p>
 */
@AndroidEntryPoint
public class UsersTableActivity extends BaseActivity {
    private final List<User> usersList = new ArrayList<>();
    private UsersTableAdapter adapter;
    private EditText editSearch;
    private Spinner spinnerSearchType;
    private User currentUser;
    private Typeface textFont;
    private int textColor, backgroundColor;

    /**
     * Initializes the activity, sets up the UI, RecyclerView, search/filter functionality,
     * and action listeners for user management.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        currentUser = sharedPreferencesUtil.getUser();

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        Button btnAddUser = findViewById(R.id.btn_UsersTable_add_user);
        btnAddUser.setOnClickListener(v -> new AddUserDialog(this, (fName, lName, birthDateMillis, email, password) -> databaseService.getAuthService().addUser(fName, lName, birthDateMillis, email, password, new IAuthService.AddUserCallback() {
            @Override
            public void onSuccess(User user) {
                usersList.add(user);
                filterUsers(editSearch.getText().toString().trim());
                Toast.makeText(UsersTableActivity.this, "משתמש נוסף בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UsersTableActivity.this, message, Toast.LENGTH_LONG).show();
            }
        })).show());

        adapter = new UsersTableAdapter(currentUser,
                new UsersTableAdapter.OnUserActionListener() {

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
                        new EditUserDialog(
                                UsersTableActivity.this,
                                clickedUser,
                                (fName, lName, birthDate, email, password) -> databaseService.getAuthService().updateUser(clickedUser, fName, lName, birthDate, email, password, new IAuthService.UpdateUserCallback() {
                                    @Override
                                    public void onSuccess(User updatedUser) {
                                        Toast.makeText(UsersTableActivity.this, "פרטי המשתמש עודכנו", Toast.LENGTH_SHORT).show();
                                        loadUsers();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(UsersTableActivity.this, "שגיאה בעדכון: " + message, Toast.LENGTH_LONG).show();
                                    }
                                })
                        ).show();
                    }

                    @Override
                    public void onUserImageClicked(User user, ImageView imageView) {
                        String base64Image = user.getProfileImage();
                        if (base64Image == null || base64Image.isEmpty()) return;

                        Drawable drawable = imageView.getDrawable();
                        if (drawable == null) return;

                        new FullImageDialog(UsersTableActivity.this, drawable).show();
                    }
                });

        RecyclerView recyclerView = findViewById(R.id.recycler_UsersTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        editSearch = findViewById(R.id.edit_UsersTable_search);
        spinnerSearchType = findViewById(R.id.spinner_UsersTable_search_type);

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

        textFont = ResourcesCompat.getFont(this, R.font.text_hebrew);
        textColor = getResources().getColor(R.color.text_color, null);
        backgroundColor = getResources().getColor(R.color.background_color_buttons, null);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.search_types)
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTypeface(textFont);
                tv.setTextColor(textColor);
                tv.setBackgroundColor(backgroundColor);
                tv.setTextSize(22);
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTypeface(textFont);
                tv.setTextColor(textColor);
                tv.setBackgroundColor(backgroundColor);
                tv.setTextSize(22);
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };

        spinnerSearchType.setAdapter(spinnerAdapter);

        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterUsers(editSearch.getText().toString().trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Reloads the user list from the database when the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    /**
     * Fetches the complete list of users from the database and updates the UI.
     */
    private void loadUsers() {
        databaseService.getUserService().getUserList(new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> list) {
                usersList.clear();
                usersList.addAll(list.stream().filter(u -> u != null && u.getId() != null).collect(Collectors.toList()));
                filterUsers(editSearch.getText().toString().trim());
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersTableActivity.this, "שגיאה בהעלאת משתמשים", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Toggles the admin status of a user.
     *
     * @param user The user whose admin status is to be toggled.
     */
    private void handleToggleAdmin(User user) {
        UserRole newRole = user.getRole() == UserRole.ADMIN ? UserRole.REGULAR : UserRole.ADMIN;

        databaseService.getUserService().updateUserRole(user.getId(), newRole, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(
                        UsersTableActivity.this,
                        "הסטטוס עודכן בהצלחה",
                        Toast.LENGTH_SHORT
                ).show();
                user.setRole(newRole);
                filterUsers(editSearch.getText().toString().trim());
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(
                        UsersTableActivity.this,
                        "שגיאה בעדכון סטטוס",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    /**
     * Handles the deletion of a user. If the admin deletes their own account, they are logged out.
     *
     * @param user The user to be deleted.
     */
    private void handleDeleteUser(User user) {
        boolean isSelf = user.equals(currentUser);
        databaseService.getUserService().deleteUser(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                if (isSelf) {
                    sharedPreferencesUtil.signOutUser();
                    Intent intent = new Intent(UsersTableActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }

                Toast.makeText(UsersTableActivity.this, "המשתמש נמחק", Toast.LENGTH_SHORT).show();
                usersList.remove(user);
                filterUsers(editSearch.getText().toString().trim());
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(UsersTableActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filters the list of users based on the search query and the selected filter type.
     *
     * @param query The search text entered by the admin.
     */
    private void filterUsers(String query) {
        String searchType = spinnerSearchType.getSelectedItem().toString();
        String lowerQuery = query.toLowerCase();

        List<User> newFilteredList;

        switch (searchType) {
            case "מנהלים":
                newFilteredList = usersList.stream()
                        .filter(user -> user.isAdmin() && user.getFullName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                break;

            case "משתמשים רגילים":
                newFilteredList = usersList.stream()
                        .filter(user -> !user.isAdmin() && user.getFullName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                break;

            case "ניצחונות":
                newFilteredList = usersList.stream()
                        .filter(user -> user.getFullName().toLowerCase().contains(lowerQuery))
                        .sorted((u1, u2) -> Integer.compare(u2.getCountWins(), u1.getCountWins()))
                        .collect(Collectors.toList());
                break;

            case "שם פרטי":
                newFilteredList = usersList.stream()
                        .filter(user -> user.getFirstName() != null && user.getFirstName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                break;

            case "שם משפחה":
                newFilteredList = usersList.stream()
                        .filter(user -> user.getLastName() != null && user.getLastName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                break;

            case "אימייל":
                newFilteredList = usersList.stream()
                        .filter(user -> String.valueOf(user.getCountWins()).contains(lowerQuery))
                        .collect(Collectors.toList());
                break;

            case "הכל":
            default:
                newFilteredList = usersList.stream()
                        .filter(user -> user.getFullName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                break;
        }

        adapter.setUserList(newFilteredList);
    }
}
