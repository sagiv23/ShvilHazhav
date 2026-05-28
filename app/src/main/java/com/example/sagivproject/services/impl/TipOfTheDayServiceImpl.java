package com.example.sagivproject.services.impl;

import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.services.DatabaseCallback;
import com.example.sagivproject.services.ITipOfTheDayService;
import com.example.sagivproject.utils.CalendarUtil;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Implementation of the {@link ITipOfTheDayService} interface.
 * <p>
 * This class manages the persistence and retrieval of the "Tip of the Day" in the
 * Firebase Realtime Database. It ensures that only one tip is saved per day
 * by using a date-based ID ("yyyy-MM-dd") and Firebase transactions to prevent overwrites.
 * </p>
 */
public class TipOfTheDayServiceImpl extends BaseDatabaseService<TipOfTheDay> implements ITipOfTheDayService {
    private static final String TIP_OF_THE_DAY_PATH = "tip_of_the_day";
    private static final String INSPIRATIONS_PATH = "inspirations";
    private final CalendarUtil calendarUtil;

    /**
     * Constructs a new TipOfTheDayServiceImpl.
     * Initializes the base service with the tips' path.
     *
     * @param firebaseDatabase The {@link FirebaseDatabase} instance.
     * @param calendarUtil     The {@link CalendarUtil} instance.
     */
    @Inject
    public TipOfTheDayServiceImpl(FirebaseDatabase firebaseDatabase, CalendarUtil calendarUtil) {
        super(firebaseDatabase, TIP_OF_THE_DAY_PATH, TipOfTheDay.class);
        this.calendarUtil = calendarUtil;
    }

    /**
     * Retrieves the tip assigned to the current calendar day.
     *
     * @param callback The callback invoked with the found {@link TipOfTheDay} or null.
     */
    @Override
    public void getTipForToday(DatabaseCallback<TipOfTheDay> callback) {
        String today = calendarUtil.getCurrentDate();
        get(today, callback);
    }

    /**
     * Retrieves all tips stored in the database.
     *
     * @param callback The callback invoked with the list of {@link TipOfTheDay} objects.
     */
    @Override
    public void getAllTips(DatabaseCallback<List<TipOfTheDay>> callback) {
        getAll(callback);
    }

    /**
     * Retrieves a specific tip by its date ID.
     *
     * @param dateId   The date ID (yyyy-MM-dd).
     * @param callback The callback invoked with the found {@link TipOfTheDay} or null.
     */
    @Override
    public void getTipByDate(String dateId, DatabaseCallback<TipOfTheDay> callback) {
        get(dateId, callback);
    }

    /**
     * Saves a daily tip only if no entry exists for its specific date ID.
     * <p>
     * Uses a transaction to ensure that if multiple users generate a tip simultaneously,
     * only the first one is committed, and others receive the already-stored tip.
     * </p>
     *
     * @param tip      The {@link TipOfTheDay} object to potentially save.
     * @param callback The callback invoked with the final tip stored in the database.
     */
    @Override
    public void saveTipIfNotExists(TipOfTheDay tip, DatabaseCallback<TipOfTheDay> callback) {
        runTransaction(TIP_OF_THE_DAY_PATH + "/" + tip.getId(), currentTip -> Objects.requireNonNullElse(currentTip, tip), callback);
    }

    @Override
    public void saveTip(TipOfTheDay tip, DatabaseCallback<Void> callback) {
        create(tip, callback);
    }

    @Override
    public void deleteTip(String dateId, DatabaseCallback<Void> callback) {
        delete(dateId, callback);
    }

    @Override
    public void getAllInspirations(DatabaseCallback<List<TipOfTheDay>> callback) {
        getDataList(INSPIRATIONS_PATH, callback);
    }

    @Override
    public void saveInspiration(TipOfTheDay inspiration, DatabaseCallback<Void> callback) {
        if (inspiration.getId() == null) {
            inspiration.setId(databaseReference.child(INSPIRATIONS_PATH).push().getKey());
        }
        writeData(INSPIRATIONS_PATH + "/" + inspiration.getId(), inspiration, callback);
    }

    @Override
    public void deleteInspiration(String id, DatabaseCallback<Void> callback) {
        deleteData(INSPIRATIONS_PATH + "/" + id, callback);
    }
}
