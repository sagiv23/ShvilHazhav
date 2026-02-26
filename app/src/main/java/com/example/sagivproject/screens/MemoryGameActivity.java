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
import com.example.sagivproject.screens.dialogs.ConfirmDialog;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The main activity for the online memory game.
 * <p>
 * This screen manages the entire lifecycle of a memory game between two players.
 * It handles setting up the game board, listening for real-time updates from Firebase,
 * managing player turns, checking for matches, updating scores, and determining the winner.
 * </p>
 */
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

    /**
     * Initializes the activity, sets up the UI components, and starts listening for game updates.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        adapter = new MemoryGameAdapter(this);
        recyclerCards.setAdapter(adapter);

        Button btnExit = findViewById(R.id.btn_OnlineMemoryGame_to_exit);
        btnExit.setOnClickListener(v -> showExitGameDialog());

        listenToGame();
    }

    /**
     * Shows a confirmation dialog for exiting the game. If confirmed, the opponent is declared the winner.
     */
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

        new ConfirmDialog(this, "יציאה מהמשחק", "האם ברצונך לצאת מהמשחק?", "צא", "בטל", onConfirm).show();
    }

    /**
     * Shows a dialog announcing the end of the game with a win, loss, or draw message.
     *
     * @param room The final state of the game room.
     */
    private void showGameEndDialog(GameRoom room) {
        if (endDialogShown) return;
        endDialogShown = true;

        String winnerUid = room.getWinnerUid();
        String message;

        if ("draw".equals(winnerUid)) {
            message = "זה נגמר בתיקו!";
        } else if (user.getId().equals(winnerUid)) {
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

        new ConfirmDialog(this, "המשחק הסתיים", message, onExit).show();
    }

    /**
     * Updates the score display text view.
     *
     * @param room The current game room state.
     */
    private void updateScoreUI(GameRoom room) {
        boolean amIPlayer1 = user.getId().equals(room.getPlayer1Uid());

        int myScore = amIPlayer1 ? room.getPlayer1Score() : room.getPlayer2Score();
        int opponentScore = amIPlayer1 ? room.getPlayer2Score() : room.getPlayer1Score();

        tvScore.setText(MessageFormat.format("אני: {0} | יריב: {1}", myScore, opponentScore));
    }

    /**
     * Sets up the initial game board by fetching random images, creating card pairs,
     * shuffling them, and saving the board state to Firebase. This is only done by Player 1.
     *
     * @param room The game room.
     */
    private void setupGameBoard(GameRoom room) {
        if ((room.getCards() == null || room.getCards().isEmpty()) && user.getId().equals(room.getPlayer1Uid())) {
            databaseService.getImageService().getAllImages(new DatabaseCallback<>() {
                @Override
                public void onCompleted(List<ImageData> allImages) {
                    if (allImages == null || allImages.size() < 6) {
                        Toast.makeText(MemoryGameActivity.this, "אין מספיק תמונות כדי להתחיל את המשחק.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MemoryGameActivity.this, "שגיאה בטעינת תמונות המשחק", Toast.LENGTH_SHORT).show();
                    databaseService.getGameService().cancelRoom(roomId, null);
                    finish();
                }
            });
        }
    }

    /**
     * Checks if it is the current user's turn to play.
     *
     * @return True if it's the user's turn, false otherwise.
     */
    public boolean isMyTurn() {
        return currentRoom != null &&
                user.getId().equals(currentRoom.getCurrentTurnUid()) &&
                !currentRoom.isProcessingMatch();
    }

    /**
     * Handles a click on a card. It allows a card to be selected only if it is the player's turn.
     *
     * @param card     The card that was clicked.
     * @param position The position of the item that was clicked.
     */
    @Override
    public void onCardClicked(Card card, int position) {
        if (currentRoom == null || localLock || !isMyTurn()) return;

        if (card.getIsMatched() || card.getIsRevealed()) return;

        handleCardSelection(position);
    }

    /**
     * Manages the logic for selecting one or two cards.
     *
     * @param clickedIndex The index of the card that was just clicked.
     */
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

    /**
     * Checks if the two selected cards are a match.
     * Updates scores and card states accordingly.
     *
     * @param idx1 The index of the first selected card.
     * @param idx2 The index of the second selected card.
     */
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

                String nextTurn = user.getId().equals(currentRoom.getPlayer1Uid())
                        ? currentRoom.getPlayer2Uid()
                        : currentRoom.getPlayer1Uid();
                databaseService.getGameService().updateRoomField(roomId, "currentTurnUid", nextTurn);
            }, 600);
        }

        databaseService.getGameService().updateRoomField(roomId, "firstSelectedCardIndex", null);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            databaseService.getGameService().setProcessing(roomId, false);
            localLock = false;
        }, 700);
    }

    /**
     * Checks if all cards on the board have been matched. If so, finishes the game.
     */
    private void checkIfGameFinished() {
        boolean allCardsMatched = true;
        for (Card card : currentRoom.getCards()) {
            if (!card.getIsMatched()) {
                allCardsMatched = false;
                break;
            }
        }

        if (allCardsMatched) {
            finishGame(currentRoom);
        }
    }

    /**
     * Finishes the game by calculating the winner and updating the game room status.
     *
     * @param room The final state of the game room.
     */
    private void finishGame(GameRoom room) {
        if ("finished".equals(room.getStatus())) return;

        String winnerUid = calculateWinner(room);
        databaseService.getGameService().updateRoomField(roomId, "winnerUid", winnerUid);
        databaseService.getGameService().updateRoomField(roomId, "status", "finished");
    }

    /**
     * Sets up a real-time listener for the game room to react to changes in the game state.
     */
    private void listenToGame() {
        databaseService.getGameService().listenToGame(roomId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(GameRoom room) {
                if (room == null) {
                    Toast.makeText(MemoryGameActivity.this, "החדר נמחק.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentRoom = room;

                if (room.getPlayer1Uid() != null && room.getPlayer2Uid() != null) {
                    String opponentUid = user.getId().equals(room.getPlayer1Uid()) ? room.getPlayer2Uid() : room.getPlayer1Uid();
                    databaseService.getUserService().getUser(opponentUid, new DatabaseCallback<>() {
                        @Override
                        public void onCompleted(User opponent) {
                            if (opponent != null) {
                                tvOpponentName.setText(String.format("משחק נגד: %s", opponent.getFullName()));
                            }
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

                if (room.getCards() != null) {
                    adapter.setCards(room.getCards());
                }

                String myUid = user.getId();
                String opponentUid = myUid.equals(room.getPlayer1Uid()) ?
                        room.getPlayer2Uid() : room.getPlayer1Uid();
                databaseService.getGameService().setupForfeitOnDisconnect(roomId, opponentUid);

                if ("finished".equals(room.getStatus())) {
                    if (turnTimer != null) turnTimer.cancel();
                    databaseService.getGameService().removeForfeitOnDisconnect(roomId);

                    if (!endDialogShown) {
                        if (myUid.equals(room.getWinnerUid())) {
                            databaseService.getGameService().addUserWin(myUid);
                        }

                        showGameEndDialog(room);
                    }

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
                Toast.makeText(MemoryGameActivity.this, "שגיאה בהאזנה למשחק.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Cleans up resources, particularly the game listener, when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomId != null) {
            databaseService.getGameService().stopListeningToGame(roomId);
        }
    }

    /**
     * Determines the winner of the game based on the final scores.
     *
     * @param room The final game room state.
     * @return The UID of the winning player, or "draw" for a tie.
     */
    private String calculateWinner(GameRoom room) {
        int p1 = room.getPlayer1Score();
        int p2 = room.getPlayer2Score();

        if (p1 > p2) {
            return room.getPlayer1Uid();
        }
        if (p2 > p1) {
            return room.getPlayer2Uid();
        }
        return "draw";
    }

    /**
     * Starts a countdown timer for the current player's turn. If the time runs out,
     * the turn automatically passes to the opponent.
     */
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

                String opponentUid = user.getId().equals(currentRoom.getPlayer1Uid()) ?
                        currentRoom.getPlayer2Uid() : currentRoom.getPlayer1Uid();
                databaseService.getGameService().updateRoomField(roomId, "currentTurnUid", opponentUid);
            }
        }.start();
    }
}
