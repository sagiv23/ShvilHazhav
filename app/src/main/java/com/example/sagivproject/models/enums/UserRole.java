package com.example.sagivproject.models.enums;

/**
 * Defines the possible roles a user can have within the application.
 * <p>
 * Roles determine the level of access and available features for each user.
 * </p>
 */
public enum UserRole {
    /**
     * A standard user with regular permissions (Games, Forum, Medications).
     */
    REGULAR,
    /**
     * An administrative user with elevated permissions (User management, moderation).
     */
    ADMIN
}