package com.example.sagivproject.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import com.example.sagivproject.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A utility class for handling date and calendar-related operations using java. Time API.
 * <p>
 * This class provides methods to open standardized DatePickerDialog and TimePickerDialog,
 * and to format date/time values into strings. It is managed as a Singleton by Hilt.
 * </p>
 */
@Singleton
public class CalendarUtil {
    /**
     * Default date display format: dd/MM/yyyy
     */
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    /**
     * Database date storage format: yyyy-MM-dd
     */
    public static final String DATABASE_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * Database timestamp storage format: yyyy-MM-dd HH:mm:ss
     */
    public static final String DATABASE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * Display timestamp format: dd/MM/yyyy HH:mm
     */
    public static final String DISPLAY_TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm";
    /**
     * Key format for month-based queries: yyyy-MM
     */
    public static final String MONTH_KEY_FORMAT = "yyyy-MM";

    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATABASE_DATE_FORMAT);
    private static final DateTimeFormatter DB_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(DATABASE_TIMESTAMP_FORMAT);
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter DISPLAY_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_TIMESTAMP_FORMAT);
    private static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern(MONTH_KEY_FORMAT);

    /**
     * Constructs a new CalendarUtil. Injected by Hilt.
     */
    @Inject
    public CalendarUtil() {
    }

    /**
     * Opens a DatePickerDialog with customizable restrictions and formatting.
     *
     * @param context       The UI context.
     * @param initialDate   Initial date in DATABASE_DATE_FORMAT. If null, current date is used.
     * @param listener      Callback for the selected date.
     * @param futureOnly    If true, prevents selecting past dates.
     * @param pastOnly      If true, prevents selecting future dates.
     * @param displayFormat Custom format for the display string. If null, DEFAULT_DATE_FORMAT is used.
     */
    public void openDatePicker(Context context, String initialDate, OnDateSelectedListener listener, boolean futureOnly, boolean pastOnly, String displayFormat) {
        LocalDate initial = null;
        if (initialDate != null) {
            try {
                initial = LocalDate.parse(initialDate, DB_DATE_FORMATTER);
            } catch (Exception ignored) {
            }
        }
        if (initial == null) {
            initial = LocalDate.now();
        }

        DateTimeFormatter finalDisplayFormatter = (displayFormat != null)
                ? DateTimeFormatter.ofPattern(displayFormat)
                : DISPLAY_DATE_FORMATTER;

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    LocalDate selected = LocalDate.of(year, month + 1, dayOfMonth);
                    if (listener != null) {
                        listener.onDateSelected(
                                selected.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                selected.format(DB_DATE_FORMATTER),
                                selected.format(finalDisplayFormatter)
                        );
                    }
                },
                initial.getYear(),
                initial.getMonthValue() - 1,
                initial.getDayOfMonth()
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
     * Opens a TimePickerDialog for selecting a time.
     *
     * @param context     The UI context.
     * @param initialTime Initial time in "HH:mm" format. If null, current time is used.
     * @param listener    Callback for the selected time.
     */
    public void openTimePicker(Context context, String initialTime, OnTimeSelectedListener listener) {
        LocalTime initial = null;
        if (initialTime != null && initialTime.contains(":")) {
            try {
                initial = LocalTime.parse(initialTime);
            } catch (Exception ignored) {
            }
        }
        if (initial == null) {
            initial = LocalTime.now();
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                R.style.TimePickerCustomTheme,
                (view, hourOfDay, minuteOfHour) -> {
                    if (listener != null) {
                        listener.onTimeSelected(hourOfDay, minuteOfHour, formatTime(hourOfDay, minuteOfHour));
                    }
                },
                initial.getHour(),
                initial.getMinute(),
                true
        );

        timePickerDialog.show();
    }

    /**
     * Parses a date string from database format to epoch milliseconds.
     *
     * @param dateStr Date in DATABASE_DATE_FORMAT.
     * @return Epoch milliseconds, or -1 on failure.
     */
    public long parseDateFromDatabase(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return -1;
        try {
            return LocalDate.parse(dateStr, DB_DATE_FORMATTER)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Parses a month key (yyyy-MM) to epoch milliseconds of the first day of that month.
     *
     * @param monthKey Month in MONTH_KEY_FORMAT.
     * @return Epoch milliseconds, or -1 on failure.
     */
    public long parseMonthKey(String monthKey) {
        if (monthKey == null || monthKey.isEmpty()) return -1;
        try {
            return YearMonth.parse(monthKey, MONTH_KEY_FORMATTER)
                    .atDay(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gets the current system date in database format.
     *
     * @return Formatted date string.
     */
    public String getCurrentDate() {
        return LocalDate.now().format(DB_DATE_FORMATTER);
    }

    /**
     * Gets the current system timestamp in database format.
     *
     * @return Formatted timestamp string.
     */
    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DB_TIMESTAMP_FORMATTER);
    }

    /**
     * Formats epoch milliseconds to display date string.
     *
     * @param millis Epoch milliseconds.
     * @return Formatted date string in DEFAULT_DATE_FORMAT.
     */
    public String formatDate(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Formats epoch milliseconds to string using custom format.
     *
     * @param millis Epoch milliseconds.
     * @param format Pattern string.
     * @return Formatted date/time string.
     */
    public String formatDate(long millis, String format) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Formats hour and minute into "HH:mm" string.
     *
     * @param hour   Hour (0-23).
     * @param minute Minute (0-59).
     * @return Formatted time string.
     */
    public String formatTime(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    /**
     * Gets epoch milliseconds from specific year, month, and day.
     *
     * @param year  The year.
     * @param month The month (0-11).
     * @param day   The day of month.
     * @return Epoch milliseconds.
     */
    public long getMillisFromDate(int year, int month, int day) {
        return LocalDate.of(year, month + 1, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    /**
     * Calculates the epoch milliseconds for the next occurrence of a given time.
     *
     * @param hourStr       Time in "HH:mm" format.
     * @param forceTomorrow If true, always returns a time in the future (at least tomorrow).
     * @return Epoch milliseconds, or -1 on failure.
     */
    public long getNextOccurrenceMillis(String hourStr, boolean forceTomorrow) {
        if (hourStr == null || !hourStr.contains(":")) return -1;
        try {
            LocalTime time = LocalTime.parse(hourStr);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime occurrence = now.with(time).withSecond(0).withNano(0);

            if (forceTomorrow || occurrence.isBefore(now)) {
                occurrence = occurrence.plusDays(1);
            }
            return occurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Formats a month key (yyyy-MM) to display format (MM/yyyy).
     *
     * @param monthKey Month in MONTH_KEY_FORMAT.
     * @return Formatted display string.
     */
    public String formatMonthKeyForDisplay(String monthKey) {
        if (monthKey == null || !monthKey.contains("-")) return monthKey;
        try {
            return YearMonth.parse(monthKey, MONTH_KEY_FORMATTER)
                    .format(DateTimeFormatter.ofPattern("MM/yyyy"));
        } catch (Exception e) {
            return monthKey;
        }
    }

    /**
     * Converts a database date string (yyyy-MM-dd) to display format (dd/MM/yyyy).
     *
     * @param dbDate Date in DATABASE_DATE_FORMAT.
     * @return Formatted display string.
     */
    public String formatDbDateToDisplay(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) return "";
        try {
            return LocalDate.parse(dbDate, DB_DATE_FORMATTER)
                    .format(DISPLAY_DATE_FORMATTER);
        } catch (Exception e) {
            return dbDate;
        }
    }

    /**
     * Converts a database timestamp string (yyyy-MM-dd HH:mm:ss) to display format (dd/MM/yyyy HH:mm).
     *
     * @param dbTimestamp Timestamp in DATABASE_TIMESTAMP_FORMAT.
     * @return Formatted display string.
     */
    public String formatDbTimestampToDisplay(String dbTimestamp) {
        if (dbTimestamp == null || dbTimestamp.isEmpty()) return "";
        try {
            return LocalDateTime.parse(dbTimestamp, DB_TIMESTAMP_FORMATTER)
                    .format(DISPLAY_TIMESTAMP_FORMATTER);
        } catch (Exception e) {
            return dbTimestamp;
        }
    }

    /**
     * Calculates age based on a birthdate string.
     *
     * @param birthDateStr Birthdate in DATABASE_DATE_FORMAT.
     * @return Age in years, or -1 on failure.
     */
    public int calculateAge(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.isEmpty()) return -1;
        try {
            LocalDate birthDate = LocalDate.parse(birthDateStr, DB_DATE_FORMATTER);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Interface for date selection callback.
     */
    public interface OnDateSelectedListener {
        /**
         * Called when a date is selected.
         *
         * @param dateMillis    Epoch milliseconds.
         * @param dbDate        Date in DATABASE_DATE_FORMAT.
         * @param formattedDate Date in display format.
         */
        void onDateSelected(long dateMillis, String dbDate, String formattedDate);
    }

    /**
     * Interface for time selection callback.
     */
    public interface OnTimeSelectedListener {
        /**
         * Called when a time is selected.
         *
         * @param hour          Selected hour.
         * @param minute        Selected minute.
         * @param formattedTime Time in "HH:mm" format.
         */
        void onTimeSelected(int hour, int minute, String formattedTime);
    }
}