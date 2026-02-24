package com.example.sagivproject.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.Idable;

import java.util.Objects;

/**
 * A generic {@link DiffUtil.ItemCallback} for calculating the difference between two lists of objects
 * that implement the {@link Idable} interface.
 * <p>
 * This class simplifies the implementation of {@link DiffUtil} for any data model that has a unique ID,
 * making it reusable across different ListAdapters.
 *
 * @param <T> The type of the items in the list, which must extend {@link Idable}.
 */
public class GenericDiffCallback<T extends Idable> extends DiffUtil.ItemCallback<T> {
    /**
     * Called by DiffUtil to decide whether two objects represent the same item.
     * <p>
     * It compares the items based on their unique ID from the {@link Idable} interface.
     *
     * @return True if the two items have the same ID, false otherwise.
     */
    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        // If either ID is null, items are not the same.
        if (oldItem.getId() == null || newItem.getId() == null) {
            return false;
        }
        return oldItem.getId().equals(newItem.getId());
    }

    /**
     * Called by DiffUtil to check whether two items have the same data.
     * <p>
     * This implementation uses the {@link Objects#equals(Object, Object)} method to perform
     * a deep comparison of the item's content. For this to work correctly, the data model class
     * should override the {@code equals()} method.
     *
     * @return True if the contents of the items are the same, false otherwise.
     */
    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return Objects.equals(oldItem, newItem);
    }
}
