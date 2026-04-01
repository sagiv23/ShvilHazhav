package com.example.sagivproject.utils;

import android.app.DatePickerDialog;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A utility class for handling date and calendar-related operations.
 * <p>
 * This class provides methods to open a standardized {@link DatePickerDialog}
 * and to format date values into strings. It is managed as a Singleton by Hilt.
 * </p>
 */
@Singleton
public class CalendarUtil {
    /**
     * The default date display format used throughout the app.
     */
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";

    /**
     * Constructs a new CalendarUtil.
     * @param context The application context.
     */
    @Inject
    public CalendarUtil(@ApplicationContext Context context) {
    }

    /**
     * Opens a DatePickerDialog with customizable restrictions and formatting.
     * @param context The context to display the dialog in.
     * @param initialMillis The initial date to show in the picker.
     * @param listener The listener for the selected date.
     * @param futureOnly If true, restricts date selection to today and future dates.
     * @param pastOnly If true, restricts date selection to today and past dates.
     * @param format The date format string to use for the result.
     */
    public void openDatePicker(Context context, long initialMillis, OnDateSelectedListener listener, boolean futureOnly, boolean pastOnly, String format) {
        final Calendar calendar = Calendar.getInstance();
        if (initialMillis > 0) {
            calendar.setTimeInMillis(initialMillis);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, day) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, day, 0, 0, 0);
                    selectedCal.set(Calendar.MILLISECOND, 0);

                    long selectedMillis = selectedCal.getTimeInMillis();
                    String formattedDate = formatDate(selectedMillis, format);

                    if (listener != null) {
                        listener.onDateSelected(selectedMillis, formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (futureOnly) {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }
        if (pastOnly) {
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        dialog.show();
    }

    /**
     * Formats a timestamp from milliseconds to a string using the default format ("dd/MM/yyyy").
     * @param millis The timestamp in milliseconds.
     * @return The formatted date string.
     */
    public String formatDate(long millis) {
        return formatDate(millis, DEFAULT_DATE_FORMAT);
    }

    /**
     * Formats a timestamp from milliseconds to a string using a specified format.
     * @param millis The timestamp in milliseconds.
     * @param format The desired date format (e.g., "yyyy-MM-dd").
     * @return The formatted date string.
     */
    public String formatDate(long millis, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    /**
     * A listener interface for receiving the result from the date picker dialog.
     */
    public interface OnDateSelectedListener {
        /**
         * Called when the user confirms their date selection.
         * @param dateMillis The selected date in milliseconds.
         * @param formattedDate The selected date formatted as a string.
         */
        void onDateSelected(long dateMillis, String formattedDate);
    }
}