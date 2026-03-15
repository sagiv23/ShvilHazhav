package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseForumFragment;
import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment that displays the messages within a specific forum category.
 * <p>
 * This fragment extends {@link BaseForumFragment} to inherit common forum logic
 * and implements {@link BaseForumFragment.ForumPermissions} to define message
 * deletion rules (users can delete their own messages).
 * </p>
 */
@AndroidEntryPoint
public class ForumFragment extends BaseForumFragment implements BaseForumFragment.ForumPermissions {
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = sharedPreferencesUtil.getUser();

        String categoryId = null;
        String categoryName = null;
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            categoryName = getArguments().getString("categoryName");
        }

        view.findViewById(R.id.btn_forum_back_to_categories).setOnClickListener(v -> navigateTo(R.id.action_forumFragment_to_forumCategoriesFragment));

        Button btnSendMessage = view.findViewById(R.id.btn_forum_send_message);
        btnSendMessage.setOnClickListener(v -> sendMessage());

        initForumViews(view, R.id.recycler_forum, R.id.edt_forum_new_message, R.id.btn_forum_new_messages_indicator);
        this.permissions = this;
        setupForum(view, categoryId, categoryName);
    }

    /**
     * Determines if the current user has permission to delete a specific forum message.
     *
     * @param message The message to check for deletion permission.
     * @return true if the current user is the author of the message, false otherwise.
     */
    @Override
    public boolean canDelete(ForumMessage message) {
        return user != null && message.getUserId() != null && message.getUserId().equals(user.getId());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}
