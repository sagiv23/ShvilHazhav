package com.example.sagivproject.services;

/**
 * A central interface that acts as a façade for accessing all other data-related services.
 * <p>
 * This interface provides a single point of entry to get instances of all specific service interfaces,
 * simplifying dependency injection throughout the application.
 * </p>
 */
public interface IDatabaseService {
    /**
     * Gets the authentication service.
     *
     * @return The {@link IAuthService} instance.
     */
    IAuthService getAuthService();

    /**
     * Gets the user management service.
     *
     * @return The {@link IUserService} instance.
     */
    IUserService getUserService();

    /**
     * Gets the medication management service.
     *
     * @return The {@link IMedicationService} instance.
     */
    IMedicationService getMedicationService();

    /**
     * Gets the game management service.
     *
     * @return The {@link IGameService} instance.
     */
    IGameService getGameService();

    /**
     * Gets the statistics management service.
     *
     * @return The {@link IStatsService} instance.
     */
    IStatsService getStatsService();

    /**
     * Gets the forum message management service.
     *
     * @return The {@link IForumService} instance.
     */
    IForumService getForumService();

    /**
     * Gets the game image management service.
     *
     * @return The {@link IImageService} instance.
     */
    IImageService getImageService();

    /**
     * Gets the forum category management service.
     *
     * @return The {@link IForumCategoriesService} instance.
     */
    IForumCategoriesService getForumCategoriesService();

    /**
     * Gets the tip of the day management service.
     *
     * @return The {@link ITipOfTheDayService} instance.
     */
    ITipOfTheDayService getTipOfTheDayService();

    /**
     * A generic callback interface for asynchronous database operations.
     *
     * @param <T> The type of the expected result.
     */
    interface DatabaseCallback<T> {
        /**
         * Called when the asynchronous operation completes successfully.
         *
         * @param object The result of the operation. This can be null for operations that don't return a value.
         */
        void onCompleted(T object);

        /**
         * Called when the asynchronous operation fails.
         *
         * @param e The exception that occurred during the operation.
         */
        void onFailed(Exception e);
    }
}
