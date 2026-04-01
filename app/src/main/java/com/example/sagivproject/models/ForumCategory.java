package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Represents a single category in the application forum.
 * <p>
 * This class holds the metadata for a forum category, including its unique identifier
 * and display name. Categories are used to group forum messages by topic.
 * </p>
 */
public class ForumCategory implements Serializable, Idable {
    private String id;
    private String name;

    /** Default constructor required for Firebase deserialization. */
    public ForumCategory() {
    }

    /**
     * Constructs a new ForumCategory.
     * @param id The unique ID of the category.
     * @param name The display name of the category.
     */
    public ForumCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() { return this.id; }

    @Override
    public void setId(String id) { this.id = id; }

    /** @return The display name of the category. */
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @NonNull
    @Override
    public String toString() {
        return "ForumCategory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}