package com.example.sagivproject.screens.models;

public class ForumMessage {
    private String messageId;
    private String fullName;
    private String email;
    private String message;
    private long timestamp;
    private String userId;

    public ForumMessage() {}

    public ForumMessage(String messageId, String fullName, String email, String message, long timestamp, String userId) {
        this.messageId = messageId;
        this.fullName = fullName;
        this.email = email;
        this.message = message;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}