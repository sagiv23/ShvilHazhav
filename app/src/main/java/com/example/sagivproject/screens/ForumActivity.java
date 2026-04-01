package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    /**
     * The RecyclerView displaying the forum messages.
     */
    private RecyclerView recycler;

    /**
     * The input field for typing new messages.
     */
    private EditText edtMessage;

    /**
     * A button that appears to alert the user of new messages below the current scroll position.
     */
    private Button btnNewMessagesIndicator;

    /**
     * The adapter managing the binding of {@link ForumMessage} objects.
     */
    private ForumAdapter adapter;

    /**
     * The ID of the current forum category being displayed.
     */
    private String categoryId;

    /**
     * The currently logged-in user.
     */
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forumPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupMenu();

        user = sharedPreferencesUtil.getUser();

        categoryId = getIntent().getStringExtra("categoryId");
        String categoryName = getIntent().getStringExtra("categoryName");

        findViewById(R.id.btn_forum_back_to_categories).setOnClickListener(v -> finish());

        Button btnSendMessage = findViewById(R.id.btn_forum_send_message);
        btnSendMessage.setOnClickListener(v -> sendMessage());

        initForumViews(R.id.recycler_forum, R.id.edt_forum_new_message, R.id.btn_forum_new_messages_indicator);
        setupForum(categoryId, categoryName);
    }

    /**
     * Initializes the core UI views required for the forum functionality.
     * @param recyclerId Resource ID for the RecyclerView.
     * @param edtId Resource ID for the message EditText.
     * @param btnIndicatorId Resource ID for the new message indicator button.
     */
    private void initForumViews(int recyclerId, int edtId, int btnIndicatorId) {
        this.recycler = findViewById(recyclerId);
        this.edtMessage = findViewById(edtId);
        this.btnNewMessagesIndicator = findViewById(btnIndicatorId);

        if (btnNewMessagesIndicator != null) {
            btnNewMessagesIndicator.setOnClickListener(v -> {
                scrollToBottom(true);
                btnNewMessagesIndicator.setVisibility(View.GONE);
            });
        }
    }

    /**
     * Sets up the forum with the specified category details and initializes the adapter.
     * @param categoryId The unique ID of the forum category.
     * @param categoryName The display name of the forum category.
     */
    private void setupForum(String categoryId, String categoryName) {
        TextView title = findViewById(R.id.txtForumTitle);
        if (title != null) {
            title.setText(categoryName);
        }

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