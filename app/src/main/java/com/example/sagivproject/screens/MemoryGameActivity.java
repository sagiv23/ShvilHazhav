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

        recyclerCards = findViewById(R.id.recycler_OnlineMemoryGame);
        recyclerCards.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new MemoryGameAdapter(new ArrayList<>(), this);
        recyclerCards.setAdapter(adapter);

        btnExit = findViewById(R.id.btn_OnlineMemoryGame_to_exit);
        btnExit.setOnClickListener(v -> showExitGameDialog());

        listenToGame();
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
            DatabaseService.getInstance().updateRoomField(roomId, "status", "finished");
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
        if (currentRoom == null) return;

        // 1. בדיקה אם זה התור שלי
        if (!user.getUid().equals(currentRoom.getCurrentTurnUid())) return;

        // 2. בדיקה אם המשחק כרגע ב"המתנה" (אנימציית סגירה של זוג לא תואם)
        if (currentRoom.isProcessingMatch()) return;

        // 3. בדיקה אם הקלף כבר פתוח או נמצא
        int cardIndex = adapter.getCards().indexOf(card);
        if (card.getIsMatched() || card.getIsRevealed()) return;

        handleCardSelection(cardIndex);
    }

    private void handleCardSelection(int clickedIndex) {
        List<Card> cards = currentRoom.getCards();
        Integer firstIndex = currentRoom.getFirstSelectedCardIndex();

        if (firstIndex == null) {
            // --- בחירת קלף ראשון ---
            DatabaseService.getInstance().updateCardStatus(roomId, clickedIndex, true, false);
            DatabaseService.getInstance().updateRoomField(roomId, "firstSelectedCardIndex", clickedIndex);
        } else {
            // --- בחירת קלף שני ---
            if (firstIndex == clickedIndex) return; // לחיצה על אותו קלף

            DatabaseService.getInstance().setProcessing(roomId, true); // חסימת לחיצות נוספות
            DatabaseService.getInstance().updateCardStatus(roomId, clickedIndex, true, false);

            // בדיקה אם יש התאמה
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                checkMatch(firstIndex, clickedIndex);
            }, 1000); // השהייה כדי שהשחקן יראה את הקלף השני
        }
    }

    private void checkMatch(int idx1, int idx2) {
        Card c1 = currentRoom.getCards().get(idx1);
        Card c2 = currentRoom.getCards().get(idx2);

        if (c1.getImageResId() == c2.getImageResId()) {
            // --- הצלחה ---
            adapter.animateSuccess(idx1, recyclerCards);
            adapter.animateSuccess(idx2, recyclerCards);

            // כאן אין צורך בהשהייה גדולה כי הקלפים נשארים פתוחים
            DatabaseService.getInstance().updateCardStatus(roomId, idx1, true, true);
            DatabaseService.getInstance().updateCardStatus(roomId, idx2, true, true);

            int newScore = user.getUid().equals(currentRoom.getPlayer1().getUid()) ?
                    currentRoom.getPlayer1Score() + 1 : currentRoom.getPlayer2Score() + 1;
            String scoreField = user.getUid().equals(currentRoom.getPlayer1().getUid()) ? "player1Score" : "player2Score";
            DatabaseService.getInstance().updateRoomField(roomId, scoreField, newScore);

        } else {
            // --- טעות ---
            adapter.animateError(idx1, recyclerCards);
            adapter.animateError(idx2, recyclerCards);

            // חשוב! השהייה של חצי שנייה כדי שהשחקן יראה את הרעד לפני שהקלפים נסגרים ב-DB
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                DatabaseService.getInstance().updateCardStatus(roomId, idx1, false, false);
                DatabaseService.getInstance().updateCardStatus(roomId, idx2, false, false);

                String nextTurn = user.getUid().equals(currentRoom.getPlayer1().getUid())
                        ? currentRoom.getPlayer2().getUid()
                        : currentRoom.getPlayer1().getUid();
                DatabaseService.getInstance().updateRoomField(roomId, "currentTurnUid", nextTurn);
            }, 600);
        }

        DatabaseService.getInstance().updateRoomField(roomId, "firstSelectedCardIndex", null);

        // משחררים את ה-Processing רק אחרי שהעדכונים הסתיימו
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            DatabaseService.getInstance().setProcessing(roomId, false);
        }, 700);
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
