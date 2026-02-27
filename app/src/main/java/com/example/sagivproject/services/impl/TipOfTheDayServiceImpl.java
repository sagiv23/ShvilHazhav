package com.example.sagivproject.services.impl;

import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.ITipOfTheDayService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

/**
 * An implementation of the {@link ITipOfTheDayService} interface.
 * <p>
 * This service handles operations related to the "Tip of the Day", such as fetching and saving the tip
 * for the current day. It extends {@link BaseDatabaseService} to leverage common
 * Firebase database interactions.
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

    /**
     * Retrieves the tip for the current day from the database.
     *
     * @param callback The callback to be invoked with the tip or an error.
     */
    @Override
    public void getTipForToday(IDatabaseService.DatabaseCallback<TipOfTheDay> callback) {
        String today = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        get(today, callback);
    }

    /**
     * Saves the tip for the current day to the database.
     *
     * @param tip      The tip to save.
     * @param callback The callback to be invoked upon completion.
     */
    @Override
    public void saveTipForToday(String tip, IDatabaseService.DatabaseCallback<Void> callback) {
        String today = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        TipOfTheDay tipOfTheDay = new TipOfTheDay(tip, today);
        tipOfTheDay.setId(today);
        create(tipOfTheDay, callback);
    }
}
