package com.example.sagivproject.screens;

import android.os.Bundle;
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

import java.util.List;
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
    private RecyclerView recycler;
    private Button btnNewMessagesIndicator;
    private ForumAdapter adapter;
    private String categoryId;
    private User user;

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
        recycler.setAdapter(adapter);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (isLastItemVisible() && btnNewMessagesIndicator != null) {
                    btnNewMessagesIndicator.setVisibility(View.GONE);
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
        });

        loadMessages();
    }

    /**
     * Starts listening for real-time message updates in the current category.
     */
    private void loadMessages() {
        databaseService.getForumService().listenToMessages(categoryId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumMessage> list) {
                boolean wasAtBottom = isLastItemVisible();
                int previousItemCount = adapter.getItemCount();

                adapter.setMessages(list);

                if (previousItemCount == 0 || wasAtBottom) {
                    scrollToBottom(false);
                } else if (list.size() > previousItemCount && btnNewMessagesIndicator != null) {
                    btnNewMessagesIndicator.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ForumActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter.onDestroy();
        }
        super.onDestroy();
    }
}