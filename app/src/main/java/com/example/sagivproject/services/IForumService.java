package com.example.sagivproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * An interface that defines the contract for operations related to forum messages.
 * <p>
 * This service handles sending, listening for, and deleting messages within specific forum categories.
 * It implements a data minimization strategy where only core message data is stored in the forum node,
 * while sender details are retrieved dynamically from the user database.
 * </p>
 */
public interface IForumService {
    /**
     * Sends a new message to a specific forum category.
     * <p>
     * To optimize storage, this method ensures only the message text, timestamp, and sender ID
     * are persisted. Full user details are not duplicated in the forum branch.
     * </p>
     * @param user The {@link User} sending the message (used to extract the user ID).
     * @param text The message content string.
     * @param categoryId The unique identifier of the forum category.
     * @param callback An optional callback invoked when the message is successfully saved.
     */
    void sendMessage(User user, String text, String categoryId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Attaches a listener to a forum category to receive real-time message updates.
     * <p>
     * The returned {@link ForumMessage} objects will have their sender details (name, email, role)
     * automatically populated by the service by cross-referencing the user database.
     * </p>
     * @param categoryId The unique identifier of the forum category to monitor.
     * @param callback The callback that will be invoked with the updated list of {@link ForumMessage} objects.
     */
    void listenToMessages(String categoryId, DatabaseCallback<List<ForumMessage>> callback);

    /**
     * Deletes a specific message from a forum category.
     * @param messageId The unique identifier of the message to delete.
     * @param categoryId The ID of the category containing the message.
     * @param callback An optional callback invoked upon completion.
     */
    void deleteMessage(@NonNull String messageId, String categoryId, @Nullable DatabaseCallback<Void> callback);
}