package com.example.sagivproject.services;

import com.example.sagivproject.adapters.ForumAdapter;
import com.example.sagivproject.adapters.ForumCategoryAdapter;
import com.example.sagivproject.adapters.LeaderboardAdapter;
import com.example.sagivproject.adapters.MedicationImagesTableAdapter;
import com.example.sagivproject.adapters.MedicationListAdapter;
import com.example.sagivproject.adapters.MemoryGameAdapter;
import com.example.sagivproject.adapters.MemoryGameLogAdapter;
import com.example.sagivproject.adapters.UsersTableAdapter;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ActivityScoped;

/**
 * A service that provides access to all RecyclerView adapters used in the application.
 * <p>
 * This class uses Hilt to inject and manage the lifecycle of various adapters.
 * By centralizing adapter access, it simplifies dependency management in Activities
 * and ensures that adapters requiring Activity context are correctly instantiated.
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

    @Inject
    public AdapterService(
            ForumAdapter forumAdapter,
            ForumCategoryAdapter forumCategoryAdapter,
            MedicationListAdapter medicationListAdapter,
            MemoryGameAdapter memoryGameAdapter,
            MemoryGameLogAdapter memoryGameLogAdapter,
            UsersTableAdapter usersTableAdapter,
            MedicationImagesTableAdapter medicationImagesTableAdapter,
            LeaderboardAdapter leaderboardAdapter
    ) {
        this.forumAdapter = forumAdapter;
        this.forumCategoryAdapter = forumCategoryAdapter;
        this.medicationListAdapter = medicationListAdapter;
        this.memoryGameAdapter = memoryGameAdapter;
        this.memoryGameLogAdapter = memoryGameLogAdapter;
        this.usersTableAdapter = usersTableAdapter;
        this.medicationImagesTableAdapter = medicationImagesTableAdapter;
        this.leaderboardAdapter = leaderboardAdapter;
    }

    public ForumAdapter getForumAdapter() {
        return forumAdapter;
    }

    public ForumCategoryAdapter getForumCategoryAdapter() {
        return forumCategoryAdapter;
    }

    public MedicationListAdapter getMedicationListAdapter() {
        return medicationListAdapter;
    }

    public MemoryGameAdapter getMemoryGameAdapter() {
        return memoryGameAdapter;
    }

    public MemoryGameLogAdapter getMemoryGameLogAdapter() {
        return memoryGameLogAdapter;
    }

    public UsersTableAdapter getUsersTableAdapter() {
        return usersTableAdapter;
    }

    public MedicationImagesTableAdapter getMedicationImagesTableAdapter() {
        return medicationImagesTableAdapter;
    }

    public LeaderboardAdapter getLeaderboardAdapter() {
        return leaderboardAdapter;
    }
}
