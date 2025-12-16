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
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.setAlpha(1f);
        holder.itemView.setTranslationX(0f);

        //מצב תצוגה
        if (card.isRevealed() || card.isMatched()) {
            holder.cardImage.setImageResource(card.getImageResId());
        } else {
            holder.cardImage.setImageResource(R.drawable.fold_card_img);
        }

        holder.itemView.setOnClickListener(v -> listener.onCardClicked(card, holder.itemView, holder.cardImage));
    }

    @Override
    public int getItemCount() {
        return cards.size();
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