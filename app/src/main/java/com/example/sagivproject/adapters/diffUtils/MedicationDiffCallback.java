package com.example.sagivproject.adapters.diffUtils;

import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.Medication;

import java.util.List;
import java.util.Objects;

/**
 * A {@link DiffUtil.Callback} for calculating the difference between two lists of {@link Medication} objects.
 * <p>
 * This is used to efficiently update the RecyclerView that displays a user's medication list.
 * </p>
 */
public class MedicationDiffCallback extends DiffUtil.Callback {
    private final List<Medication> oldList;
    private final List<Medication> newList;

    public MedicationDiffCallback(List<Medication> oldList, List<Medication> newList) {
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
     * Checks if the contents of two items are the same by comparing all their fields.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Medication oldItem = oldList.get(oldItemPosition);
        Medication newItem = newList.get(newItemPosition);

        return Objects.equals(oldItem.getId(), newItem.getId())
                && Objects.equals(oldItem.getName(), newItem.getName())
                && Objects.equals(oldItem.getDetails(), newItem.getDetails())
                && Objects.equals(oldItem.getType(), newItem.getType())
                && Objects.equals(oldItem.getReminderHours(), newItem.getReminderHours());
    }
}
