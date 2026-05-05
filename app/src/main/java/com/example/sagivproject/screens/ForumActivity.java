package com.example.sagivproject.screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.bases.BaseActivity;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that displays and manages messages within a specific forum category.
 * <p>
 * This activity handles real-time message synchronization, sending new messages,
 * and role-based message deletion. It manages its own {@link RecyclerView} and {@link ForumAdapter}.
 * Following the project's data optimization policy, it relies on the service layer to provide
 * fully populated {@link ForumMessage} objects with sender details retrieved from the user database.
 * </p>
 */
@AndroidEntryPoint
public class ForumActivity extends BaseActivity {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private RecyclerView recycler;
    private Button btnNewMessagesIndicator;
    private ForumAdapter adapter;
    private String categoryId;
    private User user;
    private TextToSpeech tts;
    private String currentlySpeakingMsgId = null;
    private boolean isLoadingOlder = false;
    private boolean hasMoreOlder = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_forum, R.id.forumPage);
        setupMenu();

        user = sharedPreferencesUtil.getUser();

        categoryId = getIntent().getStringExtra("categoryId");
        String categoryName = getIntent().getStringExtra("categoryName");

        btnNewMessagesIndicator = findViewById(R.id.btn_forum_new_messages_indicator);
        recycler = findViewById(R.id.recycler_forum);
        TextView title = findViewById(R.id.txtForumTitle);

        findViewById(R.id.btn_forum_back_to_categories).setOnClickListener(v -> finish());
        findViewById(R.id.btn_forum_send_message).setOnClickListener(v -> sendMessage());
        btnNewMessagesIndicator.setOnClickListener(v -> {
            scrollToBottom(true);
            btnNewMessagesIndicator.setVisibility(View.GONE);
        });
        title.setText(categoryName);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(layoutManager);

        adapter = adapterService.getForumAdapter();
        adapter.setCurrentUserId(user.getId());
        recycler.setAdapter(adapter);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (isLastItemVisible() && btnNewMessagesIndicator != null) {
                    btnNewMessagesIndicator.setVisibility(View.GONE);
                }

                // Pagination: detect scroll to top
                LinearLayoutManager lm = (LinearLayoutManager) recycler.getLayoutManager();
                if (lm != null && lm.findFirstCompletelyVisibleItemPosition() == 0 && !isLoadingOlder && hasMoreOlder) {
                    loadMoreMessages();
                }
            }
        });

        adapter.setForumMessageListener(new ForumAdapter.ForumMessageListener() {
            @Override
            public void onClick(ForumMessage message) {
                databaseService.getForumService().deleteMessage(message.getId(), categoryId, new DatabaseCallback<>() {
                    @Override
                    public void onCompleted(Void data) {
                        adapter.removeMessage(message);
                        Toast.makeText(ForumActivity.this, "ההודעה נמחקה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(ForumActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public boolean isShowMenuOptions(ForumMessage message) {
                return canDelete(message);
            }

            @Override
            public void onSpeakClicked(ForumMessage message) {
                if (message.getId().equals(currentlySpeakingMsgId)) {
                    if (tts != null) tts.stop();
                    currentlySpeakingMsgId = null;
                    notifyItemChangedById(message.getId());
                } else {
                    String oldId = currentlySpeakingMsgId;
                    if (oldId != null) {
                        if (tts != null) tts.stop();
                        currentlySpeakingMsgId = null;
                        notifyItemChangedById(oldId);
                    }

                    if (tts == null) {
                        initTTS(message);
                    } else {
                        speak(message);
                    }
                }
            }

            @Override
            public String getCurrentlySpeakingMsgId() {
                return currentlySpeakingMsgId;
            }
        });
    }

    /**
     * Loads older messages for pagination.
     * Fetches a slice of history preceding the current oldest message.
     */
    private void loadMoreMessages() {
        List<ForumMessage> currentMessages = adapter.getItemList();
        if (currentMessages.isEmpty()) return;

        isLoadingOlder = true;
        String oldestTimestamp = currentMessages.get(0).getTimestamp();

        databaseService.getForumService().loadOlderMessages(categoryId, oldestTimestamp, 20, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumMessage> list) {
                if (list.isEmpty()) {
                    hasMoreOlder = false;
                } else {
                    adapter.setSenderMap(databaseService.getForumService().getUserCache());
                    adapter.addOlderMessages(list);
                }
                isLoadingOlder = false;
            }

            @Override
            public void onFailed(Exception e) {
                isLoadingOlder = false;
                Toast.makeText(ForumActivity.this, "שגיאה בטעינת הודעות ישנות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Starts listening for real-time message updates in the current category.
     * <p>
     * Implements a "Smart Merge" strategy:
     * 1. Detects deletions by identifying missing messages within the latest synchronized window.
     * 2. Updates existing messages if their content or sender details have changed.
     * 3. Adds new messages and triggers the "New Message" indicator if the user is scrolled up.
     * </p>
     */
    private void loadMessages() {
        databaseService.getForumService().listenToMessages(categoryId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumMessage> latestMessages) {
                if (latestMessages == null) return;

                TextView txtNoMessages = findViewById(R.id.txt_no_messages);
                if (txtNoMessages != null) {
                    txtNoMessages.setVisibility(latestMessages.isEmpty() && adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                }

                boolean wasAtBottom = isLastItemVisible();
                List<ForumMessage> currentList = mergeMessages(adapter.getItemList(), latestMessages, wasAtBottom);

                adapter.setSenderMap(databaseService.getForumService().getUserCache());
                adapter.setData(currentList);

                if (wasAtBottom) {
                    scrollToBottom(false);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumActivity.this, "שגיאה בסנכרון הודעות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends the text currently in the EditText as a new message.
     */
    private void sendMessage() {
        EditText edtMessage = findViewById(R.id.edt_forum_new_message);
        String text = edtMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        User currentUser = sharedPreferencesUtil.getUser();

        databaseService.getForumService().sendMessage(Objects.requireNonNull(currentUser), text, categoryId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                edtMessage.setText("");
                scrollToBottom(true);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumActivity.this, "שגיאה בשליחה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Determines if the current user has the authority to delete a specific message.
     *
     * @param message The {@link ForumMessage} to check permissions for.
     * @return true if the current user is an admin or the author of the message.
     */
    private boolean canDelete(ForumMessage message) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        return message.getUserId() != null && message.getUserId().equals(user.getId());
    }

    /**
     * Checks if the last item in the RecyclerView is currently visible to the user.
     *
     * @return true if the last item is visible, false otherwise.
     */
    private boolean isLastItemVisible() {
        LinearLayoutManager lm = (LinearLayoutManager) recycler.getLayoutManager();
        if (lm == null || adapter == null || adapter.getItemCount() == 0) return true;

        int lastVisible = lm.findLastCompletelyVisibleItemPosition();
        return lastVisible >= adapter.getItemCount() - 1;
    }

    /**
     * Scrolls the RecyclerView to the bottom.
     *
     * @param smooth true to use a smooth animation, false for an immediate jump.
     */
    private void scrollToBottom(boolean smooth) {
        if (adapter != null && adapter.getItemCount() > 0) {
            if (smooth) {
                recycler.smoothScrollToPosition(adapter.getItemCount() - 1);
            } else {
                recycler.post(() -> recycler.scrollToPosition(adapter.getItemCount() - 1));
            }
        }
    }

    /**
     * Initializes the Text-to-Speech engine and begins speaking the provided message.
     *
     * @param msg The {@link ForumMessage} to read aloud after initialization.
     */
    private void initTTS(ForumMessage msg) {
        if (tts == null) {
            tts = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("he", "IL"));
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            currentlySpeakingMsgId = utteranceId;
                            mainHandler.post(() -> notifyItemChangedById(utteranceId));
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if (utteranceId.equals(currentlySpeakingMsgId)) {
                                currentlySpeakingMsgId = null;
                            }
                            mainHandler.post(() -> notifyItemChangedById(utteranceId));
                        }

                        @Override
                        public void onError(String utteranceId) {
                            if (utteranceId.equals(currentlySpeakingMsgId)) {
                                currentlySpeakingMsgId = null;
                            }
                            mainHandler.post(() -> notifyItemChangedById(utteranceId));
                        }
                    });
                    speak(msg);
                }
            });
        }
    }

    /**
     * Triggers the TTS engine to speak the text of the given message.
     *
     * @param msg The message to speak.
     */
    private void speak(ForumMessage msg) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, msg.getId());
            tts.speak(msg.getMessage(), TextToSpeech.QUEUE_FLUSH, params, msg.getId());
        }
    }

    /**
     * Finds a message by its ID and triggers a partial UI refresh for that item.
     *
     * @param msgId The unique ID of the message.
     */
    private void notifyItemChangedById(String msgId) {
        List<ForumMessage> messages = adapter.getItemList();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(msgId)) {
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * Merges a batch of synchronized messages into the existing message list.
     * <p>
     * This method applies a deletion detection strategy and updates existing messages
     * or adds new ones, while maintaining the overall chronological order.
     * </p>
     *
     * @param currentList    The current list of messages in the adapter.
     * @param latestMessages The new batch of messages from the database.
     * @param wasAtBottom    Whether the user was at the bottom of the list before the merge.
     * @return A new, sorted list of {@link ForumMessage} objects.
     */
    private List<ForumMessage> mergeMessages(List<ForumMessage> currentList, List<ForumMessage> latestMessages, boolean wasAtBottom) {
        List<ForumMessage> result = new ArrayList<>(currentList);

        if (latestMessages == null || latestMessages.isEmpty()) return result;

        // 1. Handle Deletions
        String newestTs = latestMessages.get(latestMessages.size() - 1).getTimestamp();
        String oldestInWindowTs = latestMessages.get(0).getTimestamp();

        result.removeIf(m ->
                m.getTimestamp().compareTo(oldestInWindowTs) >= 0 &&
                        m.getTimestamp().compareTo(newestTs) <= 0 &&
                        latestMessages.stream().noneMatch(lm -> lm.getId().equals(m.getId()))
        );

        // 2. Handle Adds and Updates
        for (ForumMessage newMsg : latestMessages) {
            int existingIndex = -1;
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getId().equals(newMsg.getId())) {
                    existingIndex = i;
                    break;
                }
            }

            if (existingIndex != -1) {
                if (!result.get(existingIndex).equals(newMsg)) {
                    result.set(existingIndex, newMsg);
                }
            } else {
                result.add(newMsg);
                if (!wasAtBottom && btnNewMessagesIndicator != null) {
                    btnNewMessagesIndicator.setVisibility(View.VISIBLE);
                }
            }
        }

        result.sort(Comparator.comparing(ForumMessage::getTimestamp));
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseService.getForumService().clearUserCache();
        loadMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseService.getForumService().stopListeningToMessages(categoryId);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
