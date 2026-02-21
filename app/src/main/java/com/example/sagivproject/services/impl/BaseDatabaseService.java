package com.example.sagivproject.services.impl;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.Idable;
import com.example.sagivproject.services.IDatabaseService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * An abstract base class for Firebase Realtime Database services.
 * <p>
 * This class provides a generic implementation for common CRUD (Create, Read, Update, Delete)
 * operations for any data model that implements the {@link Idable} interface.
 * It simplifies interaction with the Firebase database by handling data serialization,
 * callbacks, and error logging.
 * </p>
 *
 * @param <T> The type of the data model, which must extend {@link Idable}.
 */
public abstract class BaseDatabaseService<T extends Idable> {
    /**
     * Tag for logging.
     */
    private static final String TAG = "BaseFirebaseService";

    /**
     * The reference to the Firebase database.
     */
    protected final DatabaseReference databaseReference;

    /**
     * The path in the database for this entity type.
     */
    private final String path;

    /**
     * The class of the entity type (needed for Firebase deserialization).
     */
    private final Class<T> clazz;

    /**
     * Constructs a new BaseDatabaseService.
     *
     * @param path  The path to the data in the Firebase Realtime Database.
     * @param clazz The class of the data model.
     */
    protected BaseDatabaseService(@NotNull final String path, @NotNull final Class<T> clazz) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        this.path = path;
        this.clazz = clazz;
    }

    /**
     * Generates a new unique ID for a new entity in the database.
     *
     * @return A new unique ID string.
     */
    protected String generateId() {
        return databaseReference.child(path).push().getKey();
    }

    /**
     * Creates or overwrites an entity in the database.
     *
     * @param item     The entity to create or overwrite.
     * @param callback The callback to be invoked upon completion.
     */
    protected void create(@NotNull final T item, @Nullable final IDatabaseService.DatabaseCallback<Void> callback) {
        writeData(path + "/" + item.getId(), item, callback);
    }

    /**
     * Retrieves a single entity from the database by its ID.
     *
     * @param id       The ID of the entity to retrieve.
     * @param callback The callback to be invoked with the result.
     */
    protected void get(@NotNull final String id, final IDatabaseService.DatabaseCallback<T> callback) {
        getData(path + "/" + id, callback);
    }

    /**
     * Retrieves all entities of this type from the database.
     *
     * @param callback The callback to be invoked with the list of results.
     */
    protected void getAll(final IDatabaseService.DatabaseCallback<List<T>> callback) {
        getDataList(path, callback);
    }

    /**
     * Deletes an entity from the database by its ID.
     *
     * @param id       The ID of the entity to delete.<br>
     * @param callback The callback to be invoked upon completion.
     */
    protected void delete(@NotNull final String id, @Nullable final IDatabaseService.DatabaseCallback<Void> callback) {
        deleteData(path + "/" + id, callback);
    }

    /**
     * Updates an entity using a transaction to ensure atomicity.
     *
     * @param id       The ID of the entity to update.
     * @param function The function to apply to the current value of the entity.
     * @param callback The callback to be invoked with the updated entity.
     */
    protected void update(@NotNull final String id, final @NotNull UnaryOperator<T> function, @Nullable final IDatabaseService.DatabaseCallback<T> callback) {
        runTransaction(path + "/" + id, function, callback);
    }

    // region low-level helpers

    /**
     * Gets a DatabaseReference for a specific path.
     *
     * @param fullPath The full path in the database.
     * @return A {@link DatabaseReference} for the given path.
     */
    protected DatabaseReference readData(@NotNull final String fullPath) {
        return databaseReference.child(fullPath);
    }

    /**
     * Writes data to a specific path in the database.
     *
     * @param fullPath The full path to write to.
     * @param data     The data to write.
     * @param callback The callback to be invoked upon completion.
     */
    protected void writeData(@NotNull final String fullPath, @NotNull final Object data, final @Nullable IDatabaseService.DatabaseCallback<Void> callback) {
        readData(fullPath).setValue(data, (error, ref) -> {
            if (callback == null) return;
            if (error != null) {
                callback.onFailed(error.toException());
            } else {
                callback.onCompleted(null);
            }
        });
    }

    /**
     * Deletes data from a specific path in the database.
     *
     * @param fullPath The full path to delete from.
     * @param callback The callback to be invoked upon completion.
     */
    protected void deleteData(@NotNull final String fullPath, @Nullable final IDatabaseService.DatabaseCallback<Void> callback) {
        readData(fullPath).removeValue((error, ref) -> {
            if (callback == null) return;
            if (error != null) {
                callback.onFailed(error.toException());
            } else {
                callback.onCompleted(null);
            }
        });
    }

    /**
     * Retrieves a single data object from a specific path.
     *
     * @param fullPath The full path to read from.
     * @param callback The callback to be invoked with the result.
     */
    protected void getData(@NotNull final String fullPath, @NotNull final IDatabaseService.DatabaseCallback<T> callback) {
        readData(fullPath).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data at " + fullPath, task.getException());
                // Add logging for problematic data if exception is DatabaseException
                if (task.getException() instanceof DatabaseException) {
                    Object problematicData = task.getResult() != null ? task.getResult().getValue() : "null DataSnapshot";
                    Log.e(TAG, "Problematic data at " + fullPath + " (before deserialization attempt): " + problematicData);
                }
                callback.onFailed(task.getException());
                return;
            }
            try {
                T data = task.getResult().getValue(clazz);
                callback.onCompleted(data);
            } catch (DatabaseException e) {
                Log.e(TAG, "Failed to deserialize data at " + fullPath + " into " + clazz.getSimpleName() + ": " + e.getMessage(), e);
                Object problematicData = task.getResult() != null ? task.getResult().getValue() : "null DataSnapshot";
                Log.e(TAG, "Problematic data at " + fullPath + " (during deserialization): " + problematicData);
                callback.onFailed(e);
            }
        });
    }

    /**
     * Retrieves a list of data objects from a specific path.
     *
     * @param fullPath The full path to read from.
     * @param callback The callback to be invoked with the list of results.
     */
    protected void getDataList(@NotNull final String fullPath, @NotNull final IDatabaseService.DatabaseCallback<List<T>> callback) {
        readData(fullPath).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data list from " + fullPath, task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                try {
                    T value = snapshot.getValue(clazz);
                    if (value != null) {
                        tList.add(value);
                    } else {
                        Log.w(TAG, "Skipping null value for snapshot: " + snapshot.getKey());
                    }
                } catch (DatabaseException e) {
                    Log.e(TAG, "Failed to deserialize snapshot " + snapshot.getKey() + " into " + clazz.getSimpleName() + ": " + e.getMessage(), e);
                    // Log the problematic data for inspection
                    Object problematicData = snapshot.getValue();
                    Log.e(TAG, "Problematic data for key " + snapshot.getKey() + ": " + problematicData);
                }
            }
            callback.onCompleted(tList);
        });
    }

    /**
     * Executes a transaction on a specific data path.
     *
     * @param fullPath The full path for the transaction.
     * @param function The function to apply to the current value of the entity.
     * @param callback The callback to be invoked upon completion.
     */
    protected void runTransaction(@NotNull final String fullPath, @NotNull final UnaryOperator<T> function, @Nullable final IDatabaseService.DatabaseCallback<T> callback) {
        readData(fullPath).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                if (currentValue != null) {
                    currentValue = function.apply(currentValue);
                }
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (callback == null) {
                    if (error != null)
                        Log.e(TAG, "Transaction failed silently at " + fullPath, error.toException());
                    return;
                }
                if (error != null) {
                    Log.e(TAG, "Transaction failed at " + fullPath, error.toException());
                    // If a DatabaseException occurred in doTransaction, currentData might be null or contain problematic data.
                    // This logging helps capture the state.
                    if (currentData != null) {
                        Object problematicData = currentData.getValue();
                        Log.e(TAG, "Problematic data during transaction at " + fullPath + ": " + problematicData);
                    }
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });
    }

    // endregion low-level helpers
}
