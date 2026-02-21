package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Represents a single message within a forum category.
 * <p>
 * This class holds all the data for a forum message, including its content, sender information,
 * and timestamp. This object is used for displaying messages in the UI and for database operations.
 * </p>
 */
public class ForumMessage implements Serializable, Idable {
    private String id;
    private String fullName;
    private String email;
    private String message;
    private long timestamp;
    private String userId;
    private boolean sentByAdmin;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(ForumMessage.class).
     */
    public ForumMessage() {
    }

    /**
     * Constructs a new ForumMessage.
     *
     * @param id          The unique ID of the message.
     * @param fullName    The full name of the sender.
     * @param email       The email of the sender.
     * @param message     The content of the message.
     * @param timestamp   The time the message was sent, in milliseconds.
     * @param userId      The unique ID of the sender.
     * @param sentByAdmin True if the sender is an admin, false otherwise.
     */
    public ForumMessage(String id, String fullName, String email, String message, long timestamp, String userId, boolean sentByAdmin) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.message = message;
        this.timestamp = timestamp;
        this.userId = userId;
        this.sentByAdmin = sentByAdmin;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String messageId) {
        this.id = messageId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSentByAdmin() {
        return sentByAdmin;
    }

    public void setSentByAdmin(boolean sentByAdmin) {
        this.sentByAdmin = sentByAdmin;
    }

    @NonNull
    @Override
    public String toString() {
        return "ForumMessage{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                ", sentByAdmin=" + sentByAdmin +
                '}';
    }
}
