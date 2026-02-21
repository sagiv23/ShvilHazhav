package com.example.sagivproject.services;

import com.example.sagivproject.models.User;

/**
 * An interface that defines the contract for authentication-related operations.
 * <p>
 * This service handles user login, registration, adding new users (by admins),
 * updating user profiles, and logging out.
 * </p>
 */
public interface IAuthService {
    /**
     * Logs in a user with the given email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @param callback The callback to be invoked when the login process is complete.
     */
    void login(String email, String password, LoginCallback callback);

    /**
     * Registers a new user with the given details.
     *
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @param email           The user's email.
     * @param password        The user's password.
     * @param callback        The callback to be invoked when the registration process is complete.
     */
    void register(String firstName, String lastName, long birthDateMillis, String email, String password, RegisterCallback callback);

    /**
     * Adds a new user with the given details (typically an admin action).
     *
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @param email           The user's email.
     * @param password        The user's password.
     * @param callback        The callback to be invoked when the user is added.
     */
    void addUser(String firstName, String lastName, long birthDateMillis, String email, String password, AddUserCallback callback);

    /**
     * Updates an existing user's details.
     *
     * @param user               The user object to update.
     * @param newFirstName       The new first name.
     * @param newLastName        The new last name.
     * @param newBirthDateMillis The new birthdate in milliseconds.
     * @param newEmail           The new email.
     * @param newPassword        The new password.
     * @param callback           The callback to be invoked when the update is complete.
     */
    void updateUser(User user, String newFirstName, String newLastName, long newBirthDateMillis, String newEmail, String newPassword, UpdateUserCallback callback);

    /**
     * Logs out the current user.
     *
     * @return The email of the logged-out user, which can be used to pre-fill the login form.
     */
    String logout();

    /**
     * A callback interface for the login process.
     */
    interface LoginCallback {
        /**
         * Called when the login is successful.
         *
         * @param user The authenticated user object.
         */
        void onSuccess(User user);

        /**
         * Called when the login fails.
         *
         * @param message A descriptive error message.
         */
        void onError(String message);
    }

    /**
     * A callback interface for the registration process.
     */
    interface RegisterCallback {
        /**
         * Called when the registration is successful.
         *
         * @param user The newly created user object.
         */
        void onSuccess(User user);

        /**
         * Called when the registration fails.
         *
         * @param message A descriptive error message.
         */
        void onError(String message);
    }

    /**
     * A callback interface for the process of adding a new user.
     */
    interface AddUserCallback {
        /**
         * Called when the user is added successfully.
         *
         * @param user The newly created user object.
         */
        void onSuccess(User user);

        /**
         * Called when adding the user fails.
         *
         * @param message A descriptive error message.
         */
        void onError(String message);
    }

    /**
     * A callback interface for the process of updating a user.
     */
    interface UpdateUserCallback {
        /**
         * Called when the user is updated successfully.
         *
         * @param updatedUser The user object with the updated information.
         */
        void onSuccess(User updatedUser);

        /**
         * Called when updating the user fails.
         *
         * @param message A descriptive error message.
         */
        void onError(String message);
    }
}
