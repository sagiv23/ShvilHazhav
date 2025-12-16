package com.example.sagivproject.models;

public class GameRoom {

    private String roomId;
    private String status; // waiting | playing | finished
    private User player1;
    private User player2;

    public GameRoom() {}

    public GameRoom(String roomId, User player1) {
        this.roomId = roomId;
        this.player1 = player1;
        this.player2 = null;
        this.status = "waiting";
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getPlayer1() { return player1; }
    public void setPlayer1(User player1) { this.player1 = player1; }

    public User getPlayer2() { return player2; }
    public void setPlayer2(User player2) { this.player2 = player2; }
}
