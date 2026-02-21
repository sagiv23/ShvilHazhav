package com.example.sagivproject.bases;

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
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;
import java.util.Objects;

/**
 * An abstract base class for activities that display a forum message board.
 * <p>
 * This class encapsulates the common logic for a forum screen, including setting up the
 * RecyclerView, sending messages, listening for real-time updates, and managing the scroll
 * position. It provides a structured way to handle forum interactions while allowing
 * subclasses to define specific behaviors, such as message deletion permissions.
 * </p>
 */
public abstract class BaseForumActivity extends BaseActivity {
    protected RecyclerView recycler;
    protected EditText edtMessage;
    protected Button btnNewMessagesIndicator;
    protected ForumAdapter adapter;
    protected ForumPermissions permissions;
    protected String categoryId;
    protected String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Initializes the core UI views required for the forum functionality.
     *
     * @param recycler       The RecyclerView to display messages.
     * @param edtMessage     The EditText for composing new messages.
     * @param btnNewMessages The Button that indicates new incoming messages.
     */
    protected void initForumViews(RecyclerView recycler, EditText edtMessage, Button btnNewMessages) {
        this.recycler = recycler;
        this.edtMessage = edtMessage;
        this.btnNewMessagesIndicator = btnNewMessages;

        if (btnNewMessagesIndicator != null) {
            btnNewMessagesIndicator.setOnClickListener(v -> {
                scrollToBottom(true);
                btnNewMessagesIndicator.setVisibility(View.GONE);
            });
        }
    }

    /**
     * Sets up the forum with the specified category details and initializes the adapter and listeners.
     *
     * @param categoryId   The ID of the forum category.
     * @param categoryName The name of the forum category, to be used as the title.
     */
    protected void setupForum(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;

        TextView title = findViewById(R.id.txtForumTitle);
        if (title != null) {
            title.setText(categoryName);
        }

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ForumAdapter();
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
                        Toast.makeText(BaseForumActivity.this, "ההודעה נמחקה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(BaseForumActivity.this, "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public boolean isShowMenuOptions(ForumMessage message) {
                return permissions.canDelete(message);
            }
        });

        loadMessages();
    }

    /**
     * Loads messages from the database and sets up a real-time listener for new messages.
     */
    protected void loadMessages() {
        databaseService.getForumService().listenToMessages(categoryId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumMessage> list) {
                boolean wasAtBottom = isLastItemVisible();
                int previousItemCount = adapter.getItemCount();

                adapter.submitList(list, () -> {
                    if (wasAtBottom) {
                        scrollToBottom(false);
                    } else if (list.size() > previousItemCount && btnNewMessagesIndicator != null) {
                        btnNewMessagesIndicator.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(BaseForumActivity.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends the message currently in the EditText to the forum.
     */
    protected void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        User user = sharedPreferencesUtil.getUser();

        databaseService.getForumService().sendMessage(Objects.requireNonNull(user), text, categoryId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(Void data) {
                edtMessage.setText("");
                scrollToBottom(true);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(BaseForumActivity.this, "שגיאה בשליחה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks if the last item in the RecyclerView is fully visible.
     *
     * @return True if the last item is visible, false otherwise.
     */
    private boolean isLastItemVisible() {
        LinearLayoutManager lm = (LinearLayoutManager) recycler.getLayoutManager();
        if (lm == null || adapter == null || adapter.getItemCount() == 0) return true;

        int lastVisible = lm.findLastCompletelyVisibleItemPosition();
        return lastVisible >= adapter.getItemCount() - 1;
    }

    /**
     * Scrolls the RecyclerView to the last item.
     *
     * @param smooth True to animate the scroll, false to scroll instantly.
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
     * An interface to delegate permission checks to the concrete Activity.
     */
    public interface ForumPermissions {
        /**
         * Determines if the current user can delete a specific message.
         *
         * @param message The message to check.
         * @return True if deletion is allowed, false otherwise.
         */
        boolean canDelete(ForumMessage message);
    }
}
