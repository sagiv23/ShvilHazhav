package com.example.sagivproject.adapters;

import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.Idable;

import java.util.List;
import java.util.Objects;

/**
 * A generic {@link DiffUtil.Callback} for calculating the difference between two lists of objects
 * that implement the {@link Idable} interface.
 * <p>
 * This class simplifies the implementation of {@link DiffUtil} for any data model that has a unique ID,
 * making it reusable across different RecyclerView adapters.
 *
 * @param <T> The type of the items in the list, which must extend {@link Idable}.
 */
public class GenericDiffCallback<T extends Idable> extends DiffUtil.Callback {
    private final List<T> oldList;
    private final List<T> newList;

    /**
     * Constructs a new GenericDiffCallback.
     *
     * @param oldList The old list of items.
     * @param newList The new list of items.
     */
    public GenericDiffCallback(List<T> oldList, List<T> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    /**
     * Called by DiffUtil to decide whether two objects represent the same item.
     * <p>
     * It compares the items based on their unique ID from the {@link Idable} interface.
     *
     * @param oldItemPosition The position of the item in the old list.
     * @param newItemPosition The position of the item in the new list.
     * @return True if the two items have the same ID, false otherwise.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = oldList.get(oldItemPosition);
        T newItem = newList.get(newItemPosition);
        return oldItem.getId().equals(newItem.getId());
    }

    /**
     * Called by DiffUtil to check whether two items have the same data.
     * <p>
     * This implementation uses the {@link Objects#equals(Object, Object)} method to perform
     * a deep comparison of the item's content. For this to work correctly, the data model class
     * should override the {@code equals()} method.
     *
     * @param oldItemPosition The position of the item in the old list.
     * @param newItemPosition The position of the item in the new list.
     * @return True if the contents of the items are the same, false otherwise.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        T oldItem = oldList.get(oldItemPosition);
        T newItem = newList.get(newItemPosition);
        return Objects.equals(oldItem, newItem);
    }
}
