package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IForumCategoriesService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
     */
    @Inject
    public ForumCategoriesServiceImpl() {
        super(CATEGORIES_PATH, ForumCategory.class);
    }

    /**
     * Retrieves all forum categories from the database, ordered by an internal timestamp.
     * Uses a {@link ValueEventListener} to provide real-time updates to the caller.
     *
     * @param callback A callback invoked with the full list of {@link ForumCategory} objects.
     */
    @Override
    public void getCategories(IDatabaseService.DatabaseCallback<List<ForumCategory>> callback) {
        databaseReference.child(CATEGORIES_PATH).orderByChild("orderTimestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ForumCategory> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ForumCategory category = child.getValue(ForumCategory.class);
                    if (category != null) {
                        list.add(category);
                    }
                }
                if (callback != null) callback.onCompleted(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailed(error.toException());
            }
        });
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
        }, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(ForumCategory result) {
                if (callback != null) callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }
}