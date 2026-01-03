package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.Card;
import com.example.sagivproject.models.GameRoom;
import com.example.sagivproject.models.ImageData;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.dialogs.ExitGameDialog;
import com.example.sagivproject.screens.dialogs.GameEndDialog;
import com.example.sagivproject.services.DatabaseService;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends BaseActivity implements MemoryGameAdapter.MemoryGameListener {
    private RecyclerView recyclerCards;
    private boolean endDialogShown = false;
    private boolean localLock = false;
    private String roomId;
    private User user;
    private GameRoom currentRoom;
    private MemoryGameAdapter adapter;
    private TextView tvTimer, tvTurnStatus, tvScore, tvOpponentName;
    private CountDownTimer turnTimer;
    private static final long TURN_TIME_LIMIT = 15000; //15 שניות
    private boolean isWinRecorded = false; //דגל למניעת כפל ניצחונות

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_memory_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.memoryGamePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        roomId = getIntent().getStringExtra("roomId");
        user = SharedPreferencesUtil.getUser(this);

        recyclerCards = findViewById(R.id.recycler_OnlineMemoryGame);
        recyclerCards.setLayoutManager(new GridLayoutManager(this, 3));

        tvTimer = findViewById(R.id.tv_OnlineMemoryGame_timer);
        tvTurnStatus = findViewById(R.id.tv_OnlineMemoryGame_turn_status);
        tvScore = findViewById(R.id.tv_OnlineMemoryGame_score);
        tvOpponentName = findViewById(R.id.tv_OnlineMemoryGame_opponent_name);

        adapter = new MemoryGameAdapter(new ArrayList<>(), this);
        recyclerCards.setAdapter(adapter);

        Button btnExit = findViewById(R.id.btn_OnlineMemoryGame_to_exit);
        btnExit.setOnClickListener(v -> showExitGameDialog());

        listenToGame();
    }

    private void showExitGameDialog() {
        new ExitGameDialog(this, () -> {
            if (currentRoom != null && !"finished".equals(currentRoom.getStatus())) {
                String myUid = user.getUid();
                String opponentUid = myUid.equals(currentRoom.getPlayer1().getUid()) ?
                        currentRoom.getPlayer2().getUid() : currentRoom.getPlayer1().getUid();

                databaseService.updateRoomField(roomId, "winnerUid", opponentUid);
                databaseService.updateRoomField(roomId, "status", "finished");
            }

            Intent intent = new Intent(this, GameHomeScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).show();
    }

    private void showGameEndDialog(GameRoom room) {
        if (endDialogShown) return;
        endDialogShown = true;

        String winnerUid = room.getWinnerUid();
        String message;

        if ("draw".equals(winnerUid)) {
            message = "זה נגמר בתיקו!";
        } else if (user.getUid().equals(winnerUid)) {
            message = "כל הכבוד! ניצחת והתווסף לך ניצחון!";
        } else {
            message = "הפעם הפסדת... לא נורא!";
        }

        new GameEndDialog(this, message, () -> {
            // מעבר למסך הבית בעת לחיצה על "אישור"
            Intent intent = new Intent(MemoryGameActivity.this, GameHomeScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }).show();
    }

    private void updateScoreUI(GameRoom room) {
        boolean amIPlayer1 = user.getUid().equals(room.getPlayer1().getUid());

        int myScore = amIPlayer1 ? room.getPlayer1Score() : room.getPlayer2Score();
        int opponentScore = amIPlayer1 ? room.getPlayer2Score() : room.getPlayer1Score();

        tvScore.setText("אני: " + myScore + " | יריב: " + opponentScore);
    }

    private void setupGameBoard(GameRoom room) {
        // רק השחקן הראשון יוצר את הלוח
        if (room.getCards() == null && user.getUid().equals(room.getPlayer1().getUid())) {
            databaseService.getAllImages(new DatabaseService.DatabaseCallback<List<ImageData>>() {
                @Override
                public void onCompleted(List<ImageData> allImages) {
                    if (allImages == null || allImages.isEmpty()) return;

                    // בחירת 6 תמונות רנדומליות ליצירת 12 קלפים (זוגות)
                    Collections.shuffle(allImages);
                    List<ImageData> selected = allImages.subList(0, Math.min(6, allImages.size()));

                    List<Card> cards = new ArrayList<>();
                    for (ImageData img : selected) {
                        // יצירת שני קלפים עם אותו ID ותוכן Base64
                        cards.add(new Card(img.getId(), img.getBase64()));
                        cards.add(new Card(img.getId(), img.getBase64()));
                    }
                    Collections.shuffle(cards);

                    // שמירה ל-Firebase
                    databaseService.initGameBoard(roomId, cards, room.getPlayer1().getUid(), null);
                }

                @Override
                public void onFailed(Exception e) { /* טיפול בשגיאה */ }
            });
        }
    }

    public boolean isMyTurn() {
        return currentRoom != null &&
                user.getUid().equals(currentRoom.getCurrentTurnUid()) &&
                !currentRoom.isProcessingMatch();
    }

    @Override
    public void onCardClicked(Card card, View itemView, ImageView imageView) {
        if (currentRoom == null) return;

        if (localLock) return;

        if (!user.getUid().equals(currentRoom.getCurrentTurnUid())) return;

        if (currentRoom.isProcessingMatch()) return;

        int cardIndex = adapter.getCards().indexOf(card);
        if (card.getIsMatched() || card.getIsRevealed()) return;

        handleCardSelection(cardIndex);
    }

    private void handleCardSelection(int clickedIndex) {
        Integer firstIndex = currentRoom.getFirstSelectedCardIndex();

        if (firstIndex == null) {
            // --- בחירת קלף ראשון ---
            databaseService.updateCardStatus(roomId, clickedIndex, true, false);
            databaseService.updateRoomField(roomId, "firstSelectedCardIndex", clickedIndex);
        } else {
            // --- בחירת קלף שני ---
            if (firstIndex == clickedIndex) return; //לחיצה על אותו קלף

            localLock = true;
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

        if (c1.getImageId().equals(c2.getImageId())) {
            // --- הצלחה ---
            adapter.animateSuccess(idx1, recyclerCards);
            adapter.animateSuccess(idx2, recyclerCards);

            databaseService.updateCardStatus(roomId, idx1, true, true);
            databaseService.updateCardStatus(roomId, idx2, true, true);

            String scoreField = user.getUid().equals(currentRoom.getPlayer1().getUid()) ? "player1Score" : "player2Score";
            int newScore = user.getUid().equals(currentRoom.getPlayer1().getUid()) ?
                    currentRoom.getPlayer1Score() + 1 : currentRoom.getPlayer2Score() + 1;

            databaseService.updateRoomField(roomId, scoreField, newScore);

            // עדכון מקומי זמני כדי ש-checkIfGameFinished יראה את הניקוד החדש מיד
            if (user.getUid().equals(currentRoom.getPlayer1().getUid())) {
                currentRoom.setPlayer1Score(newScore);
            } else {
                currentRoom.setPlayer2Score(newScore);
            }

            checkIfGameFinished();
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
            localLock = false;
        }, 700);
    }

    private void checkIfGameFinished() {
        // סך כל הזוגות במשחק הוא 6 (מתוך 12 קלפים)
        int totalPairsFound = currentRoom.getPlayer1Score() + currentRoom.getPlayer2Score();

        // אנחנו בודקים אם הגענו ל-6 זוגות
        if (totalPairsFound >= 6) {
            finishGame(currentRoom);
        }
    }

    private void finishGame(GameRoom room) {
        if ("finished".equals(room.getStatus())) return;

        String winnerUid = calculateWinner(room);

        // עדכון ה-DB שהמשחק נגמר
        databaseService.updateRoomField(roomId, "winnerUid", winnerUid);
        databaseService.updateRoomField(roomId, "status", "finished");
    }

    private void listenToGame() {
        databaseService.listenToGame(roomId, new DatabaseService.DatabaseCallback<GameRoom>() {
            @Override
            public void onCompleted(GameRoom room) {
                if (room == null) return;
                currentRoom = room;

                if (room.getPlayer1() != null && room.getPlayer2() != null) {
                    String opponentName = user.getUid().equals(room.getPlayer1().getUid()) ?
                            room.getPlayer2().getFullName() : room.getPlayer1().getFullName();
                    tvOpponentName.setText("משחק נגד: " + opponentName);
                }

                updateScoreUI(room);

                if (room.getCards() == null || room.getCards().isEmpty()) {
                    setupGameBoard(room);
                    return; // מחכים לעדכון הבא מה-DB שיכיל את הקלפים
                }

                // עדכון ה-Adapter עם הקלפים החדשים מה-DB
                if (room.getCards() != null) {
                    adapter.getCards().clear();
                    adapter.getCards().addAll(room.getCards());
                    adapter.notifyDataSetChanged();
                }

                String myUid = user.getUid();
                String opponentUid = myUid.equals(room.getPlayer1().getUid()) ?
                        room.getPlayer2().getUid() : room.getPlayer1().getUid();
                databaseService.setupForfeitOnDisconnect(roomId, opponentUid);

                // בדיקה האם המשחק הסתיים (כולל עקב ניתוק)
                if ("finished".equals(room.getStatus())) {
                    if (turnTimer != null) turnTimer.cancel();
                    databaseService.removeForfeitOnDisconnect(roomId);

                    // אם אני המנצח ועדיין לא רשמתי את הניצחון ב-DB
                    if (myUid.equals(room.getWinnerUid()) && !isWinRecorded) {
                        isWinRecorded = true; // סימון שביצענו עדכון
                        databaseService.addUserWin(myUid);
                        user.setCountWins(user.getCountWins() + 1);
                        SharedPreferencesUtil.saveUser(MemoryGameActivity.this, user);
                    }

                    showGameEndDialog(room);
                    return;
                }

                // 1. עדכון טקסט התור
                boolean isMyTurn = user.getUid().equals(room.getCurrentTurnUid());
                if (isMyTurn) {
                    tvTurnStatus.setText("תורך!");
                    tvTurnStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    startTurnTimer(); // הפעלת טיימר רק אם זה תורי
                } else {
                    tvTurnStatus.setText("תור היריב...");
                    tvTurnStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    if (turnTimer != null) turnTimer.cancel(); // ביטול טיימר אם התור עבר
                    tvTimer.setText("");
                }
            }

            @Override
            public void onFailed(Exception e) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // קריאה לפונקציית הניתוק דרך השירות בלבד
        if (roomId != null) {
            databaseService.stopListeningToGame(roomId);
        }
    }

    private String calculateWinner(GameRoom room) {
        int p1 = room.getPlayer1Score();
        int p2 = room.getPlayer2Score();

        if (p1 > p2) {
            return room.getPlayer1().getUid();
        }
        if (p2 > p1) {
            return room.getPlayer2().getUid();
        }
        return "draw";
    }

    private void startTurnTimer() {
        if (turnTimer != null) turnTimer.cancel();

        turnTimer = new android.os.CountDownTimer(TURN_TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("זמן נותר: " + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // הזמן נגמר - העברת תור אוטומטית ליריב
                String opponentUid = user.getUid().equals(currentRoom.getPlayer1().getUid()) ?
                        currentRoom.getPlayer2().getUid() : currentRoom.getPlayer1().getUid();
                databaseService.updateRoomField(roomId, "currentTurnUid", opponentUid);
            }
        }.start();
    }
}
