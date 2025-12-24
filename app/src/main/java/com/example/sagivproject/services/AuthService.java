package com.example.sagivproject.services;

import android.content.Context;

import com.example.sagivproject.models.User;
import com.example.sagivproject.utils.SharedPreferencesUtil;

public class AuthService {
    private final Context context;

    public AuthService(Context context) {
        this.context = context.getApplicationContext();
    }

    //התנתקות
    public String logout() {
        User user = SharedPreferencesUtil.getUser(context);

        String email = user != null ? user.getEmail() : "";
        SharedPreferencesUtil.signOutUser(context);

        return email;
    }
}