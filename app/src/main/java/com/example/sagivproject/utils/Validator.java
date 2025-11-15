package com.example.sagivproject.utils;

import android.util.Patterns;
import androidx.annotation.Nullable;

/// Validator class to validate user input.
/// This class contains static methods to validate user input,
/// like email, password, phone, name etc.
public class Validator {
    /// Check if the name is valid
    public static boolean isNameValid(@Nullable String name) {
        return name != null && name.length() >= 3;
    }

    /// Check if the email is valid
    /// @see Patterns#EMAIL_ADDRESS
    public static boolean isEmailValid(@Nullable String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /// Check if the password is valid
    public static boolean isPasswordValid(@Nullable String password) {
        return password != null && password.length() >= 6;
    }
}