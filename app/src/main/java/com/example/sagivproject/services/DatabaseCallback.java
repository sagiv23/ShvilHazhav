package com.example.sagivproject.services;

/**
 * A generic callback interface for handling the results of asynchronous database operations.
 *
 * @param <T> The type of the data returned upon successful completion.
 */
public interface DatabaseCallback<T> {
    /**
     * Invoked when the asynchronous operation completes successfully.
     *
     * @param object The resulting data object (can be null if the operation has no return value).
     */
    void onCompleted(T object);

    /**
     * Invoked when the asynchronous operation fails due to an error or exception.
     *
     * @param e The exception that occurred during the process.
     */
    void onFailed(Exception e);
}
