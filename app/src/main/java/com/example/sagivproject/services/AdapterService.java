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

import javax.inject.Inject;

import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A service that provides activity-scoped access to all RecyclerView adapters used in the application.
 * <p>
 * This class acts as a central provider for adapters, ensuring they are properly managed within
 * the activity lifecycle and enabling clean dependency injection into fragments and activities.
 * By using this service, components can retrieve pre-configured adapter instances without
 * direct instantiation logic.
 * </p>
 */
@ActivityScoped
public class AdapterService {
    private final ForumAdapter forumAdapter;
    private final ForumCategoryAdapter forumCategoryAdapter;
    private final MedicationListAdapter medicationListAdapter;
    private final MemoryGameAdapter memoryGameAdapter;
    private final MemoryGameLogAdapter memoryGameLogAdapter;
    private final UsersTableAdapter usersTableAdapter;
    private final MedicationImagesTableAdapter medicationImagesTableAdapter;
    private final LeaderboardAdapter leaderboardAdapter;
    private final MedicationUsageAdapter medicationUsageAdapter;
    private final EmergencyContactsAdapter emergencyContactsAdapter;
    private final GraphAdapter graphAdapter;

    /**
     * Constructs a new AdapterService with all required adapters injected by Hilt.
     */
    @Inject
    public AdapterService(
            ForumAdapter forumAdapter,
            ForumCategoryAdapter forumCategoryAdapter,
            MedicationListAdapter medicationListAdapter,
            MemoryGameAdapter memoryGameAdapter,
            MemoryGameLogAdapter memoryGameLogAdapter,
            UsersTableAdapter usersTableAdapter,
            MedicationImagesTableAdapter medicationImagesTableAdapter,
            LeaderboardAdapter leaderboardAdapter,
            MedicationUsageAdapter medicationUsageAdapter,
            EmergencyContactsAdapter emergencyContactsAdapter,
            GraphAdapter graphAdapter
    ) {
        this.forumAdapter = forumAdapter;
        this.forumCategoryAdapter = forumCategoryAdapter;
        this.medicationListAdapter = medicationListAdapter;
        this.memoryGameAdapter = memoryGameAdapter;
        this.memoryGameLogAdapter = memoryGameLogAdapter;
        this.usersTableAdapter = usersTableAdapter;
        this.medicationImagesTableAdapter = medicationImagesTableAdapter;
        this.leaderboardAdapter = leaderboardAdapter;
        this.medicationUsageAdapter = medicationUsageAdapter;
        this.emergencyContactsAdapter = emergencyContactsAdapter;
        this.graphAdapter = graphAdapter;
    }

    /**
     * @return The adapter for forum message lists.
     */
    public ForumAdapter getForumAdapter() {
        return forumAdapter;
    }

    /**
     * @return The adapter for forum category lists.
     */
    public ForumCategoryAdapter getForumCategoryAdapter() {
        return forumCategoryAdapter;
    }

    /**
     * @return The adapter for the user's medication schedule.
     */
    public MedicationListAdapter getMedicationListAdapter() {
        return medicationListAdapter;
    }

    /**
     * @return The adapter for memory game card grids.
     */
    public MemoryGameAdapter getMemoryGameAdapter() {
        return memoryGameAdapter;
    }

    /**
     * @return The adapter for memory game history logs.
     */
    public MemoryGameLogAdapter getMemoryGameLogAdapter() {
        return memoryGameLogAdapter;
    }

    /**
     * @return The adapter for the administrative user table.
     */
    public UsersTableAdapter getUsersTableAdapter() {
        return usersTableAdapter;
    }

    /**
     * @return The adapter for managing medication card images.
     */
    public MedicationImagesTableAdapter getMedicationImagesTableAdapter() {
        return medicationImagesTableAdapter;
    }

    /**
     * @return The adapter for the ranked user leaderboard.
     */
    public LeaderboardAdapter getLeaderboardAdapter() {
        return leaderboardAdapter;
    }

    /**
     * @return The adapter for viewing historical medication logs.
     */
    public MedicationUsageAdapter getMedicationUsageAdapter() {
        return medicationUsageAdapter;
    }

    /**
     * @return The adapter for emergency contact cards.
     */
    public EmergencyContactsAdapter getEmergencyContactsAdapter() {
        return emergencyContactsAdapter;
    }

    /**
     * @return The adapter for statistical XY graphs.
     */
    public GraphAdapter getGraphAdapter() {
        return graphAdapter;
    }
}
