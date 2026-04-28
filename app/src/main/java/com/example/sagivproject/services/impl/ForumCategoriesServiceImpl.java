package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;

import com.example.sagivproject.models.ForumCategory;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IForumCategoriesService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Implementation of the {@link IForumCategoriesService} interface.
 * <p>
 * This class manages forum categories. It intentionally bypasses {@code super.getAll()}
 * to provide real-time updates via {@link ValueEventListener}, which the base class
 * does not currently support in a generic way.
 * </p>
 */
public class ForumCategoriesServiceImpl extends BaseDatabaseService<ForumCategory> implements IForumCategoriesService {
    private static final String CATEGORIES_PATH = "forum_categories";

    /**
     * Constructs a new ForumCategoriesServiceImpl.
     *
     * @param firebaseDatabase The {@link FirebaseDatabase} instance.
     */
    @Inject
    public ForumCategoriesServiceImpl(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, CATEGORIES_PATH, ForumCategory.class);
    }

    /**
     * Retrieves all forum categories with real-time updates.
     * Note: We don't use {@code super.getAll()} because it performs a one-time fetch only.
     *
     * @param callback A callback invoked with the list of categories whenever the data changes.
     */
    @Override
    public void getCategories(IDatabaseService.DatabaseCallback<List<ForumCategory>> callback) {
        databaseReference.child(CATEGORIES_PATH).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ForumCategory> categories = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ForumCategory category = child.getValue(ForumCategory.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                if (callback != null) callback.onCompleted(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailed(error.toException());
            }
        });
    }

    /**
     * Adds a new forum category using the base service's create method.
     */
    @Override
    public void addCategory(String name, IDatabaseService.DatabaseCallback<Void> callback) {
        String categoryId = generateId();
        ForumCategory category = new ForumCategory(categoryId, name);
        create(category, callback);
    }

    /**
     * Deletes a forum category.
     * Since messages are stored nested within the category node,
     * deleting the category node automatically cleans up its messages.
     *
     * @param categoryId The ID of the category to delete.
     * @param callback   A callback to be invoked upon completion.
     */
    @Override
    public void deleteCategory(String categoryId, IDatabaseService.DatabaseCallback<Void> callback) {
        delete(categoryId, callback);
    }

    /**
     * Updates the name of an existing forum category using the base service's transaction method.
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
