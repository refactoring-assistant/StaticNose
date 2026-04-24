package calendar.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Parsers for the given formats.
 */
public final class DateTimeParser {
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private DateTimeParser() {
  }

  /**
   * Parses a date string in the format YYYY-MM-DD.
   *
   * @param s date string to parse
   * @return LocalDate representing the parsed date
   */
  public static LocalDate parseDate(String s) {
    return LocalDate.parse(s.trim(), DATE);
  }

  /**
   * Parses a date-time string in the format YYYY-MM-DDThh:mm.
   *
   * @param s date-time string to parse
   * @return LocalDateTime representing the parsed date and time
   */
  public static LocalDateTime parseDateTime(String s) {
    return LocalDateTime.parse(s.trim(), DATETIME);
  }
}