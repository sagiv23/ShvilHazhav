package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.UserRole;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for database operations related to the {@link User} model.
 * <p>
 * This service manages all CRUD (Create, Read, Update, Delete) operations for users,
 * as well as specific queries like authentication and email validation.
 * </p>
 */
public interface IUserService {
    /**
     * Generates a new, unique identifier for a user record.
     *
     * @return A unique user ID string.
     */
    String generateUserId();

    /**
     * Creates a new user record in the database.
     *
     * @param user     The {@link User} object to create.
     * @param callback An optional callback to be invoked upon completion.
     */
    void createNewUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves a single user from the database by their unique ID.
     *
     * @param uid      The ID of the user to retrieve.
     * @param callback The callback invoked with the retrieved {@link User} object.
     */
    void getUser(@NonNull String uid, @NonNull DatabaseCallback<User> callback);

    /**
     * Retrieves a list of all registered users from the database.
     *
     * @param callback The callback invoked with the list of all users.
     */
    void getUserList(@NonNull DatabaseCallback<List<User>> callback);

    /**
     * Deletes a user account and its associated data from the database.
     *
     * @param uid      The ID of the user to remove.
     * @param callback An optional callback invoked upon completion.
     */
    void deleteUser(@NonNull String uid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves a user by matching their email and password credentials.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback The callback invoked with the matching user, or null if not found.
     */
    void getUserByEmailAndPassword(@NonNull String email, @NonNull String password, @NonNull DatabaseCallback<User> callback);

    /**
     * Checks if a specific email address is already registered in the system.
     *
     * @param email    The email address to validate.
     * @param callback The callback invoked with true if the email exists.
     */
    void checkIfEmailExists(@NonNull String email, @NonNull DatabaseCallback<Boolean> callback);

    /**
     * Updates an existing user's information in the database.
     *
     * @param user     The {@link User} object containing updated details.
     * @param callback An optional callback invoked upon completion.
     */
    void updateUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates the administrative role of a specific user.
     *
     * @param uid      The ID of the user to modify.
     * @param role     The new {@link UserRole} to assign.
     * @param callback An optional callback invoked upon completion.
     */
    void updateUserRole(@NonNull String uid, @NonNull UserRole role, @Nullable DatabaseCallback<Void> callback);
}