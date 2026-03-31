package com.example.sagivproject.services.impl;

import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IEmergencyService;
import com.example.sagivproject.services.IForumCategoriesService;
import com.example.sagivproject.services.IForumService;
import com.example.sagivproject.services.IImageService;
import com.example.sagivproject.services.IMedicationService;
import com.example.sagivproject.services.IMemoryGameService;
import com.example.sagivproject.services.IStatsService;
import com.example.sagivproject.services.ITipOfTheDayService;
import com.example.sagivproject.services.IUserService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A singleton façade service that provides a single point of access to all specific database-related services.
 * <p>
 * This class implements the {@link IDatabaseService} interface and coordinates between various
 * domain-specific services (e.g., Auth, Users, Medications). It uses dependency injection
 * to aggregate these services, simplifying the dependency graph for activities and ViewModels
 * which now only need to interact with this single entry point.
 * </p>
 */
@Singleton
public class DatabaseService implements IDatabaseService {
    private final IAuthService authService;
    private final IUserService userService;
    private final IMedicationService medicationService;
    private final IMemoryGameService gameService;
    private final IStatsService statsService;
    private final IForumService forumService;
    private final IImageService imageService;
    private final IForumCategoriesService forumCategoriesService;
    private final ITipOfTheDayService tipOfTheDayService;
    private final IEmergencyService emergencyService;

    /**
     * Constructs a new DatabaseService with all specific sub-services injected.
     *
     * @param authService            Service handling authentication and user creation logic.
     * @param userService            Service for direct user-related database CRUD operations.
     * @param medicationService      Service managing medication records and usage logs.
     * @param gameService            Service coordinating memory game sessions and logic.
     * @param statsService           Service for updating and retrieving activity statistics.
     * @param forumService           Service for forum message persistence and retrieval.
     * @param imageService           Service managing image assets for game cards.
     * @param forumCategoriesService Service for managing forum discussion topics.
     * @param tipOfTheDayService     Service providing daily health and motivational tips.
     * @param emergencyService       Service managing emergency contacts and SMS alerts.
     */
    @Inject
    public DatabaseService(
            IAuthService authService,
            IUserService userService,
            IMedicationService medicationService,
            IMemoryGameService gameService,
            IStatsService statsService,
            IForumService forumService,
            IImageService imageService,
            IForumCategoriesService forumCategoriesService,
            ITipOfTheDayService tipOfTheDayService,
            IEmergencyService emergencyService
    ) {
        this.authService = authService;
        this.userService = userService;
        this.medicationService = medicationService;
        this.gameService = gameService;
        this.statsService = statsService;
        this.forumService = forumService;
        this.imageService = imageService;
        this.forumCategoriesService = forumCategoriesService;
        this.tipOfTheDayService = tipOfTheDayService;
        this.emergencyService = emergencyService;
    }

    /**
     * @return The authentication and account management service.
     */
    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    /**
     * @return The primary user data service.
     */
    @Override
    public IUserService getUserService() {
        return userService;
    }

    /**
     * @return The medication management and logging service.
     */
    @Override
    public IMedicationService getMedicationService() {
        return medicationService;
    }

    /**
     * @return The memory game coordination service.
     */
    @Override
    public IMemoryGameService getGameService() {
        return gameService;
    }

    /**
     * @return The performance tracking and statistics service.
     */
    @Override
    public IStatsService getStatsService() {
        return statsService;
    }

    /**
     * @return The forum message management service.
     */
    @Override
    public IForumService getForumService() {
        return forumService;
    }

    /**
     * @return The image asset management service.
     */
    @Override
    public IImageService getImageService() {
        return imageService;
    }

    /**
     * @return The forum category management service.
     */
    @Override
    public IForumCategoriesService getForumCategoriesService() {
        return forumCategoriesService;
    }

    /**
     * @return The daily advice and motivational tip service.
     */
    @Override
    public ITipOfTheDayService getTipOfTheDayService() {
        return tipOfTheDayService;
    }

    /**
     * @return The emergency contact and alerting service.
     */
    @Override
    public IEmergencyService getEmergencyService() {
        return emergencyService;
    }
}
