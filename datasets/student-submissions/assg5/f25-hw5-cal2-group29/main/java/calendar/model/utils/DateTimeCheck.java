package calendar.model.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for parsing and validating date/time formats used in calendar commands.
 * Supports: YYYY-MM-DD (date), HH:mm (time), and YYYY-MM-DDThh:mm (date-time) formats.
 * Includes validation for event timing and all-day event defaults (8:00 AM - 5:00 PM).
 */
public class DateTimeCheck {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");


  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");


  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Parses a date string in the format "YYYY-MM-DD".
   *
   * @param dateString the date string to parse
   * @return the corresponding {@link LocalDate}
   * @throws IllegalArgumentException if the input string is invalid
   */
  public static LocalDate parseDate(String dateString) {
    try {
      return LocalDate.parse(dateString, DATE_FORMATTER);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Invalid date format: " + dateString + ". Expected format: YYYY-MM-DD");
    }
  }

  /**
   * Parses a time string in the format "HH:mm".
   *
   * @param timeString the time string to parse
   * @return the corresponding {@link LocalTime}
   * @throws IllegalArgumentException if the input string is invalid
   */
  public static LocalTime parseTime(String timeString) {
    try {
      return LocalTime.parse(timeString, TIME_FORMATTER);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Invalid time format: " + timeString + ". Expected format: HH:mm");
    }
  }

  /**
   * Parses a date-time string in the format "YYYY-MM-DDThh:mm".
   *
   * @param dateTimeString the date-time string to parse
   * @return the corresponding {@link LocalDateTime}
   * @throws IllegalArgumentException if the input string is invalid
   */
  public static LocalDateTime parseDateTime(String dateTimeString) {
    try {
      return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Invalid date-time format: " + dateTimeString
              + ". Expected format: YYYY-MM-DDThh:mm");
    }
  }

  /**
   * Validates that a start time occurs strictly before an end time.
   *
   * @param start the start date-time
   * @param end   the end date-time
   * @throws IllegalArgumentException if the start is not before the end
   */
  public static void validateStartBeforeEnd(LocalDateTime start, LocalDateTime end) {
    if (!start.isBefore(end)) {
      throw new IllegalArgumentException(
          "Start time must be before end time. Start: " + start + ", End: " + end);
    }
  }

  /**
   * Validates that two date-times occur on the same calendar day.
   * Typically used for validating events in a series.
   *
   * @param start the start date-time
   * @param end   the end date-time
   * @throws IllegalArgumentException if the two times span multiple days
   */
  public static void validateSingleDayEvent(LocalDateTime start, LocalDateTime end) {
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException(
          "Event must start and end on the same day. Start: "
              + start.toLocalDate() + ", End: " + end.toLocalDate());
    }
  }

  /**
   * Determines whether the given time falls within the standard all-day event range.
   *
   * @param dateTime the date-time to check
   * @return true if the time is between 08:00 and 17:00 (exclusive)
   */
  public static boolean isAllDayEventTime(LocalDateTime dateTime) {
    int hour = dateTime.getHour();
    return hour >= 8 && hour < 17;
  }

  /**
   * Returns a default end time (5:00 PM) for all-day events
   * on the same day as the provided start time.
   *
   * @param start the start date-time
   * @return the end date-time at 5:00 PM on the same day
   */
  public static LocalDateTime createDefaultEndTime(LocalDateTime start) {
    return start.toLocalDate().atTime(17, 0);
  }

  /**
   * Creates a standard start time (8:00 AM) for an all-day event
   * on the given date.
   *
   * @param date the event date
   * @return a {@link LocalDateTime} representing 8:00 AM on the given date
   */
  public static LocalDateTime createAllDayStartTime(LocalDate date) {
    return date.atTime(8, 0);
  }

  /**
   * Creates a standard end time (5:00 PM) for an all-day event
   * on the given date.
   *
   * @param date the event date
   * @return a {@link LocalDateTime} representing 5:00 PM on the given date
   */
  public static LocalDateTime createAllDayEndTime(LocalDate date) {
    return date.atTime(17, 0);
  }

  private DateTimeCheck() {
    throw new UnsupportedOperationException("Utility class – cannot be instantiated.");
  }
}
