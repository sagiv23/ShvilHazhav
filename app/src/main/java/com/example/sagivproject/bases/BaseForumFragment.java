package com.example.sagivproject.bases;

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
 * An abstract base class for fragments that display a forum message board.
 * <p>
 * This class provides common logic for handling forum messages, including:
 * <ul>
 *     <li>Initializing RecyclerView and adapters.</li>
 *     <li>Listening for real-time message updates.</li>
 *     <li>Sending new messages.</li>
 *     <li>Deleting messages based on permissions.</li>
 *     <li>Auto-scrolling to the bottom when new messages arrive.</li>
 * </ul>
 * </p>
 */
public abstract class BaseForumFragment extends BaseFragment {
    protected RecyclerView recycler;
    protected EditText edtMessage;
    protected Button btnNewMessagesIndicator;
    protected ForumAdapter adapter;
    protected String categoryId;
    protected ForumPermissions permissions;

    /**
     * Initializes the core UI views required for the forum functionality.
     *
     * @param view           The root view of the fragment.
     * @param recyclerId     The resource ID of the RecyclerView.
     * @param edtId          The resource ID of the message input EditText.
     * @param btnIndicatorId The resource ID of the "new messages" scroll indicator button.
     */
    protected void initForumViews(View view, int recyclerId, int edtId, int btnIndicatorId) {
        this.recycler = view.findViewById(recyclerId);
        this.edtMessage = view.findViewById(edtId);
        this.btnNewMessagesIndicator = view.findViewById(btnIndicatorId);

        if (btnNewMessagesIndicator != null) {
            btnNewMessagesIndicator.setOnClickListener(v -> {
                scrollToBottom(true);
                btnNewMessagesIndicator.setVisibility(View.GONE);
            });
        }
    }

    /**
     * Sets up the forum with the specified category details and initializes the adapter.
     *
     * @param view         The root view.
     * @param categoryId   The ID of the forum category to display.
     * @param categoryName The display name of the category.
     */
    protected void setupForum(View view, String categoryId, String categoryName) {
        this.categoryId = categoryId;

        TextView title = view.findViewById(R.id.txtForumTitle);
        if (title != null) {
            title.setText(categoryName);
        }

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
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
                        if (getContext() != null)
                            Toast.makeText(getContext(), "ההודעה נמחקה", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public boolean isShowMenuOptions(ForumMessage message) {
                return permissions != null && permissions.canDelete(message);
            }
        });

        loadMessages();
    }

    /**
     * Starts listening for messages in the current category and updates the adapter.
     */
    protected void loadMessages() {
        databaseService.getForumService().listenToMessages(categoryId, new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<ForumMessage> list) {
                boolean wasAtBottom = isLastItemVisible();
                int previousItemCount = adapter.getItemCount();

                adapter.setMessages(list);

                if (wasAtBottom) {
                    scrollToBottom(false);
                } else if (list.size() > previousItemCount && btnNewMessagesIndicator != null) {
                    btnNewMessagesIndicator.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends the text currently in the EditText as a new message.
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
                if (getContext() != null)
                    Toast.makeText(getContext(), "שגיאה בשליחה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks if the last item in the list is currently visible to the user.
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
     * @param smooth If true, performs a smooth scroll; otherwise, snaps to bottom.
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
    public void onDestroy() {
        if (adapter != null) {
            adapter.onDestroy();
        }
        super.onDestroy();
    }

    /**
     * An interface to delegate permission checks to the concrete Fragment.
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
