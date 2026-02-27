package com.example.sagivproject.services;

import com.example.sagivproject.models.TipOfTheDay;

/**
 * An interface for a service that handles fetching and saving the "Tip of the Day".
 */
public interface ITipOfTheDayService {
    /**
     * Retrieves the tip for the current day.
     *
     * @param callback The callback to be invoked with the tip or an error.
     */
    void getTipForToday(IDatabaseService.DatabaseCallback<TipOfTheDay> callback);

    /**
     * Saves the tip for the current day.
     *
     * @param tip      The tip to save.
     * @param callback The callback to be invoked upon completion.
     */
    void saveTipForToday(String tip, IDatabaseService.DatabaseCallback<Void> callback);
}
