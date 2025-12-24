package com.example.sagivproject.screens.forum;

import com.example.sagivproject.models.ForumMessage;

public interface ForumPermissions {
    boolean canDelete(ForumMessage message);
}