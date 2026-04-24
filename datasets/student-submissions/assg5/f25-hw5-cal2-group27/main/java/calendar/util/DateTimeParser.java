package calendar.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for parsing and formatting dates and times.
 */
public class DateTimeParser {

  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Parses a string in the format "YYYY-MM-DDTHH:mm" into a ZonedDateTime object.
   *
   * @param dateTimeString The string to parse.
   * @param zoneId The time zone to use.
   * @return The parsed ZonedDateTime object.
   */
  public static ZonedDateTime parseDateTime(String dateTimeString, ZoneId zoneId) {
    LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
    return ZonedDateTime.of(localDateTime, zoneId);
  }

  /**
   * Parses a string in the format "YYYY-MM-DD" into a LocalDate object.
   *
   * @param dateString The string to parse.
   * @return The parsed LocalDate object.
   */
  public static LocalDate parseDate(String dateString) {
    return LocalDate.parse(dateString, DATE_FORMATTER);
  }

  /**
   * Parses a string of characters into a set of DayOfWeek enums.
   *
   * @param weekdaysString The string representation of weekdays.
   * @return A set of DayOfWeek enums.
   * @throws IllegalArgumentException if the string contains invalid characters.
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdaysString) {
    Set<DayOfWeek> weekdays = new HashSet<>();
    for (char dayChar : weekdaysString.toUpperCase().toCharArray()) {
      switch (dayChar) {
        case 'M':
          weekdays.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          weekdays.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          weekdays.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          weekdays.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          weekdays.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          weekdays.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          weekdays.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + dayChar);
      }
    }
    return weekdays;
  }
}
