package com.example.sagivproject.screens.forum;

import com.example.sagivproject.models.ForumMessage;
import com.example.sagivproject.models.User;

public class UserForumPermissions implements ForumPermissions {
    private final User user;

    public UserForumPermissions(User user) {
        this.user = user;
    }

    @Override
    public boolean canDelete(ForumMessage message) {
        return message.getUserId() != null && message.getUserId().equals(user.getUid());
    }
}