package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseForumFragment;
import com.example.sagivproject.models.ForumMessage;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An admin fragment for managing forum messages.
 */
@AndroidEntryPoint
public class AdminForumFragment extends BaseForumFragment implements BaseForumFragment.ForumPermissions {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_forum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String categoryId = null;
        String categoryName = null;
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            categoryName = getArguments().getString("categoryName");
        }

        view.findViewById(R.id.btn_admin_forum_send_message).setOnClickListener(v -> sendMessage());
        view.findViewById(R.id.btn_admin_forum_back_to_admin_categories).setOnClickListener(v -> navigateTo(R.id.action_adminForumFragment_to_adminForumCategoriesFragment));

        initForumViews(view, R.id.recycler_AdminForum, R.id.edt_AdminForum_new_message, R.id.btn_AdminForum_new_messages_indicator);
        this.permissions = this;
        setupForum(view, categoryId, categoryName);
    }

    @Override
    public boolean canDelete(ForumMessage message) {
        return true; // Admin can delete everything
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}
