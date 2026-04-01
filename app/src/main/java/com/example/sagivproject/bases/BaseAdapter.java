package com.example.sagivproject.bases;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.models.Idable;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic base adapter for {@link RecyclerView} that simplifies list management and data updates.
 * <p>
 * This class uses {@link DiffUtil} to calculate the minimum number of changes needed to update
 * the list, providing smooth animations and better performance compared to {@code notifyDataSetChanged()}.
 * </p>
 * @param <T> The type of the data items, which must implement the {@link Idable} interface.
 * @param <VH> The type of the {@link RecyclerView.ViewHolder} used by the adapter.
 */
public abstract class BaseAdapter<T extends Idable, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * The internal list containing the data items to be displayed.
     */
    protected final List<T> dataList = new ArrayList<>();

    /**
     * Updates the current data set with a new list of items.
     * <p>
     * This method triggers a {@link DiffUtil} calculation to determine the differences between
     * the old and new lists and automatically dispatches the necessary update events to the adapter.
     * </p>
     * @param newData The new list of data items to display.
     */
    public void setData(List<T> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(this.dataList, newData));
        this.dataList.clear();
        this.dataList.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The size of the internal data list.
     */
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /**
     * Retrieves the data item at the specified position in the list.
     * @param position The index of the item to retrieve.
     * @return The data item at the given position.
     * @throws IndexOutOfBoundsException If the position is out of range.
     */
    protected T getItem(int position) {
        return dataList.get(position);
    }

    /**
     * Returns the internal data list.
     * @return The list of items.
     */
    public List<T> getItemList() {
        return dataList;
    }

    /**
     * Internal callback class for {@link DiffUtil} to compare items in the list.
     */
    private class DiffCallback extends DiffUtil.Callback {
        private final List<T> oldList;
        private final List<T> newList;

        /**
         * Constructs a new DiffCallback.
         * @param oldList The current list of items.
         * @param newList The new list of items.
         */
        public DiffCallback(List<T> oldList, List<T> newList) {
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
         * Checks if two items represent the same entity (usually by comparing IDs).
         * @param oldItemPosition Position in the old list.
         * @param newItemPosition Position in the new list.
         * @return true if items represent the same entity.
         */
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            T oldItem = oldList.get(oldItemPosition);
            T newItem = newList.get(newItemPosition);
            if (oldItem.getId() == null || newItem.getId() == null) return false;
            return oldItem.getId().equals(newItem.getId());
        }

        /**
         * Checks if the contents of two items are the same.
         * @param oldItemPosition Position in the old list.
         * @param newItemPosition Position in the new list.
         * @return true if contents are identical.
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}