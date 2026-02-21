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
     * Generates a new, unique ID for a new user.
     *
     * @return A unique ID string.
     */
    String generateUserId();

    /**
     * Creates a new user record in the database.
     *
     * @param user     The user object to create.
     * @param callback An optional callback to be invoked upon completion.
     */
    void createNewUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves a single user from the database by their ID.
     *
     * @param uid      The ID of the user to retrieve.
     * @param callback The callback to be invoked with the retrieved user.
     */
    void getUser(@NonNull String uid, @NonNull DatabaseCallback<User> callback);

    /**
     * Retrieves a list of all users from the database.
     *
     * @param callback The callback to be invoked with the list of all users.
     */
    void getUserList(@NonNull DatabaseCallback<List<User>> callback);

    /**
     * Deletes a user from the database.
     *
     * @param uid      The ID of the user to delete.
     * @param callback An optional callback to be invoked upon completion.
     */
    void deleteUser(@NonNull String uid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves a user by matching their email and password.
     *
     * @param email    The email of the user.
     * @param password The password of the user.
     * @param callback The callback to be invoked with the matching user, or null if not found.
     */
    void getUserByEmailAndPassword(@NonNull String email, @NonNull String password, @NonNull DatabaseCallback<User> callback);

    /**
     * Checks if a given email address already exists in the database.
     *
     * @param email    The email to check.
     * @param callback The callback to be invoked with true if the email exists, false otherwise.
     */
    void checkIfEmailExists(@NonNull String email, @NonNull DatabaseCallback<Boolean> callback);

    /**
     * Updates an existing user in the database with a new user object.
     *
     * @param user     The user object containing the updated information.
     * @param callback An optional callback to be invoked upon completion.
     */
    void updateUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates only the role of a user (e.g., promotes to admin).
     *
     * @param uid      The ID of the user to update.
     * @param role     The new role for the user.
     * @param callback An optional callback for the result.
     */
    void updateUserRole(@NonNull String uid, @NonNull UserRole role, @Nullable DatabaseCallback<Void> callback);
}
