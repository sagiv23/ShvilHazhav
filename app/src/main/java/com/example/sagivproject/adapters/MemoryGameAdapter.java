package com.example.sagivproject.adapters;

import android.graphics.Bitmap;
import android.util.Base64;
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

import java.util.List;

public class MemoryGameAdapter extends RecyclerView.Adapter<MemoryGameAdapter.CardViewHolder> {
    public interface MemoryGameListener {
        void onCardClicked(Card card, View itemView, ImageView imageView);
    }

    private final List<Card> cards;
    private final MemoryGameListener listener;
    private static final int CAMERA_DISTANCE = 8000;

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

        holder.itemView.setClickable(!card.getIsMatched() && !card.getIsRevealed());

        //איפוס אנימציות
        holder.itemView.animate().cancel();
        holder.itemView.setTranslationX(0f);
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);
        holder.itemView.setAlpha(1f);

        // לוגיקת הצגת הקלף
        if (card.getIsMatched() || card.getIsRevealed()) {
            // המרה של מחרוזת ה-Base64 ל-Bitmap והצגתו
            if (card.getBase64Content() != null) {
                byte[] decodedString = Base64.decode(card.getBase64Content(), Base64.DEFAULT);
                Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.cardImage.setImageBitmap(decodedByte);
            }

            if (card.getIsMatched()) {
                holder.itemView.setAlpha(0.6f);
            }
        } else {
            // הצגת גב הקלף מה-drawable המקומי
            holder.cardImage.setImageResource(R.drawable.fold_card_img);
        }

        // אנימציות הפיכה מבוססות שינוי סטטוס
        if (card.getIsRevealed() && !card.getWasRevealed()) {
            animateFlipOpen(holder.cardImage, card.getBase64Content());
            card.setWasRevealed(true);
        } else if (!card.getIsRevealed() && card.getWasRevealed()) {
            animateFlipClose(holder.cardImage);
            card.setWasRevealed(false);
        }

        boolean isMyTurn = listener instanceof MemoryGameActivity && ((MemoryGameActivity) listener).isMyTurn();

        holder.itemView.setClickable(
                isMyTurn &&
                        !card.getIsMatched() &&
                        !card.getIsRevealed()
        );

        holder.itemView.setOnClickListener(v -> listener.onCardClicked(card, holder.itemView, holder.cardImage));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    // אנימציית הפיכה לפתיחה
    private void animateFlipOpen(ImageView imageView, String base64) {
        imageView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            if (base64 != null) {
                imageView.setImageBitmap(ImageUtil.convertFrom64base(base64));
            }
            imageView.setRotationY(-90f);
            imageView.animate().rotationY(0f).setDuration(150).start();
        }).start();
    }

    // אנימציית הפיכה לסגירה
    private void animateFlipClose(ImageView imageView) {
        imageView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            imageView.setImageResource(R.drawable.fold_card_img);
            imageView.setRotationY(-90f);
            imageView.animate().rotationY(0f).setDuration(150).start();
        }).start();
    }

    // אנימציית שגיאה (רעד)
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

    // אנימציית הצלחה (פעימה)
    public void animateSuccess(int position, RecyclerView recyclerView) {
        CardViewHolder holder = (CardViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            View view = holder.itemView;
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction(() ->
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start()
            ).start();
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.img_ItemCard_card_image);
            float scale = itemView.getResources().getDisplayMetrics().density;
            cardImage.setCameraDistance(CAMERA_DISTANCE * scale);
        }
    }
}