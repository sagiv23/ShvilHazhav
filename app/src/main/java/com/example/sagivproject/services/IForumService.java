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
 * This service handles sending, listening for, and deleting messages within a specific forum category.
 * </p>
 */
public interface IForumService {
    /**
     * Sends a message to a specific forum category.
     *
     * @param user       The user sending the message.
     * @param text       The content of the message.
     * @param categoryId The ID of the category to which the message will be posted.
     * @param callback   A callback to be invoked upon completion.
     */
    void sendMessage(User user, String text, String categoryId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Listens for real-time updates to the messages in a specific forum category.
     *
     * @param categoryId The ID of the category to listen to.
     * @param callback   A callback that will be invoked with the updated list of messages.
     */
    void listenToMessages(String categoryId, DatabaseCallback<List<ForumMessage>> callback);

    /**
     * Deletes a specific message from a forum category.
     *
     * @param messageId  The ID of the message to delete.
     * @param categoryId The ID of the category containing the message.
     * @param callback   A callback to be invoked upon completion.
     */
    void deleteMessage(@NonNull String messageId, String categoryId, @Nullable DatabaseCallback<Void> callback);
}
