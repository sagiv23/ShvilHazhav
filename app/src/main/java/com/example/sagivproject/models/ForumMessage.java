package com.example.sagivproject.models;

import androidx.annotation.NonNull;

/**
 * Represents a single message within a forum category.
 * <p>
 * This model follows a "Thin Database" strategy:
 * <ul>
 * <li><b>Persistent fields:</b> Only the message ID, content, timestamp, and sender's user ID are stored in the forum node.</li>
 * <li><b>Transient fields:</b> Sender details (name, email, admin status) are populated dynamically by the service layer
 * from the user database during retrieval.</li>
 * </ul>
 * This ensures that if a user changes their name or role, all their past messages reflect the update automatically.
 * </p>
 */
public class ForumMessage implements Idable {
    private String id;
    private String message;
    private String timestamp;
    private String userId;

    /**
     * The full name of the sender, populated at runtime. Not stored in the forum database node.
     */
    private String senderName;

    /**
     * The email of the sender, populated at runtime. Not stored in the forum database node.
     */
    private String senderEmail;

    /**
     * Whether the sender was an admin, populated at runtime. Not stored in the forum database node.
     */
    private boolean senderAdmin;

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

    /**
     * @return The display name of the sender.
     */
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * @return The email address of the sender.
     */
    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    /**
     * @return true if the sender has administrative privileges.
     */
    public boolean isSenderAdmin() {
        return senderAdmin;
    }

    public void setSenderAdmin(boolean senderAdmin) {
        this.senderAdmin = senderAdmin;
    }

    @NonNull
    @Override
    public String toString() {
        return "ForumMessage{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", senderAdmin=" + senderAdmin +
                '}';
    }
}