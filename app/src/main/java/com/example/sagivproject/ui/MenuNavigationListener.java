package com.example.sagivproject.ui;

/**
 * Interface for communication between menu fragments and the hosting activity.
 */
public interface MenuNavigationListener {
    void onNavigate(Class<?> targetActivity);
}
