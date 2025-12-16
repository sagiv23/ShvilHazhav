package com.example.sagivproject.screens;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.MemoryGameAdapter;
import com.example.sagivproject.models.Card;
import com.example.sagivproject.utils.PickAPic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity implements MemoryGameAdapter.MemoryGameListener {
    private RecyclerView recyclerCards;
    private Button btnExit;

    private Card firstSelected = null;
    private View firstSelectedView = null;
    private ImageView firstSelectedImage = null;
    private boolean isBusy = false;

    private MemoryGameAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online_memory_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onlineMemoryGamePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerCards = findViewById(R.id.recycler_OnlineMemoryGame);
        recyclerCards.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new MemoryGameAdapter(createCards(), this);
        recyclerCards.setAdapter(adapter);

        btnExit = findViewById(R.id.btn_OnlineMemoryGame_to_exit);
        btnExit.setOnClickListener(v -> showExitGameDialog());
    }

    private void showExitGameDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_exit);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        TextView txtTitle = dialog.findViewById(R.id.txt_DialogExit_title), txtMessage = dialog.findViewById(R.id.txt_DialogExit_message);
        Button btnConfirm = dialog.findViewById(R.id.btn_DialogExit_confirm), btnCancel = dialog.findViewById(R.id.btn_DialogExit_cancel);

        txtTitle.setText("יציאה מהמשחק");
        txtMessage.setText("האם ברצונך לצאת מהמשחק?");

        btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(MemoryGameActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onCardClicked(Card card, View itemView, ImageView imageView) {
        if (isBusy || card.isMatched() || card.isRevealed()) return;

        card.setRevealed(true);
        flipCard(imageView, card.getImageResId());

        if (firstSelected == null) {
            firstSelected = card;
            firstSelectedView = itemView;
            firstSelectedImage = imageView;
            return;
        }

        isBusy = true;

        if (firstSelected.getImageResId() == card.getImageResId()) {
            firstSelected.setMatched(true);
            card.setMatched(true);

            animateCorrectMatch(firstSelectedView);
            animateCorrectMatch(itemView);

            new Handler(Looper.getMainLooper()).postDelayed(this::resetTurn, 350);
        } else {
            animateWrongMatch(firstSelectedView);
            animateWrongMatch(itemView);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                flipCard(firstSelectedImage, R.drawable.fold_card_img);
                flipCard(imageView, R.drawable.fold_card_img);

                firstSelected.setRevealed(false);
                card.setRevealed(false);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    resetTurn();
                    adapter.notifyDataSetChanged();
                }, 300);
            }, 600);
        }
    }

    private void resetTurn() {
        firstSelected = null;
        firstSelectedView = null;
        firstSelectedImage = null;
        isBusy = false;
    }

    private void flipCard(ImageView imageView, int newImageRes) {
        imageView.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {
                    imageView.setImageResource(newImageRes);
                    imageView.setRotationY(-90f);
                    imageView.animate()
                            .rotationY(0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void animateWrongMatch(View view) {
        view.animate()
                .translationX(20)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(-20)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(10)
                                .setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(0)
                                        .setDuration(50)
                                        .start())
                                .start())
                        .start())
                .start();
    }

    private void animateCorrectMatch(View view) {
        view.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .alpha(0.85f)
                .setDuration(150)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private List<Card> createCards() {
        PickAPic pickAPic = new PickAPic(this, 60);

        List<Integer> images = pickAPic.getUniqueImages(6);
        List<Card> cards = new ArrayList<>();

        for (int img : images) {
            cards.add(new Card(img));
            cards.add(new Card(img));
        }

        Collections.shuffle(cards);
        return cards;
    }
}
