package calendar.model.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Immutable wrapper for LocalDateTime that provides convenient parsing and formatting.
 * This record ensures consistent date/time handling throughout the application.
 */
public class DateTime {

  // Standard format patterns used in the application
  public static final String DATE_FORMAT = "yyyy-MM-dd";
  public static final String TIME_FORMAT = "HH:mm";
  public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm";

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern(DATETIME_FORMAT);

  private final LocalDateTime dateTime;

  /**
   * Validates the dateTime parameter.
   */
  public DateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    this.dateTime = dateTime;
  }

  /**
   * Gets the underlying LocalDateTime.
   *
   * @return the LocalDateTime
   */
  public LocalDateTime dateTime() {
    return dateTime;
  }

  /**
   * Creates a DateTime from separate date and time components.
   *
   * @param date the date
   * @param time the time
   * @return a new DateTime instance
   */
  public static DateTime of(LocalDate date, LocalTime time) {
    if (date == null || time == null) {
      throw new IllegalArgumentException("Date and time cannot be null");
    }
    return new DateTime(LocalDateTime.of(date, time));
  }

  /**
   * Creates a DateTime from date and time strings.
   *
   * @param dateString date in format "yyyy-MM-dd"
   * @param timeString time in format "HH:mm"
   * @return a new DateTime instance
   * @throws DateTimeParseException if parsing fails
   */
  public static DateTime parse(String dateString, String timeString) {
    LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
    LocalTime time = LocalTime.parse(timeString, TIME_FORMATTER);
    return DateTime.of(date, time);
  }

  /**
   * Creates a DateTime from a combined date-time string.
   *
   * @param dateTimeString date-time in format "yyyy-MM-dd'T'HH:mm"
   * @return a new DateTime instance
   * @throws DateTimeParseException if parsing fails
   */
  public static DateTime parse(String dateTimeString) {
    if (dateTimeString == null) {
      throw new DateTimeParseException(
          "Invalid datetime format. Expected: " + DATETIME_FORMAT + " or ISO format",
          "", 0);
    }

    if (dateTimeString.trim().isEmpty()) {
      throw new DateTimeParseException(
          "Invalid datetime format. Expected: " + DATETIME_FORMAT + " or ISO format",
          dateTimeString, 0);
    }

    try {
      // First try standard format with T separator (yyyy-MM-dd'T'HH:mm)
      return new DateTime(LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER));
    } catch (DateTimeParseException e1) {
      try {
        // Try ISO format
        return new DateTime(LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME));
      } catch (DateTimeParseException e2) {
        // Both formats failed - throw descriptive error
        throw new DateTimeParseException(
            "Invalid datetime format. Expected: " + DATETIME_FORMAT + " or ISO format",
            dateTimeString, 0);
      }
    }
  }

  /**
   * Gets the date component.
   *
   * @return the date part
   */
  public LocalDate getDate() {
    return dateTime.toLocalDate();
  }

  /**
   * Gets the time component.
   *
   * @return the time part
   */
  public LocalTime getTime() {
    return dateTime.toLocalTime();
  }

  /**
   * Checks if this DateTime is before another.
   *
   * @param other the other DateTime
   * @return true if this is before other
   */
  public boolean isBefore(DateTime other) {
    return dateTime.isBefore(other.dateTime);
  }

  /**
   * Checks if this DateTime is after another.
   *
   * @param other the other DateTime
   * @return true if this is after other
   */
  public boolean isAfter(DateTime other) {
    return dateTime.isAfter(other.dateTime);
  }

  /**
   * Checks if this DateTime is equal to another.
   *
   * @param other the other DateTime
   * @return true if equal
   */
  public boolean isEqual(DateTime other) {
    return dateTime.isEqual(other.dateTime);
  }

  /**
   * Checks if this DateTime is on the same date as another.
   *
   * @param other the other DateTime
   * @return true if on the same date
   */
  public boolean isSameDate(DateTime other) {
    return getDate().equals(other.getDate());
  }

  /**
   * Adds days to this DateTime.
   *
   * @param days number of days to add
   * @return a new DateTime instance
   */
  public DateTime plusDays(long days) {
    return new DateTime(dateTime.plusDays(days));
  }

  /**
   * Adds weeks to this DateTime.
   *
   * @param weeks number of weeks to add
   * @return a new DateTime instance
   */
  public DateTime plusWeeks(long weeks) {
    return new DateTime(dateTime.plusWeeks(weeks));
  }

  /**
   * Adds hours to this DateTime.
   *
   * @param hours number of hours to add
   * @return a new DateTime instance
   */
  public DateTime plusHours(long hours) {
    return new DateTime(dateTime.plusHours(hours));
  }

  /**
   * Adds minutes to this DateTime.
   *
   * @param minutes number of minutes to add
   * @return a new DateTime instance
   */
  public DateTime plusMinutes(long minutes) {
    return new DateTime(dateTime.plusMinutes(minutes));
  }

  /**
   * Gets the day of week.
   *
   * @return the day of week
   */
  public java.time.DayOfWeek getDayOfWeek() {
    return dateTime.getDayOfWeek();
  }

  /**
   * Formats the date component as a string.
   *
   * @return date string in format "yyyy-MM-dd"
   */
  public String formatDate() {
    return getDate().format(DATE_FORMATTER);
  }

  /**
   * Formats the time component as a string.
   *
   * @return time string in format "HH:mm"
   */
  public String formatTime() {
    return getTime().format(TIME_FORMATTER);
  }

  /**
   * Formats the full date-time as a string.
   *
   * @return date-time string in format "yyyy-MM-dd'T'HH:mm"
   */
  public String formatDateTime() {
    return dateTime.format(DATETIME_FORMATTER);
  }

  /**
   * Converts to LocalDateTime.
   *
   * @return the underlying LocalDateTime
   */
  public LocalDateTime toLocalDateTime() {
    return dateTime;
  }

  @Override
  public String toString() {
    return formatDateTime();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    DateTime other = (DateTime) obj;
    return Objects.equals(dateTime, other.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime);
  }

  /**
   * Creates an all-day event start time (8:00 AM) for the given date.
   *
   * @param date the date
   * @return DateTime at 8:00 AM on the given date
   */
  public static DateTime allDayStart(LocalDate date) {
    return DateTime.of(date, LocalTime.of(8, 0));
  }

  /**
   * Creates an all-day event end time (5:00 PM) for the given date.
   *
   * @param date the date
   * @return DateTime at 5:00 PM on the given date
   */
  public static DateTime allDayEnd(LocalDate date) {
    return DateTime.of(date, LocalTime.of(17, 0));
  }
}