package com.example.sagivproject.utils;

import android.app.DatePickerDialog;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

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
     * Standard ISO date format for database storage.
     */
    public static final String DATABASE_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Standard ISO timestamp format for database storage.
     */
    public static final String DATABASE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Constructs a new CalendarUtil.
     */
    @Inject
    public CalendarUtil() {
    }

    /**
     * Opens a DatePickerDialog with customizable restrictions and formatting.
     *
     * @param context       The context to display the dialog in.
     * @param initialDate   The initial date to show in the picker (in yyyy-MM-dd format).
     * @param listener      The listener for the selected date.
     * @param futureOnly    If true, restricts date selection to today and future dates.
     * @param pastOnly      If true, restricts date selection to today and past dates.
     * @param displayFormat The date format string to use for the result display.
     */
    public void openDatePicker(Context context, String initialDate, OnDateSelectedListener listener, boolean futureOnly, boolean pastOnly, String displayFormat) {
        long initialMillis = parseDateFromDatabase(initialDate);
        openDatePicker(context, initialMillis, listener, futureOnly, pastOnly, displayFormat);
    }

    /**
     * Opens a DatePickerDialog with customizable restrictions and formatting.
     *
     * @param context       The context to display the dialog in.
     * @param initialMillis The initial date to show in the picker.
     * @param listener      The listener for the selected date.
     * @param futureOnly    If true, restricts date selection to today and future dates.
     * @param pastOnly      If true, restricts date selection to today and past dates.
     * @param displayFormat The date format string to use for the result display.
     */
    public void openDatePicker(Context context, long initialMillis, OnDateSelectedListener listener, boolean futureOnly, boolean pastOnly, String displayFormat) {
        final Calendar calendar = Calendar.getInstance();
        if (initialMillis != -1) {
            calendar.setTimeInMillis(initialMillis);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, day) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, day, 0, 0, 0);
                    selectedCal.set(Calendar.MILLISECOND, 0);

                    long selectedMillis = selectedCal.getTimeInMillis();
                    String dbDate = formatDateForDatabase(selectedMillis);
                    String formattedDate = formatDate(selectedMillis, displayFormat);

                    if (listener != null) {
                        listener.onDateSelected(selectedMillis, dbDate, formattedDate);
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
     * Formats a timestamp from milliseconds to a string for database storage ("yyyy-MM-dd").
     *
     * @param millis The timestamp in milliseconds.
     * @return The formatted date string.
     */
    public String formatDateForDatabase(long millis) {
        return formatDate(millis, DATABASE_DATE_FORMAT);
    }

    /**
     * Parses a date string from database format ("yyyy-MM-dd") back to milliseconds.
     *
     * @param dateStr The date string.
     * @return The timestamp in milliseconds, or -1 if parsing fails.
     */
    public long parseDateFromDatabase(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATABASE_DATE_FORMAT, Locale.getDefault());
            Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Formats a timestamp from milliseconds to a string for database storage ("yyyy-MM-dd HH:mm:ss").
     *
     * @param millis The timestamp in milliseconds.
     * @return The formatted timestamp string.
     */
    public String formatTimestampForDatabase(long millis) {
        return formatDate(millis, DATABASE_TIMESTAMP_FORMAT);
    }

    /**
     * @return The current system time formatted for database storage.
     */
    public String getCurrentTimestamp() {
        return formatTimestampForDatabase(System.currentTimeMillis());
    }

    /**
     * Parses a timestamp string from database format ("yyyy-MM-dd HH:mm:ss") back to milliseconds.
     *
     * @param timestampStr The timestamp string.
     * @return The timestamp in milliseconds, or -1 if parsing fails.
     */
    public long parseTimestampFromDatabase(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) return -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATABASE_TIMESTAMP_FORMAT, Locale.getDefault());
            Date date = sdf.parse(timestampStr);
            return date != null ? date.getTime() : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Formats a timestamp from milliseconds to a string using the default format ("dd/MM/yyyy").
     *
     * @param millis The timestamp in milliseconds.
     * @return The formatted date string.
     */
    public String formatDate(long millis) {
        return formatDate(millis, DEFAULT_DATE_FORMAT);
    }

    /**
     * Formats a timestamp from milliseconds to a string using a specified format.
     *
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
         *
         * @param dateMillis    The selected date in milliseconds.
         * @param dbDate        The date formatted for database (yyyy-MM-dd).
         * @param formattedDate The selected date formatted for display.
         */
        void onDateSelected(long dateMillis, String dbDate, String formattedDate);
    }
}