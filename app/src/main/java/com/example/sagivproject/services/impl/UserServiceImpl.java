package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.UserRole;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IUserService;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import javax.inject.Inject;

/**
 * An implementation of the {@link IUserService} interface.
 * <p>
 * This service manages all database operations related to the {@link User} model.
 * It handles creating, retrieving, updating, and deleting users. It also provides
 * methods for specific queries like finding a user by email and password or checking
 * if an email exists.
 * </p>
 */
public class UserServiceImpl extends BaseDatabaseService<User> implements IUserService {

    /**
     * Constructs a new UserServiceImpl.
     */
    @Inject
    public UserServiceImpl() {
        super("users", User.class);
    }

    /**
     * Generates a new unique ID for a user.
     *
     * @return A new unique ID string.
     */
    @Override
    public String generateUserId() {
        return super.generateId();
    }

    /**
     * Creates a new user in the database.
     *
     * @param user     The user to create.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void createNewUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback) {
        super.create(user, callback);
    }

    /**
     * Retrieves a single user by their ID.
     *
     * @param uid      The ID of the user to retrieve.
     * @param callback The callback to be invoked with the user data.
     */
    @Override
    public void getUser(@NonNull String uid, @NonNull DatabaseCallback<User> callback) {
        super.get(uid, callback);
    }

    /**
     * Retrieves a list of all users.
     *
     * @param callback The callback to be invoked with the list of users.
     */
    @Override
    public void getUserList(@NonNull DatabaseCallback<List<User>> callback) {
        super.getAll(callback);
    }

    /**
     * Deletes a user from the database by their ID.
     *
     * @param uid      The ID of the user to delete.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void deleteUser(@NonNull String uid, @Nullable DatabaseCallback<Void> callback) {
        super.delete(uid, callback);
    }

    /**
     * Finds a user by matching their email and password.
     *
     * @param email    The email to search for.
     * @param password The password to match.
     * @param callback The callback to be invoked with the found user, or null if not found.
     */
    @Override
    public void getUserByEmailAndPassword(@NonNull String email, @NonNull String password, @NonNull DatabaseCallback<User> callback) {
        getAll(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getEmail(), email) && Objects.equals(user.getPassword(), password)) {
                        callback.onCompleted(user);
                        return;
                    }
                }
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Checks if a given email address already exists in the database.
     *
     * @param email    The email to check.
     * @param callback The callback to be invoked with true if the email exists, false otherwise.
     */
    @Override
    public void checkIfEmailExists(@NonNull String email, @NonNull DatabaseCallback<Boolean> callback) {
        getAll(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getEmail(), email)) {
                        callback.onCompleted(true);
                        return;
                    }
                }
                callback.onCompleted(false);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Updates an entire user object in the database.
     *
     * @param user     The user object with updated information.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void updateUser(@NonNull User user, @Nullable DatabaseCallback<Void> callback) {
        UnaryOperator<User> updateFunction = oldUser -> user;
        super.update(user.getId(), updateFunction, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User updatedUser) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    /**
     * Updates a user's role (e.g., from REGULAR to ADMIN).
     *
     * @param uid      The ID of the user to update.
     * @param role     The new role for the user.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void updateUserRole(@NonNull String uid, @NonNull UserRole role, @Nullable DatabaseCallback<Void> callback) {
        UnaryOperator<User> updateFunction = user -> {
            user.setRole(role);
            return user;
        };
        super.update(uid, updateFunction, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }
}
