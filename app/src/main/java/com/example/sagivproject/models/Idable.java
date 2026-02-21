package com.example.sagivproject.models;

/**
 * An interface for data models that have a unique identifier.
 * <p>
 * This provides a common contract for models that can be stored and retrieved
 * from the database using a string-based ID.
 * </p>
 */
public interface Idable {
    /**
     * Gets the unique identifier of the object.
     *
     * @return The unique ID string.
     */
    String getId();

    /**
     * Sets the unique identifier of the object.
     *
     * @param id The new unique ID string.
     */
    void setId(String id);
}
