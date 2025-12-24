package com.example.sagivproject.screens.forum;

import com.example.sagivproject.models.ForumMessage;

public class AdminForumPermissions implements ForumPermissions {
    @Override
    public boolean canDelete(ForumMessage message) {
        return true;
    }
}