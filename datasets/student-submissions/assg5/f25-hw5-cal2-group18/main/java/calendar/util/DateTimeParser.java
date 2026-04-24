package calendar.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for parsing date and time strings.
 * Date: YYYY-MM-DD
 * DateTime: YYYY-MM-DDThh:mm
 */
public class DateTimeParser {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Parses a date string in format YYYY-MM-DD.
   *
   * @param dateString the date string
   * @return the LocalDate
   * @throws IllegalArgumentException if format is invalid
   */
  public static LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      throw new IllegalArgumentException("Date string cannot be null or empty");
    }

    try {
      return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date format: " + dateString + ". Expected format: YYYY-MM-DD", e);
    }
  }

  /**
   * Parses a date/time string in format YYYY-MM-DDThh:mm.
   *
   * @param dateTimeString the date/time string
   * @return the LocalDateTime
   * @throws IllegalArgumentException if format is invalid
   */
  public static LocalDateTime parseDateTime(String dateTimeString) {
    if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
      throw new IllegalArgumentException("Date/time string cannot be null or empty");
    }

    try {
      return LocalDateTime.parse(dateTimeString.trim(), DATETIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date/time format: " + dateTimeString + ". Expected format: YYYY-MM-DDThh:mm", e);
    }
  }

  /**
   * Formats a LocalDate to string (YYYY-MM-DD).
   *
   * @param date the date
   * @return the formatted string
   */
  public static String formatDate(LocalDate date) {
    if (date == null) {
      return "";
    }
    return date.format(DATE_FORMATTER);
  }

  /**
   * Formats a LocalDateTime to string (YYYY-MM-DDThh:mm).
   *
   * @param dateTime the date/time
   * @return the formatted string
   */
  public static String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "";
    }
    return dateTime.format(DATETIME_FORMATTER);
  }

  /**
   * Formats a LocalDateTime for display (user-friendly format).
   *
   * @param dateTime the date/time
   * @return the formatted string
   */
  public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "";
    }
    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
    return dateTime.format(displayFormatter);
  }

  /**
   * Formats time only for display (hh:mm AM/PM).
   *
   * @param dateTime the date/time
   * @return the formatted time string
   */
  public static String formatTimeForDisplay(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "";
    }
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    return dateTime.format(timeFormatter);
  }
}