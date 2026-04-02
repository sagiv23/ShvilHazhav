package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of an online 1-on-1 memory game session.
 * <p>
 * This class serves as the shared state between two players, synchronized in real-time
 * via Firebase. It tracks player UIDs, the game board (list of cards), current turn,
 * scores, and the overall status of the match (waiting, playing, finished).
 * </p>
 */
public class GameRoom implements Serializable, Idable {
    private String id;
    /**
     * Current status of the room: "waiting", "playing", or "finished".
     */
    private String status;

    private String player1Uid;
    private String player2Uid;

    /**
     * The list of cards representing the shuffled game board.
     */
    private List<Card> cards;

    /**
     * The unique identifier of the player who has the current turn.
     */
    private String currentTurnUid;

    private int player1Score;
    private int player2Score;

    /**
     * Index of the first card selected in a turn, used for match validation.
     */
    private Integer firstSelectedCardIndex;

    /**
     * Flag to prevent card clicks while a match is being animated or processed.
     */
    private boolean processingMatch;

    /**
     * The UID of the winning player, or "draw" if scores are tied.
     */
    private String winnerUid;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public GameRoom() {
    }

    /**
     * Constructs a new waiting GameRoom initialized with player 1.
     *
     * @param id      The unique ID of the room.
     * @param player1 The user who created the room.
     */
    public GameRoom(String id, User player1) {
        this.id = id;
        this.player1Uid = player1.getId();
        this.player2Uid = null;
        this.status = "waiting";
        this.player1Score = 0;
        this.player2Score = 0;
        this.firstSelectedCardIndex = null;
        this.processingMatch = false;
        this.cards = new ArrayList<>();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The room's status ("waiting", "playing", "finished").
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The unique ID of the room host (Player 1).
     */
    public String getPlayer1Uid() {
        return player1Uid;
    }

    public void setPlayer1Uid(String player1Uid) {
        this.player1Uid = player1Uid;
    }

    /**
     * @return The unique ID of the joined opponent (Player 2).
     */
    public String getPlayer2Uid() {
        return player2Uid;
    }

    public void setPlayer2Uid(String player2Uid) {
        this.player2Uid = player2Uid;
    }

    /**
     * @return The current list of cards on the game board.
     */
    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    /**
     * @return The UID of the player whose turn it is.
     */
    public String getCurrentTurnUid() {
        return currentTurnUid;
    }

    public void setCurrentTurnUid(String currentTurnUid) {
        this.currentTurnUid = currentTurnUid;
    }

    /**
     * @return The current score of Player 1.
     */
    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    /**
     * @return The current score of Player 2.
     */
    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    /**
     * @return The index of the first revealed card in the current turn.
     */
    public Integer getFirstSelectedCardIndex() {
        return firstSelectedCardIndex;
    }

    public void setFirstSelectedCardIndex(Integer index) {
        this.firstSelectedCardIndex = index;
    }

    /**
     * @return true if the game is currently processing a match check.
     */
    public boolean isProcessingMatch() {
        return processingMatch;
    }

    public void setProcessingMatch(boolean processingMatch) {
        this.processingMatch = processingMatch;
    }

    /**
     * @return The UID of the winner or "draw".
     */
    public String getWinnerUid() {
        return winnerUid;
    }

    public void setWinnerUid(String winnerUid) {
        this.winnerUid = winnerUid;
    }

    @NonNull
    @Override
    public String toString() {
        return "GameRoom{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", player1='" + player1Uid + '\'' +
                ", player2='" + player2Uid + '\'' +
                '}';
    }
}