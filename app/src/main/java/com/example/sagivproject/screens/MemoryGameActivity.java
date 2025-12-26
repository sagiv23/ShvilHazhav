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
import com.example.sagivproject.screens.dialogs.ExitGameDialog;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends BaseActivity implements MemoryGameAdapter.MemoryGameListener {
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
        new ExitGameDialog(this, () -> {
            databaseService.updateRoomField(roomId, "status", "finished");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).show();
    }

    @Override
    public void onCardClicked(Card card, View itemView, ImageView imageView) {
        if (currentRoom == null) return;

        //בדיקה אם זה התור שלי
        if (!user.getUid().equals(currentRoom.getCurrentTurnUid())) return;

        //בדיקה אם המשחק כרגע ב"המתנה" (אנימציית סגירה של זוג לא תואם)
        if (currentRoom.isProcessingMatch()) return;

        //בדיקה אם הקלף כבר פתוח או נמצא
        int cardIndex = adapter.getCards().indexOf(card);
        if (card.getIsMatched() || card.getIsRevealed()) return;

        handleCardSelection(cardIndex);
    }

    private void handleCardSelection(int clickedIndex) {
        List<Card> cards = currentRoom.getCards();
        Integer firstIndex = currentRoom.getFirstSelectedCardIndex();

        if (firstIndex == null) {
            // --- בחירת קלף ראשון ---
            databaseService.updateCardStatus(roomId, clickedIndex, true, false);
            databaseService.updateRoomField(roomId, "firstSelectedCardIndex", clickedIndex);
        } else {
            // --- בחירת קלף שני ---
            if (firstIndex == clickedIndex) return; //לחיצה על אותו קלף

            databaseService.setProcessing(roomId, true); //חסימת לחיצות נוספות
            databaseService.updateCardStatus(roomId, clickedIndex, true, false);

            //בדיקה אם יש התאמה
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                checkMatch(firstIndex, clickedIndex);
            }, 1000); //השהייה כדי שהשחקן יראה את הקלף השני
        }
    }

    private void checkMatch(int idx1, int idx2) {
        Card c1 = currentRoom.getCards().get(idx1);
        Card c2 = currentRoom.getCards().get(idx2);

        if (c1.getImageResId() == c2.getImageResId()) {
            // --- הצלחה ---
            adapter.animateSuccess(idx1, recyclerCards);
            adapter.animateSuccess(idx2, recyclerCards);

            databaseService.updateCardStatus(roomId, idx1, true, true);
            databaseService.updateCardStatus(roomId, idx2, true, true);

            int newScore = user.getUid().equals(currentRoom.getPlayer1().getUid()) ?
                    currentRoom.getPlayer1Score() + 1 : currentRoom.getPlayer2Score() + 1;
            String scoreField = user.getUid().equals(currentRoom.getPlayer1().getUid()) ? "player1Score" : "player2Score";
            databaseService.updateRoomField(roomId, scoreField, newScore);

        } else {
            // --- טעות ---
            adapter.animateError(idx1, recyclerCards);
            adapter.animateError(idx2, recyclerCards);

            // חשוב! השהייה של חצי שנייה כדי שהשחקן יראה את הרעד לפני שהקלפים נסגרים ב-DB
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                databaseService.updateCardStatus(roomId, idx1, false, false);
                databaseService.updateCardStatus(roomId, idx2, false, false);

                String nextTurn = user.getUid().equals(currentRoom.getPlayer1().getUid())
                        ? currentRoom.getPlayer2().getUid()
                        : currentRoom.getPlayer1().getUid();
                databaseService.updateRoomField(roomId, "currentTurnUid", nextTurn);
            }, 600);
        }

        databaseService.updateRoomField(roomId, "firstSelectedCardIndex", null);

        // משחררים את ה-Processing רק אחרי שהעדכונים הסתיימו
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            databaseService.setProcessing(roomId, false);
        }, 700);
    }

    private List<Card> createCards() {
        int totalImagesInResources = 60; // כמה תמונות קיימות לך בתיקיית ה-drawable
        int pairsNeeded = 6;            // כמה זוגות צריך (ללוח של 12 קלפים)

        List<Integer> allAvailableResIds = new ArrayList<>();

        // 1. סריקת ה-Drawables ומציאת ה-IDs שלהם לפי השם
        for (int i = 1; i <= totalImagesInResources; i++) {
            String name = "pics_for_game_" + i;
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            if (resId != 0) {
                allAvailableResIds.add(resId);
            }
        }

        // 2. ערבוב כל התמונות ובחירת כמות הזוגות הנדרשת
        Collections.shuffle(allAvailableResIds);
        List<Integer> selectedImages = allAvailableResIds.subList(0, Math.min(pairsNeeded, allAvailableResIds.size()));

        // 3. יצירת רשימת קלפים (כל תמונה מופיעה פעמיים)
        List<Card> cards = new ArrayList<>();
        for (int imgResId : selectedImages) {
            cards.add(new Card(imgResId));
            cards.add(new Card(imgResId));
        }

        // 4. ערבוב סופי של הלוח
        Collections.shuffle(cards);
        return cards;
    }

    private void listenToGame() {
        gameListener = databaseService.listenToGame(roomId, new DatabaseService.DatabaseCallback<GameRoom>() {
            @Override
            public void onCompleted(GameRoom room) {
                if (room == null) return;
                currentRoom = room;

                //רק השחקן הראשון יוצר את הלוח פעם אחת בתחילת המשחק
                if (room.getCards() == null && user.getUid().equals(room.getPlayer1().getUid())) {
                    List<Card> cards = createCards();
                    databaseService.initGameBoard(roomId, cards, room.getPlayer1().getUid(), null);
                    return;
                }

                if (room.getCards() == null) return;

                adapter.getCards().clear();
                adapter.getCards().addAll(room.getCards());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {}
        });
    }
}