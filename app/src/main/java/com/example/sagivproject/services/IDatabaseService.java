package com.example.sagivproject.services;

/**
 * A central interface that acts as a façade for accessing all domain-specific data services.
 * <p>
 * This interface provides a single point of entry to obtain instances of various services
 * (Auth, User, Medication, Game, etc.), simplifying dependency injection and service management.
 * It also defines common callback structures for asynchronous operations.
 * </p>
 */
public interface IDatabaseService {
    /** @return The authentication and session management service. */
    IAuthService getAuthService();

    /** @return The primary user data and account service. */
    IUserService getUserService();

    /** @return The medication prescription and usage logging service. */
    IMedicationService getMedicationService();

    /** @return The online memory game coordination service. */
    IMemoryGameService getGameService();

    /** @return The activity performance and historical statistics service. */
    IStatsService getStatsService();

    /** @return The forum message persistence and retrieval service. */
    IForumService getForumService();

    /** @return The image asset management service for game content. */
    IImageService getImageService();

    /** @return The forum discussion topic management service. */
    IForumCategoriesService getForumCategoriesService();

    /** @return The daily advice and motivational tips service. */
    ITipOfTheDayService getTipOfTheDayService();

    /** @return The emergency contact and SMS alerting service. */
    IEmergencyService getEmergencyService();

    /**
     * A generic callback interface for handling the results of asynchronous database operations.
     * @param <T> The type of the data returned upon successful completion.
     */
    interface DatabaseCallback<T> {
        /**
         * Invoked when the asynchronous operation completes successfully.
         * @param object The resulting data object (can be null if the operation has no return value).
         */
        void onCompleted(T object);

        /**
         * Invoked when the asynchronous operation fails due to an error or exception.
         * @param e The exception that occurred during the process.
         */
        void onFailed(Exception e);
    }
}