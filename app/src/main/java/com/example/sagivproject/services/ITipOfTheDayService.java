package com.example.sagivproject.services;

import com.example.sagivproject.models.TipOfTheDay;

import java.util.List;

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
     * Retrieves all tips stored in the database.
     *
     * @param callback The callback invoked with the list of {@link TipOfTheDay} objects.
     */
    void getAllTips(IDatabaseService.DatabaseCallback<List<TipOfTheDay>> callback);

    /**
     * Retrieves a specific tip by its date ID.
     *
     * @param dateId   The date ID (yyyy-MM-dd).
     * @param callback The callback invoked with the found {@link TipOfTheDay} or null.
     */
    void getTipByDate(String dateId, IDatabaseService.DatabaseCallback<TipOfTheDay> callback);

    /**
     * Persists a daily tip in the database using a transaction to prevent overwriting existing entries.
     *
     * @param tip      The {@link TipOfTheDay} object to save.
     * @param callback The callback invoked with the final tip stored in the database.
     */
    void saveTipIfNotExists(TipOfTheDay tip, IDatabaseService.DatabaseCallback<TipOfTheDay> callback);

    /**
     * Creates or updates a tip in the database.
     *
     * @param tip      The tip to save.
     * @param callback The callback to be invoked upon completion.
     */
    void saveTip(TipOfTheDay tip, IDatabaseService.DatabaseCallback<Void> callback);

    /**
     * Deletes a tip from the database by its date ID.
     *
     * @param dateId   The date ID (yyyy-MM-dd).
     * @param callback The callback to be invoked upon completion.
     */
    void deleteTip(String dateId, IDatabaseService.DatabaseCallback<Void> callback);
}
