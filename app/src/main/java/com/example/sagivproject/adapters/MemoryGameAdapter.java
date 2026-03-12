package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseAdapter;
import com.example.sagivproject.models.Card;
import com.example.sagivproject.utils.ImageUtil;

import java.util.List;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying the cards in the memory game.
 * <p>
 * This adapter manages the visual state of the game cards, including their revealed/hidden status,
 * matching animations, and click handling. It uses {@link ImageUtil} to load card images from Base64 strings.
 * </p>
 */
public class MemoryGameAdapter extends BaseAdapter<Card, MemoryGameAdapter.CardViewHolder> {
    private static final int CAMERA_DISTANCE = 8000;
    private final ImageUtil imageUtil;
    private MemoryGameListener listener;

    /**
     * Constructs a new MemoryGameAdapter.
     *
     * @param imageUtil A utility for loading and processing images.
     */
    @Inject
    public MemoryGameAdapter(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    /**
     * Sets the listener for game interaction events.
     *
     * @param listener The listener to be notified when a card is clicked.
     */
    public void setListener(MemoryGameListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of cards in the game.
     *
     * @param cards The new list of {@link Card} objects.
     */
    public void setCards(List<Card> cards) {
        setData(cards);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = getItem(position);

        // Reset animations and transformations to prevent visual artifacts during recycling
        holder.itemView.animate().cancel();
        holder.cardImage.animate().cancel();

        holder.itemView.setTranslationX(0f);
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.setAlpha(1f);
        holder.cardImage.setRotationY(0f);

        // Display card content or back based on state
        if (card.getIsMatched() || card.getIsRevealed()) {
            imageUtil.loadImage(card.getBase64Content(), holder.cardImage);

            if (card.getIsMatched()) {
                holder.itemView.setAlpha(0.6f);
            }
        } else {
            holder.cardImage.setImageResource(R.drawable.fold_card_img);
        }

        // Handle flip animations based on state transitions
        if (card.getIsRevealed() && !card.wasRevealed()) {
            animateFlipOpen(holder.cardImage, card.getBase64Content());
            card.setWasRevealed(true);
        } else if (!card.getIsRevealed() && card.wasRevealed()) {
            animateFlipClose(holder.cardImage);
            card.setWasRevealed(false);
        }

        // Determine clickability based on game turn and card state
        boolean isMyTurn = listener != null && listener.isMyTurn();
        holder.itemView.setClickable(isMyTurn && !card.getIsMatched() && !card.getIsRevealed());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onCardClicked(getItem(currentPosition), currentPosition);
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull CardViewHolder holder) {
        super.onViewRecycled(holder);
        holder.itemView.animate().cancel();
        holder.cardImage.animate().cancel();
    }

    /**
     * Animates the card flipping to reveal its content.
     *
     * @param imageView The ImageView to animate.
     * @param base64    The Base64 string of the image to reveal.
     */
    private void animateFlipOpen(ImageView imageView, String base64) {
        imageView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            Runnable flipIn = () -> {
                imageView.setRotationY(-90f);
                imageView.animate().rotationY(0f).setDuration(150).start();
            };

            if (base64 != null) {
                imageUtil.loadImage(base64, imageView);
                flipIn.run();
            } else {
                imageView.setImageResource(R.drawable.fold_card_img);
                flipIn.run();
            }
        }).start();
    }

    /**
     * Animates the card flipping back to its hidden state.
     *
     * @param imageView The ImageView to animate.
     */
    private void animateFlipClose(ImageView imageView) {
        imageView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            imageView.setImageResource(R.drawable.fold_card_img);
            imageView.setRotationY(-90f);
            imageView.animate().rotationY(0f).setDuration(150).start();
        }).start();
    }

    /**
     * Performs a shake animation to indicate an error (e.g., mismatched cards).
     *
     * @param position     The position of the card to animate.
     * @param recyclerView The RecyclerView containing the card.
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
     * Performs a scale animation to indicate success (e.g., matched cards).
     *
     * @param position     The position of the card to animate.
     * @param recyclerView The RecyclerView containing the card.
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
     * An interface for handling card click events in the memory game.
     */
    public interface MemoryGameListener {
        /**
         * Called when a card is clicked.
         *
         * @param card     The card that was clicked.
         * @param position The position of the card in the list.
         */
        void onCardClicked(Card card, int position);

        /**
         * Checks if it is currently the user's turn.
         *
         * @return True if it is the user's turn, false otherwise.
         */
        boolean isMyTurn();
    }

    /**
     * A ViewHolder that describes a card item view and metadata about its place within the RecyclerView.
     */
    public static class CardViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final ImageView cardImage;

        /**
         * Initializes the ViewHolder with the item view and sets the camera distance for 3D flip effects.
         *
         * @param itemView The view representing a single card.
         */
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.img_ItemCard_card_image);
            float scale = itemView.getResources().getDisplayMetrics().density;
            cardImage.setCameraDistance(CAMERA_DISTANCE * scale);
        }
    }
}
