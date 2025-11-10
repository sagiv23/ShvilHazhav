package com.example.sagivproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sagivproject.models.User;
import com.google.gson.Gson;

public class SharedPreferencesUtil {

    private static final String PREF_NAME = "com.example.sagivproject.PREFERENCE_FILE_KEY";

    private static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void clear(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    private static boolean contains(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }

    private static <T> void saveObject(Context context, String key, T object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        saveString(context, key, json);
    }

    private static <T> T getObject(Context context, String key, Class<T> type) {
        String json = getString(context, key, null);
        if (json == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public static void saveUser(Context context, User user) { saveObject(context, "user", user); }

    public static User getUser(Context context) { return getObject(context, "user", User.class); }

    public static void signOutUser(Context context) { remove(context, "user"); }

    public static boolean isUserLoggedIn(Context context) { return contains(context, "user"); }
}
