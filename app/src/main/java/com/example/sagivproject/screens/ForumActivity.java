package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseForumActivity;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An activity that displays the messages within a specific forum category.
 * <p>
 * This screen allows users to view messages, send new messages, and delete their own messages.
 * It extends {@link BaseForumActivity} to handle common forum functionalities.
 * </p>
 */
@AndroidEntryPoint
public class ForumActivity extends BaseForumActivity implements BaseForumActivity.ForumPermissions {
    private User user;

    /**
     * Initializes the activity, sets up the UI, and configures the forum for the given category.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        user = sharedPreferencesUtil.getUser();

        String categoryId = getIntent().getStringExtra("categoryId");
        String categoryName = getIntent().getStringExtra("categoryName");

        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);
        setupTopMenu(topMenuContainer);

        Button btnBackToCategories = findViewById(R.id.btn_forum_back_to_categories);
        Button btnSendMessage = findViewById(R.id.btn_forum_send_message);
        EditText edtNewMessage = findViewById(R.id.edt_forum_new_message);
        Button btnNewMessages = findViewById(R.id.btn_forum_new_messages_indicator);
        RecyclerView recyclerForum = findViewById(R.id.recycler_forum);

        btnBackToCategories.setOnClickListener(v -> finish());
        btnSendMessage.setOnClickListener(v -> sendMessage());

        initForumViews(recyclerForum, edtNewMessage, btnNewMessages);
        this.permissions = this;
        setupForum(categoryId, categoryName);
    }

    /**
     * Determines if the current user has permission to delete a specific message.
     * <p>
     * In this implementation, a user can only delete their own messages.
     * </p>
     *
     * @param message The message to check.
     * @return {@code true} if the user can delete the message, {@code false} otherwise.
     */
    @Override
    public boolean canDelete(ForumMessage message) {
        return message.getUserId() != null && message.getUserId().equals(user.getId());
    }

    /**
     * Reloads the forum messages when the activity resumes to ensure the list is up-to-date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
    }
}
