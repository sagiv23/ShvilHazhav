package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.Card;
import com.example.sagivproject.screens.MemoryGameActivity;
import com.example.sagivproject.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView adapter for displaying the cards in the memory game.
 * <p>
 * This adapter manages the visual representation of each {@link Card}, including its flipped state
 * and animations for revealing, hiding, matching, and error states. It communicates user
 * interactions, such as card clicks, back to the hosting activity or fragment through the
 * {@link MemoryGameListener}.
 * </p>
 */
public class MemoryGameAdapter extends RecyclerView.Adapter<MemoryGameAdapter.CardViewHolder> {
    private static final int CAMERA_DISTANCE = 8000;
    private final MemoryGameListener listener;
    private final List<Card> cardList;

    public MemoryGameAdapter(MemoryGameListener listener) {
        this.listener = listener;
        this.cardList = new ArrayList<>();
    }

    public void setCards(List<Card> cards) {
        cardList.clear();
        cardList.addAll(cards);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);

        // Reset animations
        holder.itemView.animate().cancel();
        holder.itemView.setTranslationX(0f);
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.setAlpha(1f);

        if (card.getIsMatched() || card.getIsRevealed()) {
            ImageUtil.loadImage(card.getBase64Content(), holder.cardImage);

            if (card.getIsMatched()) {
                holder.itemView.setAlpha(0.6f);
            }
        } else {
            holder.cardImage.setImageResource(R.drawable.fold_card_img);
        }

        // Handle flip animations based on state changes
        if (card.getIsRevealed() && !card.wasRevealed()) {
            animateFlipOpen(holder.cardImage, card.getBase64Content());
            card.setWasRevealed(true);
        } else if (!card.getIsRevealed() && card.wasRevealed()) {
            animateFlipClose(holder.cardImage);
            card.setWasRevealed(false);
        }

        boolean isMyTurn = listener instanceof MemoryGameActivity && ((MemoryGameActivity) listener).isMyTurn();
        holder.itemView.setClickable(isMyTurn && !card.getIsMatched() && !card.getIsRevealed());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onCardClicked(cardList.get(currentPosition), currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    /**
     * Animates a card flipping open to reveal its content.
     */
    private void animateFlipOpen(ImageView imageView, String base64) {
        imageView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            Runnable flipIn = () -> {
                imageView.setRotationY(-90f);
                imageView.animate().rotationY(0f).setDuration(150).start();
            };

            if (base64 != null) {
                ImageUtil.loadImage(base64, imageView);
                flipIn.run();
            } else {
                imageView.setImageResource(R.drawable.fold_card_img);
                flipIn.run();
            }
        }).start();
    }

    /**
     * Animates a card flipping closed to hide its content.
     */
    private void animateFlipClose(ImageView imageView) {
        imageView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            imageView.setImageResource(R.drawable.fold_card_img);
            imageView.setRotationY(-90f);
            imageView.animate().rotationY(0f).setDuration(150).start();
        }).start();
    }

    /**
     * Triggers a shake animation to indicate an incorrect match.
     */
    public void animateError(int position, RecyclerView recyclerView) {
        CardViewHolder holder = (CardViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            View view = holder.itemView;
            view.animate().translationXBy(-20f).setDuration(50).withEndAction(() ->
                    view.animate().translationXBy(40f).setDuration(50).withEndAction(() ->
                            view.animate().translationX(0f).setDuration(50).start()
                    ).start()
            ).start();
        }
    }

    /**
     * Triggers a pulse animation to indicate a successful match.
     */
    public void animateSuccess(int position, RecyclerView recyclerView) {
        CardViewHolder holder = (CardViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            View view = holder.itemView;
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction(() ->
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start()
            ).start();
        }
    }

    /**
     * An interface for listeners that handle card click events.
     */
    public interface MemoryGameListener {
        void onCardClicked(Card card, int position);
    }

    /**
     * A ViewHolder that describes an item view and metadata about its place within the RecyclerView.
     */
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        final ImageView cardImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.img_ItemCard_card_image);
            float scale = itemView.getResources().getDisplayMetrics().density;
            cardImage.setCameraDistance(CAMERA_DISTANCE * scale);
        }
    }
}
