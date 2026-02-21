package com.example.sagivproject.adapters.diffUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.ForumMessage;

/**
 * A {@link DiffUtil.ItemCallback} for calculating the difference between two lists of {@link ForumMessage} objects.
 * <p>
 * This is used by {@link androidx.recyclerview.widget.ListAdapter} to efficiently update the RecyclerView
 * when the list of forum messages changes.
 * </p>
 */
public class ForumDiffCallback extends DiffUtil.ItemCallback<ForumMessage> {
    /**
     * Checks if two items represent the same object.
     */
    @Override
    public boolean areItemsTheSame(@NonNull ForumMessage oldItem, @NonNull ForumMessage newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    /**
     * Checks if the contents of two items are the same.
     */
    @Override
    public boolean areContentsTheSame(@NonNull ForumMessage oldItem, @NonNull ForumMessage newItem) {
        // We only check the message and full name for changes, as these are the most likely to be updated.
        return oldItem.getMessage().equals(newItem.getMessage())
                && oldItem.getFullName().equals(newItem.getFullName());
    }
}
