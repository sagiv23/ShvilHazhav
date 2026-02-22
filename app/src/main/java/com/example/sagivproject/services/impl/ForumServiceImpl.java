package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IForumService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * An implementation of the {@link IForumService} interface.
 * <p>
 * This service manages messages within specific forum categories. It handles sending messages,
 * listening for real-time message updates, and deleting messages. It extends {@link BaseDatabaseService}
 * but uses custom paths to manage messages nested under their respective categories.
 * </p>
 */
public class ForumServiceImpl extends BaseDatabaseService<ForumMessage> implements IForumService {
    // The base path is for categories, but messages are stored within each category.
    private static final String FORUM_PATH = "forum_categories";

    /**
     * Constructs a new ForumServiceImpl.
     */
    @Inject
    public ForumServiceImpl() {
        super(FORUM_PATH, ForumMessage.class);
    }

    /**
     * Sends a new message to a forum category.
     *
     * @param user       The user sending the message.
     * @param text       The content of the message.
     * @param categoryId The ID of the category to post the message in.
     * @param callback   The callback to be invoked upon completion.
     */
    @Override
    public void sendMessage(User user, String text, String categoryId, @Nullable DatabaseCallback<Void> callback) {
        DatabaseReference categoryMessagesRef = getCategoryMessagesRef(categoryId);
        String messageId = categoryMessagesRef.push().getKey();

        if (messageId == null) {
            if (callback != null)
                callback.onFailed(new Exception("Failed to generate message ID."));
            return;
        }

        ForumMessage msg = new ForumMessage(messageId, user.getFullName(), user.getEmail(), text, System.currentTimeMillis(), user.getId(), user.isAdmin());

        writeData(getCategoryPath(categoryId) + "/" + messageId, msg, callback);
    }

    /**
     * Listens for real-time updates to the messages in a specific category.
     *
     * @param categoryId The ID of the category to listen to.
     * @param callback   The callback to be invoked with the updated list of messages.
     */
    @Override
    public void listenToMessages(String categoryId, DatabaseCallback<List<ForumMessage>> callback) {
        getCategoryMessagesRef(categoryId).orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ForumMessage> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ForumMessage msg = child.getValue(ForumMessage.class);
                    if (msg != null) {
                        list.add(msg);
                    }
                }
                if (callback != null) callback.onCompleted(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailed(error.toException());
            }
        });
    }

    /**
     * Deletes a specific message from a category.
     *
     * @param messageId  The ID of the message to delete.
     * @param categoryId The ID of the category containing the message.
     * @param callback   The callback to be invoked upon completion.
     */
    @Override
    public void deleteMessage(@NonNull String messageId, String categoryId, @Nullable DatabaseCallback<Void> callback) {
        deleteData(getCategoryPath(categoryId) + "/" + messageId, callback);
    }

    /**
     * Constructs the full database path for the messages of a specific category.
     *
     * @param categoryId The ID of the category.
     * @return The full path to the messages.
     */
    private String getCategoryPath(String categoryId) {
        return FORUM_PATH + "/" + categoryId + "/messages";
    }

    /**
     * Gets a {@link DatabaseReference} to the messages of a specific category.
     *
     * @param categoryId The ID of the category.
     * @return The database reference for the category's messages.
     */
    private DatabaseReference getCategoryMessagesRef(String categoryId) {
        return databaseReference.child(getCategoryPath(categoryId));
    }
}
