package com.example.sagivproject.services.impl;

import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IUserService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.function.Consumer;

import javax.inject.Inject;

/**
 * Implementation of the {@link IAuthService} interface.
 * <p>
 * This class provides the logic for user authentication, including login, registration,
 * administrative user creation, and profile updates. It coordinates between the
 * {@link IUserService} for database persistence and {@link SharedPreferencesUtil} for
 * local session management.
 * </p>
 */
public class AuthServiceImpl implements IAuthService {
    private static final String ERROR_INVALID_CREDENTIALS = "אימייל או סיסמה שגויים";
    private static final String ERROR_LOGIN = "שגיאה בהתחברות המשתמש";
    private static final String ERROR_EMAIL_TAKEN = "אימייל זה תפוס";
    private static final String ERROR_CHECKING_EMAIL = "שגיאה בבדיקת אימייל";
    private static final String ERROR_UPDATING_DETAILS = "שגיאה בעדכון הפרטים";

    private final IUserService userService;
    private final SharedPreferencesUtil sharedPreferencesUtil;

    /**
     * Constructs a new AuthServiceImpl.
     *
     * @param userService           The user management service for database access.
     * @param sharedPreferencesUtil The utility for managing local session and preferences.
     */
    @Inject
    public AuthServiceImpl(IUserService userService, SharedPreferencesUtil sharedPreferencesUtil) {
        this.userService = userService;
        this.sharedPreferencesUtil = sharedPreferencesUtil;
    }

    /**
     * Authenticates a user using email and password.
     * On success, the user object is saved to local preferences.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback The callback invoked with the result.
     */
    @Override
    public void login(String email, String password, LoginCallback callback) {
        userService.getUserByEmailAndPassword(email, password, new DatabaseCallback<>() {
            @Override
            public void onCompleted(User user) {
                if (user == null) {
                    callback.onError(ERROR_INVALID_CREDENTIALS);
                    return;
                }

                sharedPreferencesUtil.saveUser(user);
                callback.onSuccess(user);
            }

            @Override
            public void onFailed(Exception e) {
                sharedPreferencesUtil.signOutUser();
                callback.onError(ERROR_LOGIN);
            }
        });
    }

    /**
     * Registers a new regular user account after checking for email uniqueness.
     *
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param birthDate The user's birthdate (ISO format).
     * @param email     The user's email address.
     * @param password  The user's password.
     * @param callback  The callback invoked when the process completes.
     */
    @Override
    public void register(String firstName, String lastName, String birthDate, String email, String password, RegisterCallback callback) {
        handleUserCreation(firstName, lastName, birthDate, email, password, new DatabaseCallback<>() {
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
     * Adds a new regular user account (typically by an admin) without logging in as the new user.
     *
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param birthDate The user's birthdate (ISO format).
     * @param email     The user's email address.
     * @param password  The user's password.
     * @param callback  The callback invoked when the process completes.
     */
    @Override
    public void addUser(String firstName, String lastName, String birthDate, String email, String password, AddUserCallback callback) {
        handleUserCreation(firstName, lastName, birthDate, email, password, new DatabaseCallback<>() {
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
     * Updates an existing user's details, handling email uniqueness checks if the email was changed.
     *
     * @param user         The current user object.
     * @param newFirstName The updated first name.
     * @param newLastName  The updated last name.
     * @param newBirthDate The updated birthdate (ISO format).
     * @param newEmail     The updated email address.
     * @param newPassword  The updated password.
     * @param callback     The callback invoked when the update completes.
     */
    @Override
    public void updateUser(User user, String newFirstName, String newLastName, String newBirthDate, String newEmail, String newPassword, UpdateUserCallback callback) {
        boolean emailChanged = !newEmail.equals(user.getEmail());

        if (emailChanged) {
            userService.checkIfEmailExists(newEmail, new DatabaseCallback<>() {
                @Override
                public void onCompleted(Boolean exists) {
                    if (exists) {
                        callback.onError(ERROR_EMAIL_TAKEN);
                    } else {
                        applyUserUpdate(user, newFirstName, newLastName, newBirthDate, newEmail, newPassword, callback);
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    callback.onError(ERROR_CHECKING_EMAIL);
                }
            });
        } else {
            applyUserUpdate(user, newFirstName, newLastName, newBirthDate, newEmail, newPassword, callback);
        }
    }

    /**
     * Signs out the current user and clears local profile data.
     *
     * @return The email of the logged-out user.
     */
    @Override
    public String logout() {
        User user = sharedPreferencesUtil.getUser();
        String email = user != null ? user.getEmail() : "";
        sharedPreferencesUtil.signOutUser();
        return email;
    }

    /**
     * Internal helper to generate a UID and create a new user record in the database.
     */
    private void createUser(String firstName, String lastName, String birthDate, String email, String password, DatabaseCallback<User> callback) {
        String uid = userService.generateUserId();
        User user = new User(uid, firstName, lastName, birthDate, email, password, User.UserRole.REGULAR);

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
     * Internal helper to commit profile updates to the database.
     */
    private void applyUserUpdate(User user, String firstName, String lastName, String birthDate, String email, String password, UpdateUserCallback callback) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBirthDate(birthDate);
        user.setEmail(email);
        user.setPassword(password);

        userService.updateUser(user, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void object) {
                callback.onSuccess(user);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onError(ERROR_UPDATING_DETAILS);
            }
        });
    }

    /**
     * Internal helper to validate email availability before proceeding with account creation.
     */
    private void handleUserCreation(String firstName, String lastName, String birthDate, String email, String password, DatabaseCallback<User> successCallback, Consumer<String> errorCallback) {
        userService.checkIfEmailExists(email, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    errorCallback.accept(ERROR_EMAIL_TAKEN);
                } else {
                    createUser(firstName, lastName, birthDate, email, password, successCallback);
                }
            }

            @Override
            public void onFailed(Exception e) {
                errorCallback.accept(ERROR_CHECKING_EMAIL);
            }
        });
    }
}