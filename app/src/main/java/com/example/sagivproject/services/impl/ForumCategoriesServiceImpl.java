package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;

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
 * An implementation of the {@link IForumCategoriesService} interface.
 * <p>
 * This service handles operations related to forum categories, such as fetching, adding,
 * and deleting categories. It extends {@link BaseDatabaseService} to leverage common
 * Firebase database interactions.
 * </p>
 */
public class ForumCategoriesServiceImpl extends BaseDatabaseService<ForumCategory> implements IForumCategoriesService {
    private static final String CATEGORIES_PATH = "forum_categories";
    private static final String MESSAGES_PATH = "forum_messages"; // For cleaning up messages on category deletion

    /**
     * Constructs a new ForumCategoriesServiceImpl.
     */
    @Inject
    public ForumCategoriesServiceImpl() {
        super(CATEGORIES_PATH, ForumCategory.class);
    }

    /**
     * Retrieves all forum categories, ordered by their creation timestamp, with real-time updates.
     *
     * @param callback The callback to be invoked with the list of categories or an error.
     */
    @Override
    public void getCategories(IDatabaseService.DatabaseCallback<List<ForumCategory>> callback) {
        databaseReference.child(CATEGORIES_PATH).orderByChild("orderTimestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ForumCategory> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ForumCategory category = child.getValue(ForumCategory.class);
                    list.add(category);
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
     * Adds a new forum category.
     *
     * @param name     The name of the new category.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void addCategory(String name, IDatabaseService.DatabaseCallback<Void> callback) {
        String categoryId = super.generateId();
        ForumCategory category = new ForumCategory(categoryId, name);
        super.create(category, callback);
    }

    /**
     * Deletes a forum category and all the messages within it.
     *
     * @param categoryId The ID of the category to delete.
     * @param callback   The callback to be invoked upon completion.
     */
    @Override
    public void deleteCategory(String categoryId, IDatabaseService.DatabaseCallback<Void> callback) {
        // First, delete all messages associated with this category to prevent orphaned data.
        deleteData(MESSAGES_PATH + "/" + categoryId, new IDatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void result) {
                // After the messages are successfully deleted, delete the category entry itself.
                delete(categoryId, callback);
            }

            @Override
            public void onFailed(Exception e) {
                // If deleting the messages fails, report the error for the whole operation.
                if (callback != null) {
                    callback.onFailed(e);
                }
            }
        });
    }
}
