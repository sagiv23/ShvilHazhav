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
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.PickAPic;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity implements MemoryGameAdapter.MemoryGameListener {
    private RecyclerView recyclerCards;
    private Button btnExit;

    private String roomId;
    private User user;
    private ValueEventListener gameListener;
    private GameRoom currentRoom;

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

        roomId = getIntent().getStringExtra("roomId");
        user = SharedPreferencesUtil.getUser(this);

        listenToGame();

        recyclerCards = findViewById(R.id.recycler_OnlineMemoryGame);
        recyclerCards.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new MemoryGameAdapter(new ArrayList<>(), this);
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
        /*
        //לא התור שלך
        if (!user.getUid().equals(currentRoom.getCurrentTurnUid())) return;

        //קלף כבר פתוח / מותאם
        if (card.isMatched() || card.isRevealed()) return;

        DatabaseService.getInstance().selectCard(roomId, user.getUid(), card);
         */
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

    private void listenToGame() {
        gameListener = DatabaseService.getInstance()
                .listenToGame(roomId, new DatabaseService.DatabaseCallback<GameRoom>() {
                    @Override
                    public void onCompleted(GameRoom room) {
                        if (room == null) return;
                        currentRoom = room;

                        // init board – רק player1
                        if (room.getCards() == null &&
                                user.getUid().equals(room.getPlayer1().getUid())) {

                            List<Card> cards = createCards();
                            DatabaseService.getInstance()
                                    .initGameBoard(roomId, cards,
                                            room.getPlayer1().getUid(), null);
                            return;
                        }

                        if (room.getCards() == null) return;

                        adapter.getCards().clear();
                        adapter.getCards().addAll(room.getCards());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailed(Exception e) {
                    }
                });
    }
}
