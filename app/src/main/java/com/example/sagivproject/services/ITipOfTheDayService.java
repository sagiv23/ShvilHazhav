package com.example.sagivproject.services;

import com.example.sagivproject.models.TipOfTheDay;

/**
 * An interface for a service that handles the management of the "Tip of the Day".
 * <p>
 * This service is responsible for fetching the daily health or motivational tip from the database.
 * It also ensures that generated tips are saved globally so all users see the same content each day.
 * </p>
 */
public interface ITipOfTheDayService {
    /**
     * Retrieves the specific tip assigned to the current calendar day.
     *
     * @param callback The callback invoked with the {@link TipOfTheDay} or null if not yet created.
     */
    void getTipForToday(IDatabaseService.DatabaseCallback<TipOfTheDay> callback);

    /**
     * Persists a daily tip in the database using a transaction to prevent overwriting existing entries.
     *
     * @param tip      The {@link TipOfTheDay} object to save.
     * @param callback The callback invoked with the final tip stored in the database.
     */
    void saveTipIfNotExists(TipOfTheDay tip, IDatabaseService.DatabaseCallback<TipOfTheDay> callback);
}