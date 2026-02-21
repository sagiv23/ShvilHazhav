package com.example.sagivproject.adapters.diffUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.ForumCategory;

/**
 * A {@link DiffUtil.ItemCallback} for calculating the difference between two lists of {@link ForumCategory} objects.
 * <p>
 * This is used by {@link androidx.recyclerview.widget.ListAdapter} to efficiently update the RecyclerView
 * when the list of forum categories changes.
 * </p>
 */
public class ForumCategoryDiffCallback extends DiffUtil.ItemCallback<ForumCategory> {
    /**
     * Checks if two items represent the same object.
     */
    @Override
    public boolean areItemsTheSame(@NonNull ForumCategory oldItem, @NonNull ForumCategory newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    /**
     * Checks if the contents of two items are the same.
     */
    @Override
    public boolean areContentsTheSame(@NonNull ForumCategory oldItem, @NonNull ForumCategory newItem) {
        return oldItem.getName().equals(newItem.getName());
    }
}
