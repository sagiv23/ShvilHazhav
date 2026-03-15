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
 * by using a date-based ID and Firebase transactions.
 * </p>
 */
public class TipOfTheDayServiceImpl extends BaseDatabaseService<TipOfTheDay> implements ITipOfTheDayService {
    private static final String TIP_OF_THE_DAY_PATH = "tip_of_the_day";
    private static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Constructs a new TipOfTheDayServiceImpl.
     */
    @Inject
    public TipOfTheDayServiceImpl() {
        super(TIP_OF_THE_DAY_PATH, TipOfTheDay.class);
    }

    @Override
    public void getTipForToday(IDatabaseService.DatabaseCallback<TipOfTheDay> callback) {
        String today = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        get(today, callback);
    }

    @Override
    public void saveTipIfNotExists(TipOfTheDay tip, IDatabaseService.DatabaseCallback<TipOfTheDay> callback) {
        runTransaction(TIP_OF_THE_DAY_PATH + "/" + tip.getId(), currentTip -> {
            // If there's already a tip for today, we don't overwrite it.
            return Objects.requireNonNullElse(currentTip, tip);
        }, callback);
    }
}
