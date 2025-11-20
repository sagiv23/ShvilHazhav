package com.example.sagivproject.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.AdminPageActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.MainActivity;
import com.example.sagivproject.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PagePermissions {

    // בדיקה משותפת – תקינות Firebase + SharedPreferences
    private static String validateLoggedIn(Activity activity) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User spUser = SharedPreferencesUtil.getUser(activity);

        // אם אין שום דבר – לא מחובר
        if (firebaseUser == null && spUser == null) {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return null;
        }

        // אם יש ב-SP אך אין ב-Firebase → משהו לא תקין → ניגש ל-Login
        if (firebaseUser == null && spUser != null) {
            SharedPreferencesUtil.signOutUser(activity);
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return null;
        }

        // אם יש ב-Firebase אך אין ב-SP → חובה Logout
        if (firebaseUser != null && spUser == null) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return null;
        }

        // אם יש סתירה בין ה-UIDs
        if (!firebaseUser.getUid().equals(spUser.getUid())) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferencesUtil.signOutUser(activity);
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return null;
        }

        return firebaseUser.getUid();
    }

    // ---------------------- 1) כניסה רק למנהלים ----------------------
    public static void checkAdminPage(Activity activity) {
        /*
        String uid = validateLoggedIn(activity);
        if (uid == null) return;

        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user == null || !Boolean.TRUE.equals(user.getIsAdmin())) {
                    Toast.makeText(activity, "אין לך גישה לדף זה", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            }
        });

         */
    }

    // -------------------- 2) דף שמיועד למשתמשים רגילים -------------------
    public static void checkUserPage(Activity activity) {
        /*
        String uid = validateLoggedIn(activity);
        if (uid == null) return;

        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User dbUser) {
                if (dbUser == null) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    return;
                }

                if (Boolean.TRUE.equals(dbUser.getIsAdmin())) {
                    Toast.makeText(activity, "הדף מיועד למשתמשים רגילים בלבד", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, AdminPageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            }
        });
         */
    }

    // --------- 3) אם כבר מחובר – שלח אותו לדף המתאים במקום Login/Register ---------
    public static void redirectIfLoggedIn(Activity activity) {
        /*
        String uid = validateLoggedIn(activity);
        if (uid == null) return;

        DatabaseService.getInstance().getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User dbUser) {
                if (dbUser == null) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    return;
                }

                Intent intent;
                if (Boolean.TRUE.equals(dbUser.getIsAdmin())) {
                    intent = new Intent(activity, AdminPageActivity.class);
                } else {
                    intent = new Intent(activity, MainActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            }

            @Override
            public void onFailed(Exception e) {
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            }
        });

         */
    }
}
