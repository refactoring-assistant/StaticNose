package calendar.model.util;

import calendar.exceptions.InvalidDateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for parsing date and time strings into ZonedDateTime objects.
 *
 * <p>Supports two input formats:
 * - DateTime: YYYY-MM-DDThh:mm (ISO local datetime)
 * - Date: YYYY-MM-DD (ISO local date, returns midnight)
 *
 * <p>All parsing requires an explicit timezone to create ZonedDateTime objects.
 * This ensures events are always created in the correct timezone context.
 *
 * <p>This class is stateless and thread-safe. All methods are static.
 */
public final class DateTimeParser {

  /**
   * Parses a datetime string in format YYYY-MM-DDThh:mm to ZonedDateTime.
   *
   * <p>Example: "2025-05-05T10:30" with timezone America/New_York
   * becomes 2025-05-05T10:30:00-04:00[America/New_York]
   *
   * @param dateTimeStr the datetime string to parse (YYYY-MM-DDThh:mm)
   * @param timezone the timezone to interpret the datetime in
   * @return ZonedDateTime in the specified timezone
   * @throws InvalidDateTimeException if format is invalid
   * @throws NullPointerException if either parameter is null
   */
  public static ZonedDateTime parseDateTime(String dateTimeStr, ZoneId timezone)
      throws InvalidDateTimeException {

    if (dateTimeStr == null) {
      throw new NullPointerException("DateTime string cannot be null");
    }
    if (timezone == null) {
      throw new NullPointerException("Timezone cannot be null");
    }

    try {
      DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
      LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
      return localDateTime.atZone(timezone);
    } catch (DateTimeParseException e) {
      throw new InvalidDateTimeException(
          "Invalid datetime format: " + dateTimeStr + ". Expected YYYY-MM-DDThh:mm", e
      );
    }
  }

  /**
   * Parses a date string in format YYYY-MM-DD to ZonedDateTime at midnight.
   *
   * <p>Example: "2025-05-05" with timezone America/New_York
   * becomes 2025-05-05T00:00:00-04:00[America/New_York]
   *
   * <p>Used for all-day events and date-only queries.
   *
   * @param dateStr the date string to parse (YYYY-MM-DD)
   * @param timezone the timezone to interpret the date in
   * @return ZonedDateTime at midnight (00:00:00) in the specified timezone
   * @throws InvalidDateTimeException if format is invalid
   * @throws NullPointerException if either parameter is null
   */
  public static ZonedDateTime parseDate(String dateStr, ZoneId timezone)
      throws InvalidDateTimeException {

    if (dateStr == null) {
      throw new NullPointerException("Date string cannot be null");
    }
    if (timezone == null) {
      throw new NullPointerException("Timezone cannot be null");
    }

    try {
      DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
      LocalDate localDate = LocalDate.parse(dateStr, formatter);
      return localDate.atStartOfDay(timezone);
    } catch (DateTimeParseException e) {
      throw new InvalidDateTimeException(
          "Invalid date format: " + dateStr + ". Expected YYYY-MM-DD", e
      );
    }
  }

  /**
   * Parses a date string in format YYYY-MM-DD to LocalDate.
   *
   * <p>This is a convenience method for cases where LocalDate is needed
   * instead of ZonedDateTime (e.g., for map keys based on date).
   *
   * @param dateStr the date string to parse (YYYY-MM-DD)
   * @return LocalDate
   * @throws InvalidDateTimeException if format is invalid
   * @throws NullPointerException if dateStr is null
   */
  public static LocalDate parseDateToLocalDate(String dateStr)
      throws InvalidDateTimeException {

    if (dateStr == null) {
      throw new NullPointerException("Date string cannot be null");
    }

    try {
      return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      throw new InvalidDateTimeException(
          "Invalid date format: " + dateStr + ". Expected YYYY-MM-DD", e
      );
    }
  }

  /**
   * Formats a ZonedDateTime to string in format YYYY-MM-DDThh:mm.
   *
   * <p>Example: 2025-05-05T10:30:00-04:00[America/New_York]
   * becomes "2025-05-05T10:30"
   *
   * <p>This format is compatible with parseDateTime() and matches
   * the ISO local datetime format used throughout the calendar system.
   *
   * @param dateTime the datetime to format
   * @return formatted string in YYYY-MM-DDThh:mm format
   * @throws NullPointerException if dateTime is null
   */
  public static String formatDateTime(ZonedDateTime dateTime) {
    if (dateTime == null) {
      throw new NullPointerException("DateTime cannot be null");
    }
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  /**
   * Formats a ZonedDateTime for command parsing (without seconds).
   * Used when creating command strings that will be parsed by CommandParser.
   *
   * @param dateTime the datetime to format
   * @return formatted string in YYYY-MM-DDThh:mm format
   */
  public static String formatDateTimeForCommand(ZonedDateTime dateTime) {
    if (dateTime == null) {
      throw new NullPointerException("DateTime cannot be null");
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    return dateTime.format(formatter);
  }
}