package com.example.sagivproject.services;

import com.example.sagivproject.models.User;

/**
 * An interface that defines the contract for authentication and account management operations.
 * <p>
 * This service provides methods for user login, registration, administrative user creation,
 * profile updates, and session management.
 * </p>
 */
public interface IAuthService {
    /**
     * Attempts to log in a user with the provided credentials.
     * @param email The user's email address.
     * @param password The user's password.
     * @param callback The callback invoked when the login attempt completes.
     */
    void login(String email, String password, LoginCallback callback);

    /**
     * Registers a new regular user account.
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @param email The user's email address.
     * @param password The user's password.
     * @param callback The callback invoked when the registration completes.
     */
    void register(String firstName, String lastName, long birthDateMillis, String email, String password, RegisterCallback callback);

    /**
     * Adds a new regular user account (typically an administrative action).
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @param email The user's email address.
     * @param password The user's password.
     * @param callback The callback invoked when the account creation completes.
     */
    void addUser(String firstName, String lastName, long birthDateMillis, String email, String password, AddUserCallback callback);

    /**
     * Updates an existing user's profile information.
     * @param user The user object representing the current state.
     * @param newFirstName The updated first name.
     * @param newLastName The updated last name.
     * @param newBirthDateMillis The updated birthdate in milliseconds.
     * @param newEmail The updated email address.
     * @param newPassword The updated password.
     * @param callback The callback invoked when the update completes.
     */
    void updateUser(User user, String newFirstName, String newLastName, long newBirthDateMillis, String newEmail, String newPassword, UpdateUserCallback callback);

    /**
     * Logs out the current user and clears local session data.
     * @return The email address of the user who was logged out, useful for pre-filling login forms.
     */
    String logout();

    /** Callback interface for user login results. */
    interface LoginCallback {
        /**
         * Invoked when authentication is successful.
         * @param user The authenticated {@link User} object.
         */
        void onSuccess(User user);

        /**
         * Invoked when authentication fails.
         * @param message A descriptive error message.
         */
        void onError(String message);
    }

    /** Callback interface for user registration results. */
    interface RegisterCallback {
        /**
         * Invoked when the account is successfully created and the user is logged in.
         * @param user The newly created {@link User} object.
         */
        void onSuccess(User user);

        /**
         * Invoked when registration fails.
         * @param message A descriptive error message.
         */
        void onError(String message);
    }

    /** Callback interface for administrative user creation results. */
    interface AddUserCallback {
        /**
         * Invoked when the user account is successfully added to the database.
         * @param user The newly created {@link User} object.
         */
        void onSuccess(User user);

        /**
         * Invoked when the operation fails.
         * @param message A descriptive error message.
         */
        void onError(String message);
    }

    /** Callback interface for user profile update results. */
    interface UpdateUserCallback {
        /**
         * Invoked when the profile information is successfully updated.
         * @param updatedUser The updated {@link User} object.
         */
        void onSuccess(User updatedUser);

        /**
         * Invoked when the update operation fails.
         * @param message A descriptive error message.
         */
        void onError(String message);
    }
}