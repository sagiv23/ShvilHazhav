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
 * A RecyclerView adapter for managing the cards in the online memory game.
 * <p>
 * This adapter handles the visual state of game cards, including their revealed/hidden status,
 * matching transparency, and complex 3D flip animations. It relies on {@link ImageUtil}
 * to decode and display card content from Base64 strings.
 * </p>
 */
public class MemoryGameAdapter extends BaseAdapter<Card, MemoryGameAdapter.CardViewHolder> {
    private static final int CAMERA_DISTANCE = 8000;
    private final ImageUtil imageUtil;
    private MemoryGameListener listener;

    /**
     * Constructs a new MemoryGameAdapter.
     *
     * @param imageUtil A utility class for image processing and loading.
     */
    @Inject
    public MemoryGameAdapter(ImageUtil imageUtil) {
        this.imageUtil = imageUtil;
    }

    /**
     * Sets the listener for game interaction events.
     *
     * @param listener The {@link MemoryGameListener} to be notified when a card is clicked.
     */
    public void setListener(MemoryGameListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the board with a new set of cards.
     *
     * @param cards The list of {@link Card} objects to display.
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

        // Reset state to prevent visual artifacts from view recycling
        holder.itemView.animate().cancel();
        holder.cardImage.animate().cancel();
        holder.itemView.setTranslationX(0f);
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.setAlpha(1f);
        holder.cardImage.setRotationY(0f);

        if (card.getIsMatched() || card.getIsRevealed()) {
            imageUtil.loadImage(card.getBase64Content(), holder.cardImage);
            if (card.getIsMatched()) {
                holder.itemView.setAlpha(0.6f); // Dim matched cards
            }
        } else {
            holder.cardImage.setImageResource(R.drawable.fold_card_img);
        }

        // Logic for triggering flip animations
        if (card.getIsRevealed() && !card.wasRevealed()) {
            animateFlipOpen(holder.cardImage, card.getBase64Content());
            card.setWasRevealed(true);
        } else if (!card.getIsRevealed() && card.wasRevealed()) {
            animateFlipClose(holder.cardImage);
            card.setWasRevealed(false);
        }

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
     * Animates the card flipping open to reveal its content.
     *
     * @param imageView The {@link ImageView} to animate.
     * @param base64    The Base64 content to load onto the card mid-flip.
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
     * Animates the card flipping closed to hide its content.
     *
     * @param imageView The {@link ImageView} to animate.
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
     * @param position     The position of the card in the adapter.
     * @param recyclerView The RecyclerView containing the view holder.
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
     * @param position     The position of the card in the adapter.
     * @param recyclerView The RecyclerView containing the view holder.
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
     * Interface for handling interaction events in the memory game.
     */
    public interface MemoryGameListener {
        /**
         * Called when a card item is clicked.
         *
         * @param card     The {@link Card} object that was clicked.
         * @param position The adapter position of the card.
         */
        void onCardClicked(Card card, int position);

        /**
         * Checks if it is currently the local user's turn to play.
         *
         * @return true if it is the user's turn.
         */
        boolean isMyTurn();
    }

    /**
     * ViewHolder for memory game card items.
     */
    public static class CardViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        /**
         * The ImageView representing the card surface.
         */
        final ImageView cardImage;

        /**
         * Constructs a new CardViewHolder and sets up 3D perspective.
         *
         * @param itemView The root view of the card item.
         */
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.img_ItemCard_card_image);
            float scale = itemView.getResources().getDisplayMetrics().density;
            cardImage.setCameraDistance(CAMERA_DISTANCE * scale);
        }
    }
}
