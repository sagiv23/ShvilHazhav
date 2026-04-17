package com.example.sagivproject.models;

import com.example.sagivproject.bases.BaseAdapter;

import java.io.Serializable;

/**
 * An interface for data models that have a unique identifier.
 * <p>
 * This provides a common contract for models that can be stored and retrieved
 * from the database using a string-based ID. It is required for use with
 * the {@link BaseAdapter}.
 * </p>
 */
public interface Idable extends Serializable {
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