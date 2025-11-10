package com.example.sagivproject.models;

import android.app.Activity;
import android.content.Intent;

import com.example.sagivproject.screens.AdminPageActivity;
import com.example.sagivproject.screens.HomePageActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthHelper {
    //מחזיר false אם המשתמש לא מחובר ושולח לדף התחברות
    public static boolean checkUserLoggedIn(Activity activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User savedUser = SharedPreferencesUtil.getUser(activity);

        if (savedUser == null || firebaseUser == null) {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return false;
        }

        return true;
    }

    //מחזיר true אם המשתמש לא מחובר, אם מחובר נשלחים לדף הבית ומוחזר false
    public static boolean checkUserLoggedInFromspecialActivities(Activity activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User savedUser = SharedPreferencesUtil.getUser(activity);

        if (savedUser != null || firebaseUser != null) {
            Intent intent = new Intent(activity, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return false;
        }

        return true;
    }

    //מחזיר false אם לא מנהל ושולח לדף התחברות (אם אין משתמש בכלל) ולדף הבית אם יש משתמש
    public static boolean checkUserIsAdmin(Activity activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User savedUser = SharedPreferencesUtil.getUser(activity);

        if (savedUser == null || firebaseUser == null) {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return false;
        }

        //אם המשתמש לא אדמין - נשלח לדף הבית
        if (savedUser != null && !savedUser.getIsAdmin()) {
            Intent intent = new Intent(activity, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return false;
        }

        //אם המשתמש אדמין - נשאר באותו דף
        return true;
    }
}
