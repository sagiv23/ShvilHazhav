package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;

public class EditUserDialog {
    private final Context context;
    private final User user;
    private final Runnable onSuccess;

    public EditUserDialog(Context context, User user, Runnable onSuccess) {
        this.context = context;
        this.user = user;
        this.onSuccess = onSuccess;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_user);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText inputFirstName = dialog.findViewById(R.id.inputEditUserFirstName);
        EditText inputLastName = dialog.findViewById(R.id.inputEditUserLastName);
        EditText inputPassword = dialog.findViewById(R.id.inputEditUserPassword);
        Button btnSave = dialog.findViewById(R.id.btnEditUserSave);
        Button btnCancel = dialog.findViewById(R.id.btnEditUserCancel);

        inputFirstName.setText(user.getFirstName());
        inputLastName.setText(user.getLastName());
        inputPassword.setText(user.getPassword());

        btnSave.setOnClickListener(v -> {
            String fName = inputFirstName.getText().toString().trim();
            String lName = inputLastName.getText().toString().trim();
            String pass = inputPassword.getText().toString().trim();

            if (fName.isEmpty() || lName.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "כל השדות חובה", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setFirstName(fName);
            user.setLastName(lName);
            user.setPassword(pass);

            DatabaseService.getInstance().updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    Toast.makeText(context, "הפרטים עודכנו!", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) onSuccess.run();
                    dialog.dismiss();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(context, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}