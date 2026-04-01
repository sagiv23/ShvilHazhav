package com.example.sagivproject.services.impl;

import com.example.sagivproject.bases.BaseDatabaseService;
import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.ITipOfTheDayService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import javax.inject.Inject;

/**
 * Implementation of the {@link ITipOfTheDayService} interface.
 * <p>
 * This class manages the persistence and retrieval of the "Tip of the Day" in the
 * Firebase Realtime Database. It ensures that only one tip is saved per day
 * by using a date-based ID ("yyyymmdd") and Firebase transactions to prevent overwrites.
 * </p>
 */
public class TipOfTheDayServiceImpl extends BaseDatabaseService<TipOfTheDay> implements ITipOfTheDayService {
    /** The root database path for daily tips. */
    private static final String TIP_OF_THE_DAY_PATH = "tip_of_the_day";
    /** The date format used for generating daily identifiers. */
    private static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Constructs a new TipOfTheDayServiceImpl.
     * Initializes the base service with the tips' path.
     */
    @Inject
    public TipOfTheDayServiceImpl() { super(TIP_OF_THE_DAY_PATH, TipOfTheDay.class); }

    /**
     * Retrieves the tip assigned to the current calendar day.
     * @param callback The callback invoked with the found {@link TipOfTheDay} or null.
     */
    @Override
    public void getTipForToday(IDatabaseService.DatabaseCallback<TipOfTheDay> callback) {
        String today = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        get(today, callback);
    }

    /**
     * Saves a daily tip only if no entry exists for its specific date ID.
     * <p>
     * Uses a transaction to ensure that if multiple users generate a tip simultaneously,
     * only the first one is committed, and others receive the already-stored tip.
     * </p>
     * @param tip The {@link TipOfTheDay} object to potentially save.
     * @param callback The callback invoked with the final tip stored in the database.
     */
    @Override
    public void saveTipIfNotExists(TipOfTheDay tip, IDatabaseService.DatabaseCallback<TipOfTheDay> callback) { runTransaction(TIP_OF_THE_DAY_PATH + "/" + tip.getId(), currentTip -> Objects.requireNonNullElse(currentTip, tip), callback); }
}