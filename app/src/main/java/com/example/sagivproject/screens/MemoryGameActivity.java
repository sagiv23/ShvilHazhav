package com.example.sagivproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity managing a live 1-on-1 online memory game session.
 * <p>
 * This class handles the real-time game state, including card reveal logic, matching verification,
 * score tracking, and turn management. It implements the {@link MemoryGameAdapter.MemoryGameListener}
 * to handle card clicks. Key features:
 * <ul>
 * <li>Real-time synchronization with Firebase database.</li>
 * <li>Turn-based countdown timer.</li>
 * <li>Automatic win detection and forfeit handling.</li>
 * <li>Dynamic board initialization with random card pairs.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public class MemoryGameActivity extends BaseActivity implements MemoryGameAdapter.MemoryGameListener {
    /**
     * The time limit for each turn in milliseconds (15 seconds).
     */
    private static final long TURN_TIME_LIMIT = 15000;
    /**
     * The total time limit for the entire game in milliseconds (1.5 minutes).
     */
    private static final long TOTAL_GAME_TIME_LIMIT = 90000;

    private RecyclerView recyclerCards;
    private boolean endDialogShown = false, localLock = false;
    private String roomId;
    private User user;
    private GameRoom currentRoom;
    private MemoryGameAdapter adapter;
    private TextView tvTimer, tvTotalTimer;
    private CountDownTimer turnTimer;
    private CountDownTimer totalGameTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_memory_game, R.id.memoryGamePage);
        setupMenu();

        roomId = getIntent().getStringExtra("roomId");
        user = sharedPreferencesUtil.getUser();

        recyclerCards = findViewById(R.id.recycler_OnlineMemoryGame);
        recyclerCards.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerCards.setItemAnimator(null);
        tvTimer = findViewById(R.id.tv_OnlineMemoryGame_timer);
        tvTotalTimer = findViewById(R.id.tv_OnlineMemoryGame_total_timer);

        adapter = adapterService.getMemoryGameAdapter();
        adapter.setListener(this);
        recyclerCards.setAdapter(adapter);

        findViewById(R.id.btn_OnlineMemoryGame_to_exit).setOnClickListener(v -> showExitGameDialog());

        listenToGame();
    }

    /**
     * Displays a confirmation dialog before allowing the user to exit the game.
     * Exiting an active game results in an automatic loss for the departing player.
     */
    private void showExitGameDialog() {
        Runnable onConfirm = () -> {
            endDialogShown = true;
            if (currentRoom != null && !"finished".equals(currentRoom.getStatus())) {
                String opponentUid = user.getId().equals(currentRoom.getPlayer1Uid()) ? currentRoom.getPlayer2Uid() : currentRoom.getPlayer1Uid();
                databaseService.getGameService().finishGame(roomId, opponentUid, null);
            }
            goBack();
        };
        dialogService.showConfirmDialog(getSupportFragmentManager(), "יציאה מהמשחק", "האם ברצונך לצאת מהמשחק? יציאה תביא להפסד טכני.", "צא", "בטל", onConfirm);
    }

    /**
     * Displays the game conclusion dialog with the final result.
     * Triggers statistical updates via the service if not already processed.
     *
     * @param room The final state of the game session.
     */
    private void showGameEndDialog(GameRoom room) {
        if (endDialogShown) return;
        endDialogShown = true;

        // If the game finished (e.g. via onDisconnect) but stats weren't updated yet,
        // the remaining player triggers the final update for both players.
        if (!room.isStatsUpdated()) {
            String winnerUid = room.getWinnerUid() != null ? room.getWinnerUid() : calculateWinner(room);
            databaseService.getGameService().finishGame(roomId, winnerUid, null);
        }

        String winnerUid = room.getWinnerUid() != null ? room.getWinnerUid() : calculateWinner(room);
        boolean isWin = user.getId().equals(winnerUid);
        boolean isDraw = "draw".equals(winnerUid);

        String message;
        if (isDraw) {
            message = "זה נגמר בתיקו!";
        } else if (isWin) {
            message = "כל הכבוד! ניצחת והתווסף לך ניצחון!";
        } else {
            message = "הפעם הפסדת... לא נורא!";
        }

        dialogService.showConfirmDialog(getSupportFragmentManager(), "המשחק הסתיים", message, "אישור", null, this::goBack);
    }

    /**
     * Updates the scoreboard UI based on the current room state.
     *
     * @param room Current game room data.
     */
    private void updateScoreUI(GameRoom room) {
        boolean amIPlayer1 = user.getId().equals(room.getPlayer1Uid());
        int myScore = amIPlayer1 ? room.getPlayer1Score() : room.getPlayer2Score();
        int opponentScore = amIPlayer1 ? room.getPlayer2Score() : room.getPlayer1Score();
        ((TextView) findViewById(R.id.tv_OnlineMemoryGame_score)).setText(MessageFormat.format("אני: {0} | יריב: {1}", myScore, opponentScore));
    }

    /**
     * Initializes the board by shuffling 6 pairs of random images.
     * Typically executed by the room host (Player 1).
     *
     * @param room The room to configure.
     */
    private void setupGameBoard(GameRoom room) {
        if ((room.getCards() == null || room.getCards().isEmpty()) && user.getId().equals(room.getPlayer1Uid())) {
            databaseService.getImageService().getAllImages(new DatabaseCallback<>() {
                @Override
                public void onCompleted(List<ImageData> allImages) {
                    if (allImages == null || allImages.size() < 6) {
                        Toast.makeText(MemoryGameActivity.this, "אין מספיק תמונות.", Toast.LENGTH_LONG).show();
                        databaseService.getGameService().cancelRoom(roomId, null);
                        goBack();
                        return;
                    }

                    Collections.shuffle(allImages);
                    List<Card> cards = new ArrayList<>();
                    for (ImageData img : allImages.subList(0, 6)) {
                        cards.add(new Card(img.getId(), img.getBase64()));
                        cards.add(new Card(img.getId(), img.getBase64()));
                    }
                    Collections.shuffle(cards);
                    databaseService.getGameService().initGameBoard(roomId, cards, room.getPlayer1Uid(), null);
                }

                @Override
                public void onFailed(Exception e) {
                    databaseService.getGameService().cancelRoom(roomId, null);
                    goBack();
                }
            });
        }
    }

    /**
     * Handler for card clicks. Validates move eligibility before processing selection.
     */
    @Override
    public void onCardClicked(Card card, int position) {
        if (currentRoom == null || localLock || !isMyTurn()) return;
        if (card.getIsMatched() || card.getIsRevealed()) return;
        handleCardSelection(position);
    }

    /**
     * @return true if the local user is authorized to make a move.
     */
    @Override
    public boolean isMyTurn() {
        return currentRoom != null && user.getId().equals(currentRoom.getCurrentTurnUid()) && !currentRoom.isProcessingMatch();
    }

    /**
     * Logic for selecting a card. Synchronizes revealed status with the database.
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
     * Compares two revealed cards and updates matches/turns accordingly.
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

    /**
     * Scans the board to determine if all pairs have been found.
     */
    private void checkIfGameFinished() {
        if (currentRoom.getCards() == null || currentRoom.getCards().isEmpty()) return;

        // Don't check if game is already finished in DB
        if ("finished".equals(currentRoom.getStatus())) return;

        boolean allCardsMatched = true;
        for (Card card : currentRoom.getCards()) {
            if (!card.getIsMatched()) {
                allCardsMatched = false;
                break;
            }
        }
        if (allCardsMatched) finishGame(currentRoom);
    }

    /**
     * Marks the game as finished and identifies the winner based on final scores.
     */
    private void finishGame(GameRoom room) {
        if (room.isStatsUpdated()) return;
        String winnerUid = calculateWinner(room);
        databaseService.getGameService().finishGame(roomId, winnerUid, null);
    }

    /**
     * Establishes a persistent listener for the game room's database node.
     */
    private void listenToGame() {
        databaseService.getGameService().listenToGame(roomId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(GameRoom room) {
                if (room == null) {
                    goBack();
                    return;
                }
                currentRoom = room;
                if (room.getPlayer1Uid() != null && room.getPlayer2Uid() != null) {
                    String opponentUid = user.getId().equals(room.getPlayer1Uid()) ? room.getPlayer2Uid() : room.getPlayer1Uid();
                    databaseService.getUserService().getUser(opponentUid, new DatabaseCallback<>() {
                        @Override
                        public void onCompleted(User opponent) {
                            if (opponent != null)
                                ((TextView) findViewById(R.id.tv_OnlineMemoryGame_opponent_name)).setText(String.format("משחק נגד: %s", opponent.getFullName()));
                        }

                        @Override
                        public void onFailed(Exception e) {
                            ((TextView) findViewById(R.id.tv_OnlineMemoryGame_opponent_name)).setText("משחק נגד: יריב");
                        }
                    });
                }
                updateScoreUI(room);
                if (room.getCards() == null || room.getCards().isEmpty()) {
                    setupGameBoard(room);
                    return;
                }
                adapter.setCards(room.getCards());

                String opponentUid = user.getId().equals(room.getPlayer1Uid()) ? room.getPlayer2Uid() : room.getPlayer1Uid();
                databaseService.getGameService().setupForfeitOnDisconnect(roomId, opponentUid);

                if ("finished".equals(room.getStatus())) {
                    if (turnTimer != null) turnTimer.cancel();
                    if (totalGameTimer != null) totalGameTimer.cancel();
                    databaseService.getGameService().removeForfeitOnDisconnect(roomId);
                    showGameEndDialog(room);
                    return;
                }

                if (totalGameTimer == null) {
                    startTotalGameTimer();
                }

                checkIfGameFinished();

                boolean isMyTurn = user.getId().equals(room.getCurrentTurnUid());
                TextView tvTurnStatus = findViewById(R.id.tv_OnlineMemoryGame_turn_status);
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
                goBack();
            }
        });
    }

    /**
     * Determines the winner UID or returns "draw".
     */
    private String calculateWinner(GameRoom room) {
        int p1 = room.getPlayer1Score();
        int p2 = room.getPlayer2Score();
        if (p1 == p2) return "draw";
        return p1 > p2 ? room.getPlayer1Uid() : room.getPlayer2Uid();
    }

    /**
     * Manages the countdown for the active player's turn.
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
                // If it's my turn, only then I should handle the timer expiration
                if (isMyTurn()) {
                    if (currentRoom.getFirstSelectedCardIndex() != null) {
                        databaseService.getGameService().updateCardStatus(roomId, currentRoom.getFirstSelectedCardIndex(), false, false);
                        databaseService.getGameService().updateRoomField(roomId, "firstSelectedCardIndex", null);
                    }
                    String nextTurn = user.getId().equals(currentRoom.getPlayer1Uid()) ? currentRoom.getPlayer2Uid() : currentRoom.getPlayer1Uid();
                    databaseService.getGameService().updateRoomField(roomId, "currentTurnUid", nextTurn);
                }
            }
        }.start();
    }

    /**
     * Manages the countdown for the total game time.
     */
    private void startTotalGameTimer() {
        if (totalGameTimer != null) totalGameTimer.cancel();
        totalGameTimer = new CountDownTimer(TOTAL_GAME_TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTotalTimer.setText(String.format(Locale.getDefault(), "זמן משחק כולל: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTotalTimer.setText("זמן נגמר!");
                finishGame(currentRoom);
            }
        }.start();
    }

    /**
     * Returns the user to the game matchmaking screen.
     */
    private void goBack() {
        Intent intent = new Intent(this, GameHomeScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (roomId != null) databaseService.getGameService().stopListeningToGame(roomId);
        if (turnTimer != null) turnTimer.cancel();
        if (totalGameTimer != null) totalGameTimer.cancel();
        super.onDestroy();
    }
}