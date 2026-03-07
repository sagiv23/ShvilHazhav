package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The main activity for the online memory game.
 */
@AndroidEntryPoint
public class MemoryGameActivity extends BaseActivity implements MemoryGameAdapter.MemoryGameListener {
    private static final long TURN_TIME_LIMIT = 15000; // 15 seconds
    private RecyclerView recyclerCards;
    private boolean endDialogShown = false, localLock = false;
    private String roomId;
    private User user;
    private GameRoom currentRoom;
    private MemoryGameAdapter adapter;
    private TextView tvTimer, tvTurnStatus, tvScore, tvOpponentName;
    private CountDownTimer turnTimer;

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
        user = sharedPreferencesUtil.getUser();

        recyclerCards = findViewById(R.id.recycler_OnlineMemoryGame);
        recyclerCards.setLayoutManager(new GridLayoutManager(this, 3));

        tvTimer = findViewById(R.id.tv_OnlineMemoryGame_timer);
        tvTurnStatus = findViewById(R.id.tv_OnlineMemoryGame_turn_status);
        tvScore = findViewById(R.id.tv_OnlineMemoryGame_score);
        tvOpponentName = findViewById(R.id.tv_OnlineMemoryGame_opponent_name);

        adapter = adapterService.getMemoryGameAdapter();
        adapter.setListener(this);
        recyclerCards.setAdapter(adapter);

        Button btnExit = findViewById(R.id.btn_OnlineMemoryGame_to_exit);
        btnExit.setOnClickListener(v -> showExitGameDialog());

        listenToGame();
    }

    private void showExitGameDialog() {
        Runnable onConfirm = () -> {
            if (currentRoom != null && !"finished".equals(currentRoom.getStatus())) {
                String myUid = user.getId();
                String opponentUid = myUid.equals(currentRoom.getPlayer1Uid()) ?
                        currentRoom.getPlayer2Uid() : currentRoom.getPlayer1Uid();
                databaseService.getGameService().updateRoomField(roomId, "winnerUid", opponentUid);
                databaseService.getGameService().updateRoomField(roomId, "status", "finished");
            }
            Intent intent = new Intent(this, GameHomeScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        };
        dialogService.showConfirmDialog("יציאה מהמשחק", "האם ברצונך לצאת מהמשחק?", "צא", "בטל", onConfirm);
    }

    private void showGameEndDialog(GameRoom room) {
        if (endDialogShown) return;
        endDialogShown = true;
        String winnerUid = room.getWinnerUid();
        String message;
        boolean isWin = user.getId().equals(winnerUid);

        // Update stats once per game locally
        databaseService.getGameService().updateDailyMemoryStats(user.getId(), isWin);

        if ("draw".equals(winnerUid)) {
            message = "זה נגמר בתיקו!";
        } else if (isWin) {
            message = "כל הכבוד! ניצחת והתווסף לך ניצחון!";
        } else {
            message = "הפעם הפסדת... לא נורא!";
        }
        Runnable onExit = () -> {
            Intent intent = new Intent(MemoryGameActivity.this, GameHomeScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        };
        dialogService.showConfirmDialog("המשחק הסתיים", message, onExit);
    }

    private void updateScoreUI(GameRoom room) {
        boolean amIPlayer1 = user.getId().equals(room.getPlayer1Uid());
        int myScore = amIPlayer1 ? room.getPlayer1Score() : room.getPlayer2Score();
        int opponentScore = amIPlayer1 ? room.getPlayer2Score() : room.getPlayer1Score();
        tvScore.setText(MessageFormat.format("אני: {0} | יריב: {1}", myScore, opponentScore));
    }

    private void setupGameBoard(GameRoom room) {
        if ((room.getCards() == null || room.getCards().isEmpty()) && user.getId().equals(room.getPlayer1Uid())) {
            databaseService.getImageService().getAllImages(new DatabaseCallback<>() {
                @Override
                public void onCompleted(List<ImageData> allImages) {
                    if (allImages == null || allImages.size() < 6) {
                        Toast.makeText(MemoryGameActivity.this, "אין מספיק תמונות.", Toast.LENGTH_LONG).show();
                        databaseService.getGameService().cancelRoom(roomId, null);
                        finish();
                        return;
                    }
                    Collections.shuffle(allImages);
                    List<ImageData> selected = allImages.subList(0, 6);
                    List<Card> cards = new ArrayList<>();
                    for (ImageData img : selected) {
                        cards.add(new Card(img.getId(), img.getBase64()));
                        cards.add(new Card(img.getId(), img.getBase64()));
                    }
                    Collections.shuffle(cards);
                    databaseService.getGameService().initGameBoard(roomId, cards, room.getPlayer1Uid(), null);
                }

                @Override
                public void onFailed(Exception e) {
                    databaseService.getGameService().cancelRoom(roomId, null);
                    finish();
                }
            });
        }
    }

    public boolean isMyTurn() {
        return currentRoom != null && user.getId().equals(currentRoom.getCurrentTurnUid()) && !currentRoom.isProcessingMatch();
    }

    @Override
    public void onCardClicked(Card card, int position) {
        if (currentRoom == null || localLock || !isMyTurn()) return;
        if (card.getIsMatched() || card.getIsRevealed()) return;
        handleCardSelection(position);
    }

    private void handleCardSelection(int clickedIndex) {
        Integer firstIndex = currentRoom.getFirstSelectedCardIndex();
        if (firstIndex == null) {
            databaseService.getGameService().updateCardStatus(roomId, clickedIndex, true, false);
            databaseService.getGameService().updateRoomField(roomId, "firstSelectedCardIndex", clickedIndex);
        } else {
            if (firstIndex == clickedIndex) return;
            localLock = true;
            databaseService.getGameService().setProcessing(roomId, true);
            databaseService.getGameService().updateCardStatus(roomId, clickedIndex, true, false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> checkMatch(firstIndex, clickedIndex), 1000);
        }
    }

    private void checkMatch(int idx1, int idx2) {
        List<Card> cards = currentRoom.getCards();
        Card c1 = cards.get(idx1);
        Card c2 = cards.get(idx2);

        if (c1 != null && c2 != null && c1.getId().equals(c2.getId())) {
            adapter.animateSuccess(idx1, recyclerCards);
            adapter.animateSuccess(idx2, recyclerCards);
            databaseService.getGameService().updateCardStatus(roomId, idx1, true, true);
            databaseService.getGameService().updateCardStatus(roomId, idx2, true, true);
            databaseService.getGameService().incrementScore(roomId, user.getId(), null);
        } else {
            adapter.animateError(idx1, recyclerCards);
            adapter.animateError(idx2, recyclerCards);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                databaseService.getGameService().updateCardStatus(roomId, idx1, false, false);
                databaseService.getGameService().updateCardStatus(roomId, idx2, false, false);
                String nextTurn = user.getId().equals(currentRoom.getPlayer1Uid()) ? currentRoom.getPlayer2Uid() : currentRoom.getPlayer1Uid();
                databaseService.getGameService().updateRoomField(roomId, "currentTurnUid", nextTurn);
            }, 600);
        }
        databaseService.getGameService().updateRoomField(roomId, "firstSelectedCardIndex", null);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            databaseService.getGameService().setProcessing(roomId, false);
            localLock = false;
        }, 700);
    }

    private void checkIfGameFinished() {
        boolean allCardsMatched = true;
        for (Card card : currentRoom.getCards()) {
            if (!card.getIsMatched()) {
                allCardsMatched = false;
                break;
            }
        }
        if (allCardsMatched) finishGame(currentRoom);
    }

    private void finishGame(GameRoom room) {
        if ("finished".equals(room.getStatus())) return;
        String winnerUid = calculateWinner(room);
        databaseService.getGameService().updateRoomField(roomId, "winnerUid", winnerUid);
        databaseService.getGameService().updateRoomField(roomId, "status", "finished");
    }

    private void listenToGame() {
        databaseService.getGameService().listenToGame(roomId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(GameRoom room) {
                if (room == null) {
                    finish();
                    return;
                }
                currentRoom = room;
                if (room.getPlayer1Uid() != null && room.getPlayer2Uid() != null) {
                    String opponentUid = user.getId().equals(room.getPlayer1Uid()) ? room.getPlayer2Uid() : room.getPlayer1Uid();
                    databaseService.getUserService().getUser(opponentUid, new DatabaseCallback<>() {
                        @Override
                        public void onCompleted(User opponent) {
                            if (opponent != null)
                                tvOpponentName.setText(String.format("משחק נגד: %s", opponent.getFullName()));
                        }

                        @Override
                        public void onFailed(Exception e) {
                            tvOpponentName.setText("משחק נגד: יריב");
                        }
                    });
                }
                updateScoreUI(room);
                if (room.getCards() == null || room.getCards().isEmpty()) {
                    setupGameBoard(room);
                    return;
                }
                if (room.getCards() != null) adapter.setCards(room.getCards());
                String myUid = user.getId();
                String opponentUid = myUid.equals(room.getPlayer1Uid()) ? room.getPlayer2Uid() : room.getPlayer1Uid();
                databaseService.getGameService().setupForfeitOnDisconnect(roomId, opponentUid);
                if ("finished".equals(room.getStatus())) {
                    if (turnTimer != null) turnTimer.cancel();
                    databaseService.getGameService().removeForfeitOnDisconnect(roomId);
                    showGameEndDialog(room);
                    return;
                }
                checkIfGameFinished();
                boolean isMyTurn = user.getId().equals(room.getCurrentTurnUid());
                if (isMyTurn) {
                    tvTurnStatus.setText("תורך!");
                    tvTurnStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                    startTurnTimer();
                } else {
                    tvTurnStatus.setText("תור היריב...");
                    tvTurnStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                    if (turnTimer != null) turnTimer.cancel();
                    tvTimer.setText("");
                }
            }

            @Override
            public void onFailed(Exception e) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomId != null) databaseService.getGameService().stopListeningToGame(roomId);
    }

    private String calculateWinner(GameRoom room) {
        int p1 = room.getPlayer1Score();
        int p2 = room.getPlayer2Score();
        if (p1 > p2) return room.getPlayer1Uid();
        if (p2 > p1) return room.getPlayer2Uid();
        return "draw";
    }

    private void startTurnTimer() {
        if (turnTimer != null) turnTimer.cancel();
        turnTimer = new CountDownTimer(TURN_TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(MessageFormat.format("זמן נותר: {0}", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (currentRoom.getFirstSelectedCardIndex() != null) {
                    databaseService.getGameService().updateCardStatus(roomId, currentRoom.getFirstSelectedCardIndex(), false, false);
                    databaseService.getGameService().updateRoomField(roomId, "firstSelectedCardIndex", null);
                }
                String opponentUid = user.getId().equals(currentRoom.getPlayer1Uid()) ? currentRoom.getPlayer2Uid() : currentRoom.getPlayer1Uid();
                databaseService.getGameService().updateRoomField(roomId, "currentTurnUid", opponentUid);
            }
        }.start();
    }
}
