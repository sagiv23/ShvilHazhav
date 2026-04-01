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
 * A singleton utility class for managing persistent application preferences via {@link SharedPreferences}.
 * <p>
 * This class provides a centralized and type-safe API for interacting with local storage.
 * It supports basic primitive types and uses the {@link Gson} library to serialize and
 * deserialize complex objects, such as the {@link User} profile, into JSON strings.
 * </p>
 */
@Singleton
public class SharedPreferencesUtil {
    private static final String PREF_NAME = "com.example.sagivproject.PREFERENCE_FILE_KEY";
    private static final String KEY_USER = "user";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_FALL_DETECTION_ENABLED = "fall_detection_enabled";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    /**
     * Constructs a new SharedPreferencesUtil.
     * @param context The application context used to access shared preferences.
     * @param gson The Gson instance used for object serialization.
     */
    @Inject
    public SharedPreferencesUtil(@ApplicationContext Context context, Gson gson) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = gson;
    }

    /**
     * Saves a string value to preferences.
     * @param key The unique key for the preference.
     * @param value The string value to store.
     */
    public void saveString(String key, String value) { sharedPreferences.edit().putString(key, value).apply(); }

    /**
     * Retrieves a string value from preferences.
     * @param key The unique key for the preference.
     * @param defaultValue The value to return if the key does not exist.
     * @return The stored string or the default value.
     */
    public String getString(String key, String defaultValue) { return sharedPreferences.getString(key, defaultValue); }

    /**
     * Saves an integer value to preferences.
     * @param key The unique key for the preference.
     * @param value The integer value to store.
     */
    public void saveInt(String key, int value) { sharedPreferences.edit().putInt(key, value).apply(); }

    /**
     * Retrieves an integer value from preferences.
     * @param key The unique key for the preference.
     * @param defaultValue The value to return if the key does not exist.
     * @return The stored integer or the default value.
     */
    public int getInt(String key, int defaultValue) { return sharedPreferences.getInt(key, defaultValue); }

    /**
     * Saves a boolean value to preferences.
     * @param key The unique key for the preference.
     * @param value The boolean value to store.
     */
    public void saveBoolean(String key, boolean value) { sharedPreferences.edit().putBoolean(key, value).apply(); }

    /**
     * Retrieves a boolean value from preferences.
     * @param key The unique key for the preference.
     * @param defaultValue The value to return if the key does not exist.
     * @return The stored boolean or the default value.
     */
    public boolean getBoolean(String key, boolean defaultValue) { return sharedPreferences.getBoolean(key, defaultValue); }

    /**
     * Removes all data stored in this preferences file.
     */
    public void clear() { sharedPreferences.edit().clear().apply(); }

    /**
     * Removes a specific preference entry.
     * @param key The key of the entry to remove.
     */
    public void remove(String key) { sharedPreferences.edit().remove(key).apply(); }

    /**
     * Checks if a specific key exists in the preferences.
     * @param key The key to check.
     * @return true if the key is present.
     */
    public boolean contains(String key) { return sharedPreferences.contains(key); }

    /**
     * Serializes an object to JSON and saves it to preferences.
     * @param key The unique key for the preference.
     * @param object The object instance to store.
     * @param <T> The type of the object.
     */
    public <T> void saveObject(String key, T object) {
        String json = gson.toJson(object);
        saveString(key, json);
    }

    /**
     * Retrieves a JSON string from preferences and deserializes it into an object.
     * @param key The unique key for the preference.
     * @param type The class type of the object to return.
     * @param <T> The type of the object.
     * @return The deserialized object, or null if the key is missing.
     */
    public <T> T getObject(String key, Class<T> type) {
        String json = getString(key, null);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, type);
    }

    /**
     * Saves the current {@link User} object to persistent storage.
     * @param user The user profile to save.
     */
    public void saveUser(User user) { saveObject(KEY_USER, user); }

    /**
     * Retrieves the stored {@link User} profile.
     * @return The User object, or null if no user is authenticated.
     */
    public User getUser() { return getObject(KEY_USER, User.class); }

    /**
     * Logs out the user by removing their profile data from preferences.
     */
    public void signOutUser() { remove(KEY_USER); }

    /**
     * Checks if there is no authenticated user session.
     * @return true if no user data is stored.
     */
    public boolean isUserNotLoggedIn() { return !contains(KEY_USER); }

    /**
     * Checks the user's preferred theme setting.
     * @return true if Dark Mode is enabled.
     */
    public boolean isDarkMode() { return getBoolean(KEY_DARK_MODE, false); }

    /**
     * Sets the user's preferred theme setting.
     * @param isDarkMode true to enable Dark Mode.
     */
    public void setDarkMode(boolean isDarkMode) { saveBoolean(KEY_DARK_MODE, isDarkMode); }

    /**
     * Safely retrieves the unique identifier of the logged-in user.
     * @return The user ID string, or null if not logged in.
     */
    @Nullable
    public String getUserId() {
        User user = getUser();
        if (user != null) {
            return user.getId();
        }
        return null;
    }

    /**
     * Checks if the fall detection background service is enabled.
     * @return true if fall detection is active.
     */
    public boolean isFallDetectionEnabled() { return getBoolean(KEY_FALL_DETECTION_ENABLED, false); }

    /**
     * Updates the fall detection service state in preferences.
     * @param isEnabled true to enable monitoring.
     */
    public void setFallDetectionEnabled(boolean isEnabled) { saveBoolean(KEY_FALL_DETECTION_ENABLED, isEnabled); }
}