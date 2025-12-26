package com.example.sagivproject.services;

import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.BaseActivity;

import java.util.List;

public class ForumService extends BaseActivity {
    public interface ForumCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    public void listenToMessages(ForumCallback<List<ForumMessage>> callback) {
        databaseService.getForumMessagesRealtime(new DatabaseService.DatabaseCallback<List<ForumMessage>>() {
            @Override
            public void onCompleted(List<ForumMessage> list) {
                callback.onSuccess(list);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void sendMessage(User user, String text, ForumCallback<Void> callback) {
        String id = databaseService.generateForumMessageId();
        ForumMessage msg = new ForumMessage(
                id, user.getFullName(), user.getEmail(), text,
                System.currentTimeMillis(), user.getUid(), user.getIsAdmin()
        );

        databaseService.sendForumMessage(msg, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void obj) { callback.onSuccess(null); }
            @Override
            public void onFailed(Exception e) { callback.onError(e); }
        });
    }

    public void deleteMessage(String messageId, ForumCallback<Void> callback) {
        databaseService.deleteForumMessage(messageId, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Void obj) { callback.onSuccess(null); }
            @Override
            public void onFailed(Exception e) { callback.onError(e); }
        });
    }
}