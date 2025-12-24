package com.example.sagivproject.services;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;

import java.util.List;

public class ForumService {
    public interface ForumCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    public void listenToMessages(
            List<ForumMessage> messageList,
            RecyclerView recycler,
            ForumAdapter adapter,
            ForumCallback<List<ForumMessage>> callback) {

        DatabaseService.getInstance().getForumMessagesRealtime(
                new DatabaseService.DatabaseCallback<List<ForumMessage>>() {
                    @Override
                    public void onCompleted(List<ForumMessage> list) {

                        RecyclerView.LayoutManager lm = recycler.getLayoutManager();
                        boolean wasAtBottom = false;

                        if (lm instanceof LinearLayoutManager) {
                            LinearLayoutManager layoutManager = (LinearLayoutManager) lm;
                            int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                            wasAtBottom = lastVisible == messageList.size() - 1;
                        }

                        messageList.clear();
                        messageList.addAll(list);
                        adapter.notifyDataSetChanged();

                        if (wasAtBottom && !messageList.isEmpty()) {
                            recycler.post(new Runnable() {
                                @Override
                                public void run() {
                                    recycler.scrollToPosition(messageList.size() - 1);
                                }
                            });
                        }

                        callback.onSuccess(list);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        callback.onError(e);
                    }
                }
        );
    }

    public void sendMessage(User user, String text, ForumCallback<Void> callback) {
        String id = DatabaseService.getInstance().generateForumMessageId();

        ForumMessage msg = new ForumMessage(
                id,
                user.getFullName(),
                user.getEmail(),
                text,
                System.currentTimeMillis(),
                user.getUid(),
                user.getIsAdmin()
        );

        DatabaseService.getInstance().sendForumMessage(msg,
                new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void obj) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        callback.onError(e);
                    }
                });
    }

    public void deleteMessage(String messageId, ForumCallback<Void> callback) {
        DatabaseService.getInstance().deleteForumMessage(
                messageId,
                new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void obj) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        callback.onError(e);
                    }
                }
        );
    }
}
