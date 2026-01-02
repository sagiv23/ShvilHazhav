package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.InputValidator;

import java.util.HashMap;

public class AddUserDialog {
    public interface AddUserListener {
        void onUserAdded(User newUser);
    }

    private final Context context;
    private final AddUserListener listener;

    public AddUserDialog(Context context, AddUserListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_user);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText inputFirstName = dialog.findViewById(R.id.inputAddUserFirstName);
        EditText inputLastName = dialog.findViewById(R.id.inputAddUserLastName);
        EditText inputEmail = dialog.findViewById(R.id.inputAddUserEmail);
        EditText inputPassword = dialog.findViewById(R.id.inputAddUserPassword);

        Button btnAdd = dialog.findViewById(R.id.btnAddUserSave);
        Button btnCancel = dialog.findViewById(R.id.btnAddUserCancel);

        btnAdd.setOnClickListener(v -> {
            String fName = inputFirstName.getText().toString().trim();
            String lName = inputLastName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            } if (!InputValidator.isNameValid(fName)) {
                inputFirstName.requestFocus();
                Toast.makeText(context, "שם פרטי קצר מדי", Toast.LENGTH_LONG).show();
                return;
            } if (!InputValidator.isNameValid(lName)) {
                inputLastName.requestFocus();
                Toast.makeText(context, "שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
                return;
            } if (!InputValidator.isEmailValid(email)) {
                inputEmail.requestFocus();
                Toast.makeText(context, "כתובת האימייל לא תקינה", Toast.LENGTH_LONG).show();
                return;
            } if (!InputValidator.isPasswordValid(password)) {
                inputPassword.requestFocus();
                Toast.makeText(context, "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
                return;
            }

            String uid = DatabaseService.getInstance().generateUserId();
            User newUser = new User(uid, fName, lName, email, password
                    ,false
                    ,null
                    ,new HashMap<>()
                    ,0);

            DatabaseService.getInstance().checkIfEmailExists(email, new DatabaseService.DatabaseCallback<Boolean>() {
                @Override
                public void onCompleted(Boolean exists) {
                    if (exists) {
                        Toast.makeText(context, "אימייל זה תפוס", Toast.LENGTH_SHORT).show();
                    } else {
                        DatabaseService.getInstance().createNewUser(newUser, new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                if (listener != null) {
                                    listener.onUserAdded(newUser);
                                }

                                Toast.makeText(context, "משתמש נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Toast.makeText(context, "שגיאה בשמירה", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(context, "שגיאה בבדיקת אימייל", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}