package calendar.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Utility methods for CommandParser.
 * Handles parsing of dates, times, weekdays, and subject extraction.
 */
public class CommandParserUtils {
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Extracts subject from matcher, handling both quoted and unquoted subjects.
   *
   * @param matcher the regex matcher containing the subject
   * @param quotedIndex the group index for quoted subjects
   * @param unquotedIndex the group index for unquoted subjects
   * @return the extracted subject string
   */
  public static String extractSubject(Matcher matcher, int quotedIndex, int unquotedIndex) {
    return matcher.group(quotedIndex) != null
        ? matcher.group(quotedIndex) : matcher.group(unquotedIndex);
  }

  /**
   * Parses a date string in YYYY-MM-DD format.
   *
   * @param dateStr the date string to parse
   * @return the parsed LocalDate
   * @throws IllegalArgumentException if the date format is invalid
   */
  public static LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + dateStr
          + ". Expected YYYY-MM-DD");
    }
  }

  /**
   * Parses a date-time string in YYYY-MM-DDTHH:mm format.
   *
   * @param dateTimeStr the date-time string to parse
   * @return the parsed LocalDateTime
   * @throws IllegalArgumentException if the date-time format is invalid
   */
  public static LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, DATETIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + dateTimeStr
          + ". Expected YYYY-MM-DDTHH:mm");
    }
  }

  /**
   * Parses a weekday string into a set of DayOfWeek objects.
   * M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday.
   *
   * @param weekdayStr the weekday string to parse
   * @return set of DayOfWeek objects
   * @throws IllegalArgumentException if an invalid character is found
   */
  public static Set<DayOfWeek> parseDays(String weekdayStr) {
    Set<DayOfWeek> days = new HashSet<>();
    for (char c : weekdayStr.toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid day character: " + c);
      }
    }
    return days;
  }
}