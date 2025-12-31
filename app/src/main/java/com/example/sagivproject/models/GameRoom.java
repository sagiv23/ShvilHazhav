package com.example.sagivproject.models;

import java.util.List;

public class GameRoom {
    private String roomId;
    private String status; // waiting | playing | finished

    private User player1;
    private User player2;

    private List<Card> cards;
    private String currentTurnUid;

    private int player1Score;
    private int player2Score;

    private Integer firstSelectedCardIndex; //אינדקס הקלף הראשון שנבחר
    private boolean processingMatch; //דגל למניעת לחיצות בזמן אנימציית סגירה
    private String winnerUid; //

    public GameRoom() {}

    public GameRoom(String roomId, User player1) {
        this.roomId = roomId;
        this.player1 = player1;
        this.player2 = null;
        this.status = "waiting";
        this.player1Score = 0;
        this.player2Score = 0;
        this.firstSelectedCardIndex = null;
        this.processingMatch = false;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getPlayer1() { return player1; }
    public void setPlayer1(User player1) { this.player1 = player1; }

    public User getPlayer2() { return player2; }
    public void setPlayer2(User player2) { this.player2 = player2; }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    public String getCurrentTurnUid() { return currentTurnUid; }
    public void setCurrentTurnUid(String currentTurnUid) {
        this.currentTurnUid = currentTurnUid;
    }

    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public Integer getFirstSelectedCardIndex() { return firstSelectedCardIndex; }
    public void setFirstSelectedCardIndex(Integer index) { this.firstSelectedCardIndex = index; }
    public boolean isProcessingMatch() { return processingMatch; }
    public void setProcessingMatch(boolean processingMatch) { this.processingMatch = processingMatch; }

    public String getWinnerUid() {return winnerUid;}

    public void setWinnerUid(String winnerUid) {this.winnerUid = winnerUid;}
}