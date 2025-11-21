package com.example.sagivproject.utils;

import android.util.Patterns;
import androidx.annotation.Nullable;

public class Validator {
    public static boolean isNameValid(@Nullable String name) {
        return name != null && name.length() >= 3;
    }

    public static boolean isEmailValid(@Nullable String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(@Nullable String password) {
        return password != null && password.length() >= 6;
    }
}