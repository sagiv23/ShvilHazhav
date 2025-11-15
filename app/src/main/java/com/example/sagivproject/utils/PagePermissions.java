package com.example.sagivproject.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.AdminPageActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.MainActivity;

public class PagePermissions {
    // 1) כניסה רק למנהלים
    public static void checkAdminPage(Activity activity) {
        User savedUser = SharedPreferencesUtil.getUser(activity);

        if (savedUser == null) {
            Toast.makeText(activity, "אין לך גישה לדף זה - אתה לא מחובר!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return;
        }

        if (!savedUser.getIsAdmin()) {
            Toast.makeText(activity, "אין לך גישה לדף זה", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    }

    //2) דף שמיועד אך ורק למשתמשים רגילים (לא מנהלים)
    public static void checkUserPage(Activity activity) {
        User savedUser = SharedPreferencesUtil.getUser(activity);

        if (savedUser == null) {
            Toast.makeText(activity, "אין לך גישה לדף זה - אתה לא מחובר!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return;
        }

        if (savedUser.getIsAdmin()) {
            Toast.makeText(activity, "ניסיון יפה, מנהל! אבל לצערנו, אין לך גישה", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, AdminPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    }

    //3) אם מישהו כבר מחובר - שלח אותו לדף המתאים (מונע גישה לדפי Login/Register)
    public static void redirectIfLoggedIn(Activity activity) {
        User savedUser = SharedPreferencesUtil.getUser(activity);

        if (savedUser != null) {
            Intent intent;

            if (savedUser.getIsAdmin()) {
                intent = new Intent(activity, AdminPageActivity.class);
            } else {
                intent = new Intent(activity, MainActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    }
}
