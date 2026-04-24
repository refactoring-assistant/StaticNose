package calendar.util;

import calendar.exception.InvalidDateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Parses date/time strings into LocalDateTime objects.
 * Handles formats: YYYY-MM-DD, HH:mm, YYYY-MM-DDThh:mm.
 * All operations assume EST (America/New_York) timezone as per requirements.
 */
public class DateTimeParser {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final ZoneId EST_ZONE = ZoneId.of("America/New_York");

  /**
   * Private constructor to prevent instantiation.
   * This is a utility class with only static methods.
   */
  private DateTimeParser() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Parses a date string in format YYYY-MM-DD.
   *
   * @param dateString the date string to parse (e.g., "2025-05-05")
   * @return the parsed LocalDate
   * @throws InvalidDateTimeException if format is invalid or date doesn't exist
   */
  public static LocalDate parseDate(String dateString)
      throws InvalidDateTimeException {
    if (dateString == null || dateString.trim().isEmpty()) {
      throw new InvalidDateTimeException("Date string cannot be null or empty");
    }

    try {
      return LocalDate.parse(dateString.trim(), DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new InvalidDateTimeException(
          "Invalid date format. Expected: YYYY-MM-DD (e.g., 2025-05-05), got: "
              + dateString, e);
    }
  }

  /**
   * Parses a time string in format HH:mm (24-hour format).
   *
   * @param timeString the time string to parse (e.g., "14:30")
   * @return the parsed LocalTime
   * @throws InvalidDateTimeException if format is invalid
   */
  public static LocalTime parseTime(String timeString)
      throws InvalidDateTimeException {
    if (timeString == null || timeString.trim().isEmpty()) {
      throw new InvalidDateTimeException("Time string cannot be null or empty");
    }

    try {
      return LocalTime.parse(timeString.trim(), TIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new InvalidDateTimeException(
          "Invalid time format. Expected: HH:mm (e.g., 14:30), got: "
              + timeString, e);
    }
  }

  /**
   * Parses a date-time string in format YYYY-MM-DDThh:mm.
   * The 'T' separator is required between date and time.
   *
   * @param dateTimeString the date-time string to parse (e.g., "2025-05-05T14:30")
   * @return the parsed LocalDateTime
   * @throws InvalidDateTimeException if format is invalid
   */
  public static LocalDateTime parseDateTime(String dateTimeString)
      throws InvalidDateTimeException {
    if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
      throw new InvalidDateTimeException("DateTime string cannot be null or empty");
    }

    try {
      return LocalDateTime.parse(dateTimeString.trim(), DATE_TIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new InvalidDateTimeException(
          "Invalid date-time format. Expected: YYYY-MM-DDThh:mm "
              + "(e.g., 2025-05-05T14:30), got: " + dateTimeString, e);
    }
  }

  /**
   * Combines a date string and time string into a LocalDateTime.
   * Useful when parsing commands that provide date and time separately.
   *
   * @param dateString the date string (YYYY-MM-DD)
   * @param timeString the time string (HH:mm)
   * @return the combined LocalDateTime
   * @throws InvalidDateTimeException if either format is invalid
   */
  public static LocalDateTime parseDateTime(String dateString, String timeString)
      throws InvalidDateTimeException {
    LocalDate date = parseDate(dateString);
    LocalTime time = parseTime(timeString);
    return LocalDateTime.of(date, time);
  }

  /**
   * Gets the EST timezone used by the application.
   * All date/time operations are assumed to be in EST as per requirements.
   *
   * @return the ZoneId for EST (America/New_York)
   */
  public static ZoneId getEstZone() {
    return EST_ZONE;
  }

  /**
   * Formats a LocalDate to string in YYYY-MM-DD format.
   *
   * @param date the date to format
   * @return formatted date string
   */
  public static String formatDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    return date.format(DATE_FORMAT);
  }

  /**
   * Formats a LocalTime to string in HH:mm format.
   *
   * @param time the time to format
   * @return formatted time string
   */
  public static String formatTime(LocalTime time) {
    if (time == null) {
      throw new IllegalArgumentException("Time cannot be null");
    }
    return time.format(TIME_FORMAT);
  }

  /**
   * Formats a LocalDateTime to string in YYYY-MM-DDThh:mm format.
   *
   * @param dateTime the date-time to format
   * @return formatted date-time string
   */
  public static String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    return dateTime.format(DATE_TIME_FORMAT);
  }

  /**
   * Validates that a date string matches the expected format without parsing.
   * Useful for quick validation before attempting full parse.
   *
   * @param dateString the date string to validate
   * @return true if format appears valid, false otherwise
   */
  public static boolean isValidDateFormat(String dateString) {
    if (dateString == null) {
      return false;
    }
    return dateString.matches("\\d{4}-\\d{2}-\\d{2}");
  }

  /**
   * Validates that a time string matches the expected format without parsing.
   *
   * @param timeString the time string to validate
   * @return true if format appears valid, false otherwise
   */
  public static boolean isValidTimeFormat(String timeString) {
    if (timeString == null) {
      return false;
    }
    return timeString.matches("\\d{2}:\\d{2}");
  }

  /**
   * Validates that a date-time string matches the expected format without parsing.
   *
   * @param dateTimeString the date-time string to validate
   * @return true if format appears valid, false otherwise
   */
  public static boolean isValidDateTimeFormat(String dateTimeString) {
    if (dateTimeString == null) {
      return false;
    }
    return dateTimeString.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}");
  }
}