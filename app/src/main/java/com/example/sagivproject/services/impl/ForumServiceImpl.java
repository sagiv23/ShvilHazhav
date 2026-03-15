package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.bases.BaseDatabaseService;
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
 * Implementation of the {@link IForumService} interface.
 * <p>
 * This class manages the persistence and retrieval of forum messages within categories
 * in the Firebase Realtime Database. It provides functionality for sending messages,
 * listening for real-time updates using {@link ValueEventListener}, and deleting messages.
 * </p>
 */
public class ForumServiceImpl extends BaseDatabaseService<ForumMessage> implements IForumService {
    private static final String FORUM_PATH = "forum_categories";

    /**
     * Constructs a new ForumServiceImpl.
     */
    @Inject
    public ForumServiceImpl() {
        super(FORUM_PATH, ForumMessage.class);
    }

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

    @Override
    public void deleteMessage(@NonNull String messageId, String categoryId, @Nullable DatabaseCallback<Void> callback) {
        deleteData(getCategoryPath(categoryId) + "/" + messageId, callback);
    }

    /**
     * Helper to construct the database path for a category's messages.
     */
    private String getCategoryPath(String categoryId) {
        return FORUM_PATH + "/" + categoryId + "/messages";
    }

    /**
     * Helper to get a DatabaseReference for a category's messages.
     */
    private DatabaseReference getCategoryMessagesRef(String categoryId) {
        return databaseReference.child(getCategoryPath(categoryId));
    }
}
