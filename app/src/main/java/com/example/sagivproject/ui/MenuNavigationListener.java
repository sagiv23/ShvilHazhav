package com.example.sagivproject.ui;

import android.os.Bundle;

/**
 * Interface for communication between menu fragments and the hosting activity.
 * <p>
 * This listener allows menu components (like the navigation drawer) to request
 * navigation actions from the Activity, ensuring a decoupled architecture.
 * </p>
 */
public interface MenuNavigationListener {
    /**
     * Navigates to a specific destination in the navigation graph.
     *
     * @param resId The resource ID of the destination or action.
     */
    void onNavigate(int resId);

    /**
     * Navigates to a specific destination in the navigation graph with arguments.
     *
     * @param resId The resource ID of the destination or action.
     * @param args  The arguments to pass to the destination fragment.
     */
    void onNavigate(int resId, Bundle args);
}
