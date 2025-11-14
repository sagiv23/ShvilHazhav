package com.example.sagivproject.screens.models;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.example.sagivproject.screens.screens.LoginActivity;
import com.example.sagivproject.screens.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutHelper {
    public static void logout(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_logout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        Button btnConfirm = dialog.findViewById(R.id.btnLogoutConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnLogoutCancel);

        btnConfirm.setOnClickListener(v -> {
            String userEmail = "";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }

            SharedPreferencesUtil.signOutUser(context);

            FirebaseAuth.getInstance().signOut();
            Toast.makeText(context, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra("userEmail", userEmail);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}