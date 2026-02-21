package com.example.sagivproject.adapters.diffUtils;

import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.GameRoom;

import java.util.List;
import java.util.Objects;

/**
 * A {@link DiffUtil.Callback} for calculating the difference between two lists of {@link GameRoom} objects.
 * <p>
 * This is used to efficiently update the RecyclerView that displays the game logs, only redrawing
 * the items that have changed.
 * </p>
 */
public class GameRoomDiffCallback extends DiffUtil.Callback {
    private final List<GameRoom> oldList;
    private final List<GameRoom> newList;

    public GameRoomDiffCallback(List<GameRoom> oldList, List<GameRoom> newList) {
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
     * Checks if two items represent the same object.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    /**
     * Checks if the contents of two items are the same.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        GameRoom oldItem = oldList.get(oldItemPosition);
        GameRoom newItem = newList.get(newItemPosition);

        // We only check for changes in status and score for UI updates.
        return Objects.equals(oldItem.getStatus(), newItem.getStatus())
                && oldItem.getPlayer1Score() == newItem.getPlayer1Score()
                && oldItem.getPlayer2Score() == newItem.getPlayer2Score();
    }
}
