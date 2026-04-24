package calendar.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for parsing date and time strings.
 * This logic was extracted from CalModel.
 */
public class DateTimeUtil {

  private static final DateTimeFormatter QUERY_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter QUERY_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Parses a date string in yyyy-MM-dd format.
   *
   * @param dateString The string to parse.
   * @return A {@link LocalDate} object.
   * @throws IllegalArgumentException if parsing fails.
   */
  public static LocalDate parseDate(String dateString) {
    try {
      return LocalDate.parse(dateString, QUERY_DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date format for '" + dateString + "'. Expected yyyy-MM-dd.", e);
    }
  }

  /**
   * Parses a datetime string in yyyy-MM-ddTHH:mm format.
   *
   * @param dateTimeString The string to parse.
   * @return A {@link LocalDateTime} object.
   * @throws IllegalArgumentException if parsing fails.
   */
  public static LocalDateTime parseDateTime(String dateTimeString) {
    try {
      String isoString = dateTimeString.replace(' ', 'T');
      return LocalDateTime.parse(isoString, QUERY_DATETIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid datetime format for '" + dateTimeString + "'. Expected yyyy-MM-ddTHH:mm.", e);
    }
  }
}