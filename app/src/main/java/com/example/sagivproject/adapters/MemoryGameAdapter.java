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
import com.example.sagivproject.screens.MemoryGameActivity;
import com.example.sagivproject.utils.ImageUtil;

import java.util.List;

import javax.inject.Inject;

/**
 * A RecyclerView adapter for displaying the cards in the memory game.
 */
public class MemoryGameAdapter extends BaseAdapter<Card, MemoryGameAdapter.CardViewHolder> {
    private static final int CAMERA_DISTANCE = 8000;
    private MemoryGameListener listener;

    @Inject
    public MemoryGameAdapter() {
    }

    public void setListener(MemoryGameListener listener) {
        this.listener = listener;
    }

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

        holder.itemView.animate().cancel();
        holder.cardImage.animate().cancel();

        holder.itemView.setTranslationX(0f);
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.setAlpha(1f);
        holder.cardImage.setRotationY(0f);

        if (card.getIsMatched() || card.getIsRevealed()) {
            ImageUtil.loadImage(card.getBase64Content(), holder.cardImage);

            if (card.getIsMatched()) {
                holder.itemView.setAlpha(0.6f);
            }
        } else {
            holder.cardImage.setImageResource(R.drawable.fold_card_img);
        }

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

    private void animateFlipClose(ImageView imageView) {
        imageView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            imageView.setImageResource(R.drawable.fold_card_img);
            imageView.setRotationY(-90f);
            imageView.animate().rotationY(0f).setDuration(150).start();
        }).start();
    }

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

    public void animateSuccess(int position, RecyclerView recyclerView) {
        CardViewHolder holder = (CardViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            View view = holder.itemView;
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction(() ->
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start()
            ).start();
        }
    }

    public interface MemoryGameListener {
        void onCardClicked(Card card, int position);
    }

    public static class CardViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        final ImageView cardImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.img_ItemCard_card_image);
            float scale = itemView.getResources().getDisplayMetrics().density;
            cardImage.setCameraDistance(CAMERA_DISTANCE * scale);
        }
    }
}
