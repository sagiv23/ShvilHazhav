package com.example.sagivproject.bases;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.models.Idable;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic base adapter that handles list management and DiffUtil updates.
 *
 * @param <T>  The type of data, must implement Idable.
 * @param <VH> The ViewHolder type.
 */
public abstract class BaseAdapter<T extends Idable, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected final List<T> dataList = new ArrayList<>();

    /**
     * Updates the data set and dispatches updates to the adapter using DiffUtil.
     *
     * @param newData The new list of data.
     */
    public void setData(List<T> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(this.dataList, newData));
        this.dataList.clear();
        this.dataList.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /**
     * Gets the item at the specified position.
     *
     * @param position The position of the item in the list.
     * @return The item at the given position.
     */
    protected T getItem(int position) {
        return dataList.get(position);
    }

    private class DiffCallback extends DiffUtil.Callback {
        private final List<T> oldList;
        private final List<T> newList;

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

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            T oldItem = oldList.get(oldItemPosition);
            T newItem = newList.get(newItemPosition);
            if (oldItem.getId() == null || newItem.getId() == null) return false;
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}
