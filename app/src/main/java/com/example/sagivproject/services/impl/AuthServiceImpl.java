package com.example.sagivproject.services.impl;

import com.example.sagivproject.models.User;
import com.example.sagivproject.models.enums.UserRole;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IUserService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.HashMap;

import javax.inject.Inject;

/**
 * An implementation of the {@link IAuthService} interface.
 * <p>
 * This service handles user authentication and management, including login, registration,
 * adding new users (by an admin), and updating user profiles. It coordinates with the
 * {@link IUserService} for database operations and {@link SharedPreferencesUtil} for session management.
 * </p>
 */
public class AuthServiceImpl implements IAuthService {
    private final IUserService userService;
    private final SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Constructs a new AuthServiceImpl.
     *
     * @param userService           The user service for database interactions.
     * @param sharedPreferencesUtil The utility for managing user sessions in SharedPreferences.
     */
    @Inject
    public AuthServiceImpl(IUserService userService, SharedPreferencesUtil sharedPreferencesUtil) {
        this.userService = userService;
        this.sharedPreferencesUtil = sharedPreferencesUtil;
    }

    /**
     * Logs in a user with the provided email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @param callback The callback to be invoked with the result.
     */
    @Override
    public void login(String email, String password, LoginCallback callback) {
        userService.getUserByEmailAndPassword(email, password, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user == null) {
                    callback.onError("אימייל או סיסמה שגויים");
                    return;
                }

                sharedPreferencesUtil.saveUser(user);
                callback.onSuccess(user);
            }

            @Override
            public void onFailed(Exception e) {
                sharedPreferencesUtil.signOutUser();
                callback.onError("שגיאה בהתחברות המשתמש");
            }
        });
    }

    /**
     * Registers a new user and logs them in.
     *
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param birthDateMillis The user's birthdate in milliseconds.
     * @param email           The user's email.
     * @param password        The user's password.
     * @param callback        The callback to be invoked with the result.
     */
    @Override
    public void register(String firstName, String lastName, long birthDateMillis, String email, String password, RegisterCallback callback) {
        handleUserCreation(firstName, lastName, birthDateMillis, email, password, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                sharedPreferencesUtil.saveUser(user);
                callback.onSuccess(user);
            }

            @Override
            public void onFailed(Exception e) {
                sharedPreferencesUtil.signOutUser();
                callback.onError(e.getMessage());
            }
        }, callback::onError);
    }

    /**
     * Adds a new user, typically by an administrator.
     *
     * @param firstName       The new user's first name.
     * @param lastName        The new user's last name.
     * @param birthDateMillis The new user's birthdate in milliseconds.
     * @param email           The new user's email.
     * @param password        The new user's password.
     * @param callback        The callback to be invoked with the result.
     */
    @Override
    public void addUser(String firstName, String lastName, long birthDateMillis, String email, String password, AddUserCallback callback) {
        handleUserCreation(firstName, lastName, birthDateMillis, email, password, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onError(e.getMessage());
            }
        }, callback::onError);
    }

    /**
     * A generic handler for creating a new user. It first checks if the email already exists.
     *
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param birthDateMillis The user's birthdate.
     * @param email           The user's email.
     * @param password        The user's password.
     * @param successCallback The callback to invoke on successful user creation.
     * @param errorCallback   The callback to invoke on failure.
     */
    private void handleUserCreation(String firstName, String lastName, long birthDateMillis, String email, String password, DatabaseCallback<User> successCallback, java.util.function.Consumer<String> errorCallback) {
        userService.checkIfEmailExists(email, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    errorCallback.accept("אימייל זה תפוס");
                } else {
                    createUser(firstName, lastName, birthDateMillis, email, password, successCallback);
                }
            }

            @Override
            public void onFailed(Exception e) {
                errorCallback.accept("שגיאה בבדיקת אימייל");
            }
        });
    }

    /**
     * Updates an existing user's profile information. Checks for email availability if it's changed.
     *
     * @param user               The original user object.
     * @param newFirstName       The new first name.
     * @param newLastName        The new last name.
     * @param newBirthDateMillis The new birthdate.
     * @param newEmail           The new email.
     * @param newPassword        The new password.
     * @param callback           The callback to be invoked with the result.
     */
    @Override
    public void updateUser(User user, String newFirstName, String newLastName, long newBirthDateMillis, String newEmail, String newPassword, UpdateUserCallback callback) {
        boolean emailChanged = !newEmail.equals(user.getEmail());

        if (emailChanged) {
            userService.checkIfEmailExists(newEmail, new DatabaseCallback<>() {
                @Override
                public void onCompleted(Boolean exists) {
                    if (exists) {
                        callback.onError("אימייל זה תפוס");
                    } else {
                        applyUserUpdate(user, newFirstName, newLastName, newBirthDateMillis, newEmail, newPassword, callback);
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    callback.onError("שגיאה בבדיקת אימייל");
                }
            });
        } else {
            applyUserUpdate(user, newFirstName, newLastName, newBirthDateMillis, newEmail, newPassword, callback);
        }
    }

    /**
     * Creates a new User object and saves it to the database.
     *
     * @param firstName       The user's first name.
     * @param lastName        The user's last name.
     * @param birthDateMillis The user's birthdate.
     * @param email           The user's email.
     * @param password        The user's password.
     * @param callback        The callback for the result of the database operation.
     */
    private void createUser(String firstName, String lastName, long birthDateMillis, String email, String password, DatabaseCallback<User> callback) {
        String uid = userService.generateUserId();

        User user = new User(uid, firstName, lastName, birthDateMillis, email, password, UserRole.REGULAR, null, new HashMap<>());

        userService.createNewUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                callback.onCompleted(user);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Applies the updated fields to a user object and saves it to the database.
     *
     * @param user      The user object to update.
     * @param firstName The new first name.
     * @param lastName  The new last name.
     * @param birthDate The new birthdate.
     * @param email     The new email.
     * @param password  The new password.
     * @param callback  The callback for the result.
     */
    private void applyUserUpdate(User user, String firstName, String lastName, long birthDate, String email, String password, UpdateUserCallback callback) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBirthDateMillis(birthDate);
        user.setEmail(email);
        user.setPassword(password);

        userService.updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                sharedPreferencesUtil.saveUser(user);
                callback.onSuccess(user);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onError("שגיאה בעדכון הפרטים");
            }
        });
    }

    /**
     * Logs out the current user by clearing their data from SharedPreferences.
     *
     * @return The email of the logged-out user, or an empty string if no user was logged in.
     */
    @Override
    public String logout() {
        User user = sharedPreferencesUtil.getUser();

        String email = user != null ? user.getEmail() : "";
        sharedPreferencesUtil.signOutUser();

        return email;
    }
}
