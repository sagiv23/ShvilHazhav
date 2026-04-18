package com.example.sagivproject.services.impl;

import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IForumCategoriesService;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import javax.inject.Inject;

/**
 * Implementation of the {@link IForumCategoriesService} interface.
 * <p>
 * This class provides methods for managing forum categories in the Firebase Realtime Database.
 * It handles the retrieval of categories with real-time updates and ensures that when a category
 * is deleted, its associated messages are also removed to maintain data integrity and prevent
 * orphaned data nodes.
 * </p>
 */
public class ForumCategoriesServiceImpl extends BaseDatabaseService<ForumCategory> implements IForumCategoriesService {
    private static final String CATEGORIES_PATH = "forum_categories";
    private static final String MESSAGES_PATH = "forum_messages";

    /**
     * Constructs a new ForumCategoriesServiceImpl.
     * Initializes the base service with the forum categories root path.
     *
     * @param firebaseDatabase The {@link FirebaseDatabase} instance.
     */
    @Inject
    public ForumCategoriesServiceImpl(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, CATEGORIES_PATH, ForumCategory.class);
    }

    /**
     * Retrieves all forum categories from the database.
     *
     * @param callback A callback invoked with the full list of {@link ForumCategory} objects.
     */
    @Override
    public void getCategories(IDatabaseService.DatabaseCallback<List<ForumCategory>> callback) {
        super.getAll(callback);
    }

    /**
     * Creates a new forum category with a unique generated ID.
     *
     * @param name     The name for the new category.
     * @param callback A callback to be invoked upon completion.
     */
    @Override
    public void addCategory(String name, IDatabaseService.DatabaseCallback<Void> callback) {
        String categoryId = super.generateId();
        ForumCategory category = new ForumCategory(categoryId, name);
        super.create(category, callback);
    }

    /**
     * Deletes a forum category and all associated messages.
     * <p>
     * This operation first cleans up the message node for the category to ensure
     * no orphaned messages remain in the database before removing the category definition itself.
     * </p>
     *
     * @param categoryId The ID of the category to delete.
     * @param callback   A callback to be invoked when the entire operation is complete.
     */
    @Override
    public void deleteCategory(String categoryId, IDatabaseService.DatabaseCallback<Void> callback) {

        deleteData(MESSAGES_PATH + "/" + categoryId, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void result) {
                delete(categoryId, callback);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }

    /**
     * Updates the name of an existing forum category using an atomic transaction.
     *
     * @param categoryId The ID of the category to update.
     * @param newName    The new display name for the category.
     * @param callback   A callback to be invoked upon completion.
     */
    @Override
    public void updateCategoryName(String categoryId, String newName, IDatabaseService.DatabaseCallback<Void> callback) {
        update(categoryId, category -> {
            category.setName(newName);
            return category;
        }, callback != null ? new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(ForumCategory result) {
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        } : null);
    }
}