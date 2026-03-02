package com.example.sagivproject.utils;

import android.util.Patterns;

import androidx.annotation.Nullable;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A utility class for input validation.
 * <p>
 * This class provides methods to validate common user inputs such as names,
 * email addresses, passwords, and age. It is managed as a Singleton by Hilt.
 * </p>
 */
@Singleton
public class Validator {
    private static final int MIN_AGE = 12;

    @Inject
    public Validator() {
    }

    /**
     * Validates a name.
     *
     * @param name The name to validate.
     * @return True if the name is null or has fewer than 2 characters, false otherwise.
     */
    public boolean isNameNotValid(@Nullable String name) {
        return name == null || name.trim().length() < 2;
    }

    /**
     * Validates an email address using the standard Android Patterns.EMAIL_ADDRESS pattern.
     *
     * @param email The email to validate.
     * @return True if the email is null or does not match the standard email pattern, false otherwise.
     */
    public boolean isEmailNotValid(@Nullable String email) {
        return email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates a password.
     *
     * @param password The password to validate.
     * @return True if the password is null or has fewer than 6 characters, false otherwise.
     */
    public boolean isPasswordNotValid(@Nullable String password) {
        return password == null || password.length() < 6;
    }

    /**
     * Validates the user's age based on their birthdate.
     *
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @return True if the calculated age is less than the minimum required age (12), false otherwise.
     */
    public boolean isAgeNotValid(long birthDateMillis) {
        Calendar birth = Calendar.getInstance();
        birth.setTimeInMillis(birthDateMillis);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age < MIN_AGE;
    }
}
