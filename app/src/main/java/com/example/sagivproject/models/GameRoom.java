package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of an online memory game room.
 * <p>
 * This class holds all the information about a single game, including the players,
 * the game board (list of cards), the current turn, scores, and the overall game status.
 * It is used to synchronize the game state between players via the Firebase database.
 * </p>
 */
public class GameRoom implements Serializable, Idable {
    private String id;
    private String status; // Can be "waiting", "playing", or "finished"

    private String player1Uid;
    private String player2Uid;

    private List<Card> cards;
    private String currentTurnUid;

    private int player1Score;
    private int player2Score;

    private Integer firstSelectedCardIndex; // Index of the first card selected in a turn
    private boolean processingMatch; // Flag to prevent clicks during match processing
    private String winnerUid; // UID of the winner, or "draw"

    /**
     * Default constructor required for calls to DataSnapshot.getValue(GameRoom.class).
     */
    public GameRoom() {
    }

    /**
     * Constructs a new waiting GameRoom.
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
        this.cards = new ArrayList<>(); // Initialize cards as an empty list
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlayer1Uid() {
        return player1Uid;
    }

    public void setPlayer1Uid(String player1Uid) {
        this.player1Uid = player1Uid;
    }

    public String getPlayer2Uid() {
        return player2Uid;
    }

    public void setPlayer2Uid(String player2Uid) {
        this.player2Uid = player2Uid;
    }

    public List<Card> getCards() { // Updated return type to List<Card>
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public String getCurrentTurnUid() {
        return currentTurnUid;
    }

    public void setCurrentTurnUid(String currentTurnUid) {
        this.currentTurnUid = currentTurnUid;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public Integer getFirstSelectedCardIndex() {
        return firstSelectedCardIndex;
    }

    public void setFirstSelectedCardIndex(Integer index) {
        this.firstSelectedCardIndex = index;
    }

    public boolean isProcessingMatch() {
        return processingMatch;
    }

    public void setProcessingMatch(boolean processingMatch) {
        this.processingMatch = processingMatch;
    }

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
                ", player1Uid='" + player1Uid + '\'' +
                ", player2Uid='" + player2Uid + '\'' +
                ", cards=" + cards +
                ", currentTurnUid='" + currentTurnUid + '\'' +
                ", player1Score=" + player1Score +
                ", player2Score=" + player2Score +
                ", firstSelectedCardIndex=" + firstSelectedCardIndex +
                ", processingMatch=" + processingMatch +
                ", winnerUid='" + winnerUid + '\'' +
                '}';
    }
}
