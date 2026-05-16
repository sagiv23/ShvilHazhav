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
     * @param initialDate   The initial date in database format ("yyyy-MM-dd"). If null, current date is used.
     * @param listener      The listener for the selected date.
     * @param futureOnly    If true, restricts date selection to today and future dates.
     * @param pastOnly      If true, restricts date selection to today and past dates.
     * @param displayFormat The date format string to use for the result display. If null, uses {@link #DEFAULT_DATE_FORMAT}.
     */
    public void openDatePicker(Context context, String initialDate, OnDateSelectedListener listener, boolean futureOnly, boolean pastOnly, String displayFormat) {
        long initialMillis = parseDateFromDatabase(initialDate);

        final Calendar calendar = Calendar.getInstance();
        if (initialMillis > 0) {
            calendar.setTimeInMillis(initialMillis);
        }

        String finalDisplayFormat = (displayFormat != null) ? displayFormat : DEFAULT_DATE_FORMAT;

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, day) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, day, 0, 0, 0);
                    selectedCal.set(Calendar.MILLISECOND, 0);

                    long selectedMillis = selectedCal.getTimeInMillis();
                    if (listener != null) {
                        listener.onDateSelected(
                                selectedMillis,
                                formatDate(selectedMillis, DATABASE_DATE_FORMAT),
                                formatDate(selectedMillis, finalDisplayFormat)
                        );
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (futureOnly) {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }
        if (pastOnly) {
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        dialog.show();
    }

    /**
     * Parses a date string from database format ("yyyy-MM-dd") back to milliseconds.
     *
     * @param dateStr The date string.
     * @return The timestamp in milliseconds, or -1 if parsing fails.
     */
    public long parseDateFromDatabase(String dateStr) {
        return parse(dateStr, DATABASE_DATE_FORMAT);
    }

    /**
     * Parses a timestamp string from database format ("yyyy-MM-dd HH:mm:ss") back to milliseconds.
     *
     * @param timestampStr The timestamp string.
     * @return The timestamp in milliseconds, or -1 if parsing fails.
     */
    public long parseTimestampFromDatabase(String timestampStr) {
        return parse(timestampStr, DATABASE_TIMESTAMP_FORMAT);
    }

    /**
     * @return The current system date formatted for database storage ("yyyy-MM-dd").
     */
    public String getCurrentDate() {
        return formatDate(System.currentTimeMillis(), DATABASE_DATE_FORMAT);
    }

    /**
     * @return The current system time formatted for database storage.
     */
    public String getCurrentTimestamp() {
        return formatDate(System.currentTimeMillis(), DATABASE_TIMESTAMP_FORMAT);
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
     * Calculates age based on a birthdate string.
     *
     * @param birthDateStr Birthdate in database format ("yyyy-MM-dd").
     * @return The age in years, or -1 if invalid.
     */
    public int calculateAge(String birthDateStr) {
        long birthMillis = parseDateFromDatabase(birthDateStr);
        if (birthMillis == -1) return -1;

        Calendar birth = Calendar.getInstance();
        birth.setTimeInMillis(birthMillis);
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        return age;
    }

    /**
     * Internal helper to parse a date/timestamp string based on a given format.
     */
    private long parse(String str, String format) {
        if (str == null || str.isEmpty()) return -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            Date date = sdf.parse(str);
            return date != null ? date.getTime() : -1;
        } catch (Exception e) {
            return -1;
        }
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
