package com.example.sagivproject.utils;

import android.util.Patterns;

import androidx.annotation.Nullable;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A utility class for validating user input data.
 * <p>
 * This class provides standard methods to validate common user-entered values such as names,
 * email addresses, phone numbers, and age. It ensures consistency across the application
 * during registration and profile updates.
 * </p>
 */
@Singleton
public class Validator {
    /**
     * The minimum age required to register for the application.
     */
    private static final int MIN_AGE = 12;

    /**
     * Constructs a new Validator.
     */
    @Inject
    public Validator() {
    }

    /**
     * Validates a person's name (first or last).
     *
     * @param name The name string to validate.
     * @return true if the name is null, empty, or too short (less than 2 characters).
     */
    public boolean isNameNotValid(@Nullable String name) {
        return name == null || name.trim().length() < 2;
    }

    /**
     * Validates an email address format.
     *
     * @param email The email address to validate.
     * @return true if the email is null or does not match the standard {@link Patterns#EMAIL_ADDRESS} pattern.
     */
    public boolean isEmailNotValid(@Nullable String email) {
        return email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates a phone number format.
     *
     * @param phone The phone number to validate.
     * @return true if the phone number is null or does not match the standard {@link Patterns#PHONE} pattern.
     */
    public boolean isPhoneNotValid(@Nullable String phone) {
        return phone == null || !Patterns.PHONE.matcher(phone).matches();
    }

    /**
     * Validates a password's strength.
     *
     * @param password The password to validate.
     * @return true if the password is null or too short (less than 6 characters).
     */
    public boolean isPasswordNotValid(@Nullable String password) {
        return password == null || password.length() < 6;
    }

    /**
     * Checks if a user's age meets the minimum requirement based on their birthdate.
     *
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @return true if the calculated age is less than the required minimum age (12).
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