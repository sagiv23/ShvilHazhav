package com.example.sagivproject.screens;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.UsersTableAdapter;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.UserRole;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An admin fragment for managing the list of all users.
 */
@AndroidEntryPoint
public class UsersTableFragment extends BaseFragment {
    private final List<User> usersList = new ArrayList<>();
    private UsersTableAdapter adapter;
    private EditText editSearch;
    private Spinner spinnerSearchType;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUser = sharedPreferencesUtil.getUser();

        Button btnAddUser = view.findViewById(R.id.btn_UsersTable_add_user);
        btnAddUser.setOnClickListener(v -> dialogService.showAddUserDialog(getParentFragmentManager(), (fName, lName, birthDateMillis, email, password) ->
                databaseService.getAuthService().addUser(fName, lName, birthDateMillis, email, password, new IAuthService.AddUserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        usersList.add(user);
                        refreshList();
                        if (getContext() != null)
                            Toast.makeText(getContext(), "משתמש נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        if (getContext() != null)
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                })));

        adapter = adapterService.getUsersTableAdapter();
        adapter.init(currentUser, new UsersTableAdapter.OnUserActionListener() {
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
                User userCopy = new User(clickedUser);
                dialogService.showEditUserDialog(getParentFragmentManager(), userCopy, (fName, lName, birthDate, email, password) ->
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

                                Toast.makeText(requireContext(), "פרטי המשתמש עודכנו", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(requireContext(), "שגיאה בעדכון: " + message, Toast.LENGTH_LONG).show();
                            }
                        }));
            }

            @Override
            public void onUserImageClicked(User user, ImageView imageView) {
                Drawable drawable = imageView.getDrawable();
                if (drawable != null)
                    dialogService.showFullImageDialog(getParentFragmentManager(), drawable);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recycler_UsersTable);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        editSearch = view.findViewById(R.id.edit_UsersTable_search);
        spinnerSearchType = view.findViewById(R.id.spinner_UsersTable_search_type);
        setupSearch();
    }

    private void setupSearch() {
        if (getContext() == null) return;
        spinnerSearchType.setAdapter(getStringArrayAdapter());

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

        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // "הכל"
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

    private void refreshList() {
        usersList.sort((u1, u2) -> u1.getFullName().compareToIgnoreCase(u2.getFullName()));
        filterUsers(editSearch.getText().toString().trim());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

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
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה בהעלאת משתמשים", Toast.LENGTH_LONG).show();
            }
        });
    }

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
                Toast.makeText(requireContext(), "הסטטוס עודכן", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה בעדכון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeleteUser(User user) {
        databaseService.getUserService().deleteUser(user.getId(), new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                if (user.getId().equals(currentUser.getId())) {
                    sharedPreferencesUtil.signOutUser();
                    navigateTo(R.id.loginFragment);
                    return;
                }
                usersList.remove(user);
                refreshList();
                Toast.makeText(requireContext(), "המשתמש נמחק", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

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

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter() {
        String[] searchOptions = {"הכל", "שם פרטי", "שם משפחה", "אימייל", "מנהלים", "משתמשים רגילים"};
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, searchOptions) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.text_hebrew));
                tv.setTextSize(20);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.text_hebrew));
                return tv;
            }
        };
    }
}
