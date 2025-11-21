package com.example.sagivproject.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.AdminPageActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.MainActivity;

public class PagePermissions {
    //1) כניסה רק למנהלים
    public static void checkAdminPage(Activity activity) {
        User user = SharedPreferencesUtil.getUser(activity);

        if (!SharedPreferencesUtil.isUserLoggedIn(activity)) {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        } else if (!user.getIsAdmin()) {
            Toast.makeText(activity, "אין לך גישה לדף זה", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    }

    //2) דף שמיועד למשתמשים רגילים
    public static void checkUserPage(Activity activity) {
        User user = SharedPreferencesUtil.getUser(activity);

        if (!SharedPreferencesUtil.isUserLoggedIn(activity)) {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        } else if (user.getIsAdmin()) {
            Toast.makeText(activity, "הדף מיועד למשתמשים רגילים בלבד", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, AdminPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    }

    //3) אם כבר מחובר – שלח אותו לדף המתאים במקום Login/Register
    public static void redirectIfLoggedIn(Activity activity) {
        User user = SharedPreferencesUtil.getUser(activity);

        if (SharedPreferencesUtil.isUserLoggedIn(activity)) {
            Intent intent;
            if (user.getIsAdmin()) {
                intent = new Intent(activity, AdminPageActivity.class);
            } else {
                intent = new Intent(activity, MainActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }
    }
}