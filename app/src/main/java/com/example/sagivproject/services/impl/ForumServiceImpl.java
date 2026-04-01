package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IForumService;
import com.example.sagivproject.services.IUserService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

/**
 * Implementation of the {@link IForumService} interface.
 * <p>
 * This class manages the persistence and retrieval of forum messages within categories
 * in the Firebase Realtime Database. It ensures data minimization by storing only
 * message IDs, text, and timestamps. Sender details (name, email, role) are dynamically
 * injected from the user database during retrieval.
 * </p>
 */
public class ForumServiceImpl extends BaseDatabaseService<ForumMessage> implements IForumService {
    private static final String FORUM_PATH = "forum_categories";
    private final IUserService userService;

    /**
     * Constructs a new ForumServiceImpl.
     * @param userService The service used to fetch sender details.
     */
    @Inject
    public ForumServiceImpl(IUserService userService) {
        super(FORUM_PATH, ForumMessage.class);
        this.userService = userService;
    }

    /**
     * Sends a new message to a specific forum category.
     * <p>
     * Instead of writing the full {@link ForumMessage} object, this method writes a map containing
     * only the persistent fields (id, message, timestamp, userId). This prevents dynamic fields
     * like {@code senderName} from being saved to the database without needing {@code @Exclude} in the model.
     * </p>
     * @param user The user sending the message.
     * @param text The content of the message.
     * @param categoryId The ID of the category to which the message will be posted.
     * @param callback An optional callback to be invoked upon completion.
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

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("id", messageId);
        msgData.put("message", text);
        msgData.put("timestamp", System.currentTimeMillis());
        msgData.put("userId", user.getId());

        writeData(getCategoryPath(categoryId) + "/" + messageId, msgData, callback);
    }

    /**
     * Listens for real-time updates to the messages in a specific forum category.
     * <p>
     * After retrieving the messages from the forum node, this method performs a secondary
     * fetch for each message to retrieve the sender's current details from the user database.
     * The callback is triggered only once all sender details have been successfully mapped,
     * using {@link AtomicInteger} to synchronize the asynchronous calls.
     * </p>
     * @param categoryId The ID of the category to listen to.
     * @param callback A callback that will be invoked with the updated list of messages.
     */
    @Override
    public void listenToMessages(String categoryId, DatabaseCallback<List<ForumMessage>> callback) {
        getCategoryMessagesRef(categoryId).orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ForumMessage> messages = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ForumMessage msg = child.getValue(ForumMessage.class);
                    if (msg != null) {
                        messages.add(msg);
                    }
                }

                if (messages.isEmpty()) {
                    if (callback != null) callback.onCompleted(messages);
                    return;
                }

                AtomicInteger remaining = new AtomicInteger(messages.size());
                for (ForumMessage msg : messages) {
                    userService.getUser(msg.getUserId(), new DatabaseCallback<>() {
                        @Override
                        public void onCompleted(User user) {
                            if (user != null) {
                                msg.setSenderName(user.getFullName());
                                msg.setSenderEmail(user.getEmail());
                                msg.setSenderAdmin(user.isAdmin());
                            }
                            if (remaining.decrementAndGet() == 0) {
                                if (callback != null) callback.onCompleted(messages);
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {
                            if (remaining.decrementAndGet() == 0) {
                                if (callback != null) callback.onCompleted(messages);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { if (callback != null) callback.onFailed(error.toException()); }
        });
    }

    /**
     * Deletes a specific message from a forum category.
     * @param messageId The ID of the message to delete.
     * @param categoryId The ID of the category containing the message.
     * @param callback An optional callback to be invoked upon completion.
     */
    @Override
    public void deleteMessage(@NonNull String messageId, String categoryId, @Nullable DatabaseCallback<Void> callback) { deleteData(getCategoryPath(categoryId) + "/" + messageId, callback); }

    /**
     * Helper to construct the database path for a category's messages.
     * @param categoryId The unique ID of the forum category.
     * @return The full database path string.
     */
    private String getCategoryPath(String categoryId) { return FORUM_PATH + "/" + categoryId + "/messages"; }

    /**
     * Helper to get a DatabaseReference for a category's messages.
     * @param categoryId The unique ID of the forum category.
     * @return A DatabaseReference pointing to the messages sub-node.
     */
    private DatabaseReference getCategoryMessagesRef(String categoryId) { return databaseReference.child(getCategoryPath(categoryId)); }
}