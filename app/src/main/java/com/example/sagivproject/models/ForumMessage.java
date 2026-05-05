package com.example.sagivproject.models;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a single message within a forum category.
 * <p>
 * This model follows a "Thin Database" strategy:
 * <ul>
 * <li><b>Persistent fields:</b> Only the message ID, content, timestamp, and sender's user ID are stored.</li>
 * </ul>
 * Sender details (name, email, role) are resolved dynamically by the UI layer from the user database.
 * </p>
 */
public class ForumMessage implements Idable {
    private String id;
    private String message;
    private String timestamp;
    private String userId;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public ForumMessage() {
    }

    /**
     * Constructs a new ForumMessage with persistent data.
     *
     * @param id        The unique ID of the message.
     * @param message   The content of the message.
     * @param timestamp The time the message was sent (ISO format).
     * @param userId    The unique ID of the sender.
     */
    public ForumMessage(String id, String message, String timestamp, String userId) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String messageId) {
        this.id = messageId;
    }

    /**
     * @return The text content of the message.
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The message creation time.
     */
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return The unique identifier of the user who sent the message.
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @NonNull
    @Override
    public String toString() {
        return "ForumMessage{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForumMessage that = (ForumMessage) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(message, that.message) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, timestamp, userId);
    }
}
