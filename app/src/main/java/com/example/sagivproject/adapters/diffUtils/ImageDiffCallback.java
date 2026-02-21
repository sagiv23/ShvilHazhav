package com.example.sagivproject.adapters.diffUtils;

import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.ImageData;

import java.util.List;

/**
 * A {@link DiffUtil.Callback} for calculating the difference between two lists of {@link ImageData} objects.
 * <p>
 * This is used to efficiently update the RecyclerView in the admin screen for managing game images.
 * </p>
 */
public class ImageDiffCallback extends DiffUtil.Callback {
    private final List<ImageData> oldList;
    private final List<ImageData> newList;

    public ImageDiffCallback(List<ImageData> oldList, List<ImageData> newList) {
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
     * Checks if two items represent the same object based on their ID.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    /**
     * Checks if the contents (the Base64 string) of two items are the same.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getBase64().equals(newList.get(newItemPosition).getBase64());
    }
}
