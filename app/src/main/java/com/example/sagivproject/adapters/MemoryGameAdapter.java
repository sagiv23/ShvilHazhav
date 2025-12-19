package com.example.sagivproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.models.Card;

import java.util.List;

public class MemoryGameAdapter extends RecyclerView.Adapter<MemoryGameAdapter.CardViewHolder> {
    public interface MemoryGameListener {
        void onCardClicked(Card card, View itemView, ImageView imageView);
    }

    private final List<Card> cards;
    private final MemoryGameListener listener;

    public MemoryGameAdapter(List<Card> cards, MemoryGameListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    public List<Card> getCards() {
        return cards;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);

        //איפוס אנימציות
        holder.itemView.animate().cancel();
        holder.itemView.setTranslationX(0f);
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.setAlpha(1f);

        if (card.isRevealed() && !card.getWasRevealed()) {
            animateFlipOpen(holder.cardImage, card.getImageResId());
        }

        if (!card.isRevealed() && card.getWasRevealed()) {
            animateFlipClose(holder.cardImage);
        }

        if (card.isMatched()) {
            holder.itemView.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .alpha(0.6f)
                    .setDuration(300)
                    .start();
        }

        card.setWasRevealed(card.isRevealed());

        holder.itemView.setOnClickListener(v -> listener.onCardClicked(card, holder.itemView, holder.cardImage));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    private void animateFlipOpen(ImageView imageView, int imageResId) {
        imageView.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {
                    imageView.setImageResource(imageResId);
                    imageView.setRotationY(-90f);
                    imageView.animate()
                            .rotationY(0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void animateFlipClose(ImageView imageView) {
        imageView.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {
                    imageView.setImageResource(R.drawable.fold_card_img);
                    imageView.setRotationY(-90f);
                    imageView.animate()
                            .rotationY(0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.img_ItemCard_card_image);
            float scale = itemView.getResources().getDisplayMetrics().density;
            cardImage.setCameraDistance(8000 * scale);
        }
    }
}