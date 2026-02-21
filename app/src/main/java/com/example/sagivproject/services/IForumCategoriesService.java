package com.example.sagivproject.services;

import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to forum categories.
 * <p>
 * This service handles fetching, adding, and deleting forum categories.
 * </p>
 */
public interface IForumCategoriesService {
    /**
     * Retrieves a list of all forum categories from the database, with real-time updates.
     *
     * @param callback A callback to be invoked with the list of categories.
     */
    void getCategories(DatabaseCallback<List<ForumCategory>> callback);

    /**
     * Adds a new forum category to the database.
     *
     * @param name     The name for the new category.
     * @param callback A callback to be invoked upon completion.
     */
    void addCategory(String name, DatabaseCallback<Void> callback);

    /**
     * Deletes a forum category and all of its associated messages from the database.
     *
     * @param categoryId The ID of the category to delete.
     * @param callback   A callback to be invoked upon completion.
     */
    void deleteCategory(String categoryId, DatabaseCallback<Void> callback);
}
