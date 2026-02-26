package com.example.sagivproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.sagivproject.models.User;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A singleton utility class for managing SharedPreferences.
 * <p>
 * This class provides a simplified and centralized way to interact with SharedPreferences.
 * It includes methods for saving and retrieving primitive data types as well as complex objects
 * (like the {@link User} model) by using the Gson library for JSON serialization.
 * </p>
 */
@Singleton
public class SharedPreferencesUtil {
    private static final String PREF_NAME = "com.example.sagivproject.PREFERENCE_FILE_KEY";
    private static final String KEY_USER = "user";
    private static final String KEY_DARK_MODE = "dark_mode";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    /**
     * Constructs a new SharedPreferencesUtil.
     *
     * @param context The application context.
     */
    @Inject
    public SharedPreferencesUtil(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Saves a string to SharedPreferences.
     *
     * @param key   The key for the value.
     * @param value The string value to save.
     */
    public void saveString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * Retrieves a string from SharedPreferences.
     *
     * @param key          The key of the value to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved string or the default value.
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Saves an integer to SharedPreferences.
     *
     * @param key   The key for the value.
     * @param value The integer value to save.
     */
    public void saveInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    /**
     * Retrieves an integer from SharedPreferences.
     *
     * @param key          The key of the value to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved integer or the default value.
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Saves a boolean to SharedPreferences.
     *
     * @param key   The key for the value.
     * @param value The boolean value to save.
     */
    public void saveBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Retrieves a boolean from SharedPreferences.
     *
     * @param key          The key of the value to retrieve.
     * @param defaultValue The default value to return if the key is not found.
     * @return The retrieved boolean or the default value.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Clears all data from SharedPreferences.
     */
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Removes a specific key-value pair from SharedPreferences.
     *
     * @param key The key to remove.
     */
    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    /**
     * Checks if a key exists in SharedPreferences.
     *
     * @param key The key to check.
     * @return True if the key exists, false otherwise.
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * Saves a complex object to SharedPreferences by converting it to a JSON string.
     *
     * @param key    The key for the object.
     * @param object The object to save.
     * @param <T>    The type of the object.
     */
    public <T> void saveObject(String key, T object) {
        String json = gson.toJson(object);
        saveString(key, json);
    }

    /**
     * Retrieves a complex object from SharedPreferences by converting it from a JSON string.
     *
     * @param key  The key of the object to retrieve.
     * @param type The class of the object.
     * @param <T>  The type of the object.
     * @return The retrieved object, or null if not found.
     */
    public <T> T getObject(String key, Class<T> type) {
        String json = getString(key, null);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, type);
    }

    /**
     * Saves the current user object to SharedPreferences.
     *
     * @param user The user object to save.
     */
    public void saveUser(User user) {
        saveObject(KEY_USER, user);
    }

    /**
     * Retrieves the current user object from SharedPreferences.
     *
     * @return The user object, or null if no user is logged in.
     */
    public User getUser() {
        return getObject(KEY_USER, User.class);
    }

    /**
     * Signs out the current user by removing their data from SharedPreferences.
     */
    public void signOutUser() {
        remove(KEY_USER);
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return True if user data exists in SharedPreferences, false otherwise.
     */
    public boolean isUserLoggedIn() {
        return !contains(KEY_USER);
    }

    /**
     * Checks if dark mode is enabled in preferences.
     *
     * @return True if dark mode is enabled, false otherwise.
     */
    public boolean isDarkMode() {
        return getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Sets the dark mode preference.
     *
     * @param isDarkMode True to enable dark mode, false to disable.
     */
    public void setDarkMode(boolean isDarkMode) {
        saveBoolean(KEY_DARK_MODE, isDarkMode);
    }

    /**
     * Retrieves the ID of the currently logged-in user.
     *
     * @return The user ID, or null if no user is logged in.
     */
    @Nullable
    public String getUserId() {
        User user = getUser();
        if (user != null) {
            return user.getId();
        }
        return null;
    }
}
