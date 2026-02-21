package com.example.sagivproject.adapters.diffUtils;

import androidx.recyclerview.widget.DiffUtil;

import com.example.sagivproject.models.Card;

import java.util.List;

/**
 * A {@link DiffUtil.Callback} for calculating the difference between two lists of {@link Card} objects.
 * <p>
 * This is used to efficiently update the RecyclerView in the memory game, only animating the items
 * that have actually changed their state (revealed or matched).
 * </p>
 */
public class CardDiffCallback extends DiffUtil.Callback {
    private final List<Card> oldList;
    private final List<Card> newList;

    public CardDiffCallback(List<Card> oldList, List<Card> newList) {
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
     * Since cards can be identical (pairs), and their positions are fixed throughout the game,
     * we can assume items at the same position are the same item.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // In a static grid like a memory game, the item at a position is always the same item.
        return oldItemPosition == newItemPosition;
    }

    /**
     * Checks if the contents (state) of a card have changed.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Card oldCard = oldList.get(oldItemPosition);
        Card newCard = newList.get(newItemPosition);
        // We only care about changes in the revealed or matched state for UI updates.
        return oldCard.getIsRevealed() == newCard.getIsRevealed() &&
                oldCard.getIsMatched() == newCard.getIsMatched();
    }
}
