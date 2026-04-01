package com.example.sagivproject.services;

import com.example.sagivproject.adapters.EmergencyContactsAdapter;
import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.adapters.ForumCategoryAdapter;
import com.example.sagivproject.adapters.GraphAdapter;
import com.example.sagivproject.adapters.LeaderboardAdapter;
import com.example.sagivproject.adapters.MedicationImagesTableAdapter;
import com.example.sagivproject.adapters.MedicationListAdapter;
import com.example.sagivproject.adapters.MedicationUsageAdapter;
import com.example.sagivproject.adapters.MemoryGameAdapter;
import com.example.sagivproject.adapters.MemoryGameLogAdapter;
import com.example.sagivproject.adapters.UsersTableAdapter;

/**
 * Interface that provides activity-scoped access to all RecyclerView adapters used in the application.
 * <p>
 * This service acts as a central provider for adapters, ensuring they are properly managed within
 * the activity lifecycle and enabling clean dependency injection into fragments and activities.
 * </p>
 */
public interface IAdapterService {

    /** @return The adapter for forum message lists. */
    ForumAdapter getForumAdapter();

    /** @return The adapter for forum category lists. */
    ForumCategoryAdapter getForumCategoryAdapter();

    /** @return The adapter for the user's medication schedule. */
    MedicationListAdapter getMedicationListAdapter();

    /** @return The adapter for memory game card grids. */
    MemoryGameAdapter getMemoryGameAdapter();

    /** @return The adapter for memory game history logs. */
    MemoryGameLogAdapter getMemoryGameLogAdapter();

    /** @return The adapter for the administrative user table. */
    UsersTableAdapter getUsersTableAdapter();

    /** @return The adapter for managing medication card images. */
    MedicationImagesTableAdapter getMedicationImagesTableAdapter();

    /** @return The adapter for the ranked user leaderboard. */
    LeaderboardAdapter getLeaderboardAdapter();

    /** @return The adapter for viewing historical medication logs. */
    MedicationUsageAdapter getMedicationUsageAdapter();

    /** @return The adapter for emergency contact cards. */
    EmergencyContactsAdapter getEmergencyContactsAdapter();

    /** @return The adapter for statistical XY graphs. */
    GraphAdapter getGraphAdapter();
}