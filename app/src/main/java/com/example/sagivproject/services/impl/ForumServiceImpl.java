package com.example.sagivproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;
import com.example.sagivproject.services.IForumService;
import com.example.sagivproject.services.IUserService;
import com.example.sagivproject.utils.CalendarUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Implementation of the {@link IForumService} interface.
 * <p>
 * This class manages the persistence and retrieval of forum messages within categories
 * in the Firebase Realtime Database. It ensures data minimization by storing only
 * persistent message data (ID, text, timestamp, userId), while sender details (name, email, role)
 * are dynamically injected during retrieval.
 * </p>
 */
public class ForumServiceImpl extends BaseDatabaseService<ForumMessage> implements IForumService {
    private static final String FORUM_PATH = "forum_categories";
    private final IUserService userService;
    private final CalendarUtil calendarUtil;
    private final Map<String, User> userCache = new HashMap<>();
    private final Map<String, ValueEventListener> listeners = new HashMap<>();

    /**
     * Constructs a new ForumServiceImpl.
     *
     * @param firebaseDatabase The {@link FirebaseDatabase} instance.
     * @param userService      The service used to fetch sender details.
     * @param calendarUtil     The utility for date and time operations.
     */
    @Inject
    public ForumServiceImpl(FirebaseDatabase firebaseDatabase, IUserService userService, com.example.sagivproject.utils.CalendarUtil calendarUtil) {
        super(firebaseDatabase, FORUM_PATH, ForumMessage.class);
        this.userService = userService;
        this.calendarUtil = calendarUtil;
    }

    /**
     * Sends a new message to a specific forum category.
     * <p>
     * Uses {@link ServerValue#TIMESTAMP} for server-side time synchronization to ensure
     * messages are ordered correctly across all devices regardless of their local clock.
     * </p>
     *
     * @param user       The {@link User} sending the message.
     * @param text       The message content.
     * @param categoryId The ID of the forum category.
     * @param callback   An optional callback invoked upon completion.
     */
    @Override
    public void sendMessage(User user, String text, String categoryId, @Nullable DatabaseCallback<Void> callback) {
        String path = getCategoryPath(categoryId);
        String messageId = readData(path).push().getKey();

        if (messageId == null) {
            if (callback != null)
                callback.onFailed(new Exception("Failed to generate message ID."));
            return;
        }

        ForumMessage forumMessage = new ForumMessage(messageId, text, calendarUtil.getCurrentTimestamp(), user.getId());
        writeData(path + "/" + messageId, forumMessage, callback);
    }

    /**
     * Listens for real-time updates using a {@link ValueEventListener} on a limited query.
     * <p>
     * This ensures the latest 50 messages are always synchronized as a single batch,
     * which improves UI stability and automatically handles deletions and updates.
     * </p>
     *
     * @param categoryId The ID of the forum category to monitor.
     * @param callback   The callback invoked with the updated list of messages.
     */
    @Override
    public void listenToMessages(String categoryId, DatabaseCallback<List<ForumMessage>> callback) {
        stopListeningToMessages(categoryId);

        ValueEventListener listener = readData(getCategoryPath(categoryId))
                .orderByChild("timestamp")
                .limitToLast(50)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<ForumMessage> messages = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ForumMessage msg = child.getValue(ForumMessage.class);
                            if (msg != null) messages.add(msg);
                        }
                        messages.sort(Comparator.comparing(ForumMessage::getTimestamp));
                        processMessages(messages, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailed(error.toException());
                    }
                });
        listeners.put(categoryId, listener);
    }

    /**
     * Stops listening for updates in a specific forum category.
     *
     * @param categoryId The ID of the category to stop monitoring.
     */
    @Override
    public void stopListeningToMessages(String categoryId) {
        ValueEventListener listener = listeners.remove(categoryId);
        if (listener != null) {
            readData(getCategoryPath(categoryId)).removeEventListener(listener);
        }
    }

    /**
     * Loads older messages for pagination.
     *
     * @param categoryId      The ID of the category.
     * @param oldestTimestamp The timestamp of the oldest message currently loaded.
     * @param limit           Maximum number of messages to fetch.
     * @param callback        The callback with the list of older messages.
     */
    @Override
    public void loadOlderMessages(String categoryId, String oldestTimestamp, int limit, DatabaseCallback<List<ForumMessage>> callback) {
        readData(getCategoryPath(categoryId))
                .orderByChild("timestamp")
                .endBefore(oldestTimestamp)
                .limitToLast(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null) callback.onFailed(task.getException());
                        return;
                    }
                    DataSnapshot snapshot = task.getResult();
                    List<ForumMessage> messages = new ArrayList<>();
                    if (snapshot != null && snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ForumMessage msg = child.getValue(ForumMessage.class);
                            if (msg != null) messages.add(msg);
                        }
                    }
                    messages.sort(Comparator.comparing(ForumMessage::getTimestamp));
                    processMessages(messages, callback);
                });
    }

    /**
     * Injects sender details (name, email, role) into a list of messages.
     * <p>
     * To optimize performance and reduce database hits, this method uses an internal
     * cache to store user data. It uses an {@link AtomicInteger} to track the progress
     * of asynchronous user lookups.
     * </p>
     *
     * @param messages The list of {@link ForumMessage} objects to populate.
     * @param callback The callback to invoke once all messages are processed.
     */
    private void processMessages(List<ForumMessage> messages, DatabaseCallback<List<ForumMessage>> callback) {
        if (messages.isEmpty()) {
            if (callback != null) callback.onCompleted(messages);
            return;
        }

        Set<String> uniqueUserIdsToFetch = messages.stream()
                .map(ForumMessage::getUserId)
                .filter(id -> !userCache.containsKey(id))
                .collect(Collectors.toSet());

        if (uniqueUserIdsToFetch.isEmpty()) {
            if (callback != null) callback.onCompleted(messages);
            return;
        }

        AtomicInteger remaining = new AtomicInteger(uniqueUserIdsToFetch.size());
        for (String userId : uniqueUserIdsToFetch) {
            userService.getUser(userId, new DatabaseCallback<>() {
                @Override
                public void onCompleted(User user) {
                    if (user != null) {
                        userCache.put(userId, user);
                    }
                    checkCompletion();
                }

                @Override
                public void onFailed(Exception e) {
                    checkCompletion();
                }

                private void checkCompletion() {
                    if (remaining.decrementAndGet() == 0 && callback != null) {
                        callback.onCompleted(messages);
                    }
                }
            });
        }
    }

    @Override
    public Map<String, User> getUserCache() {
        return userCache;
    }

    @Override
    public void clearUserCache() {
        userCache.clear();
    }

    /**
     * Deletes a specific message from a forum category.
     *
     * @param messageId  The unique identifier of the message to delete.
     * @param categoryId The ID of the category containing the message.
     * @param callback   An optional callback invoked upon completion.
     */
    @Override
    public void deleteMessage(@NonNull String messageId, String categoryId, @Nullable DatabaseCallback<Void> callback) {
        deleteData(getCategoryPath(categoryId) + "/" + messageId, callback);
    }

    /**
     * Generates the database path for a specific category's message sub-node.
     *
     * @param categoryId The unique ID of the forum category.
     * @return The string representing the database path.
     */
    private String getCategoryPath(String categoryId) {
        return FORUM_PATH + "/" + categoryId + "/messages";
    }
}
