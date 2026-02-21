package com.example.sagivproject.services;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A singleton façade that provides a single point of access to all other database-related services.
 * <p>
 * This class implements the {@link IDatabaseService} interface and acts as a container for all
 * specific service interfaces (e.g., {@link IUserService}, {@link IAuthService}). It uses
 * dependency injection to get instances of these services and exposes them through getter methods.
 * This simplifies dependency management in other parts of the application, such as activities and ViewModels,
 * which now only need to inject this single `DatabaseService`.
 * </p>
 */
@Singleton
public class DatabaseService implements IDatabaseService {
    private final IAuthService authService;
    private final IUserService userService;
    private final IMedicationService medicationService;
    private final IGameService gameService;
    private final IStatsService statsService;
    private final IForumService forumService;
    private final IImageService imageService;
    private final IForumCategoriesService forumCategoriesService;
    private final ITipOfTheDayService tipOfTheDayService;

    /**
     * Constructs a new DatabaseService.
     *
     * @param authService            The authentication service.
     * @param userService            The user management service.
     * @param medicationService      The medication management service.
     * @param gameService            The game management service.
     * @param statsService           The statistics management service.
     * @param forumService           The forum message management service.
     * @param imageService           The game image management service.
     * @param forumCategoriesService The forum category management service.
     * @param tipOfTheDayService     The tip of the day management service.
     */
    @Inject
    public DatabaseService(
            IAuthService authService,
            IUserService userService,
            IMedicationService medicationService,
            IGameService gameService,
            IStatsService statsService,
            IForumService forumService,
            IImageService imageService,
            IForumCategoriesService forumCategoriesService,
            ITipOfTheDayService tipOfTheDayService
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
    }

    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    @Override
    public IUserService getUserService() {
        return userService;
    }

    @Override
    public IMedicationService getMedicationService() {
        return medicationService;
    }

    @Override
    public IGameService getGameService() {
        return gameService;
    }

    @Override
    public IStatsService getStatsService() {
        return statsService;
    }

    @Override
    public IForumService getForumService() {
        return forumService;
    }

    @Override
    public IImageService getImageService() {
        return imageService;
    }

    @Override
    public IForumCategoriesService getForumCategoriesService() {
        return forumCategoriesService;
    }

    @Override
    public ITipOfTheDayService getTipOfTheDayService() {
        return tipOfTheDayService;
    }
}
