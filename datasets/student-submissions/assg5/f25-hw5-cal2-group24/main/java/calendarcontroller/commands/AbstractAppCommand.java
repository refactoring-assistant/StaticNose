package calendarcontroller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * An abstract base class for {@link AppCommand} implementations.
 *
 * <p>This class provides common utility methods for parsing date/time strings,
 * and quoted strings, which are shared by many concrete command classes.</p>
 */
public abstract class AbstractAppCommand implements AppCommand {

  /**
   * The formatter for "YYYY-MM-DDTHH:MM" datetime strings.
   */
  protected static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  
  /**
   * The formatter for "YYYY-MM-DD" date strings.
   */
  protected static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Regex flags to ensure case-insensitive matching for commands.
   */
  protected static final int REGEX_FLAGS = Pattern.CASE_INSENSITIVE;

  /**
   * Removes surrounding quotes from a string, if they exist.
   *
   * @param input The string to unquote.
   * @return The string without surrounding quotes, or the original
   *         string if not quoted.
   */
  protected String unquote(String input) {
    if (input.startsWith("\"") && input.endsWith("\"")) {
      return input.substring(1, input.length() - 1);
    }
    return input;
  }

  /**
   * Parses a string into a {@link LocalDateTime} using the standard format.
   *
   * @param input The string to parse (e.g., "2024-10-31T13:00").
   * @return The corresponding {@link LocalDateTime}.
   * @throws DateTimeParseException if the string cannot be parsed.
   */
  protected LocalDateTime parseDateTime(String input) throws DateTimeParseException {
    return LocalDateTime.parse(input, DATE_TIME_FORMATTER);
  }

  /**
   * Parses a string into a {@link LocalDate} using the standard format.
   *
   * @param input The string to parse (e.g., "2024-10-31").
   * @return The corresponding {@link LocalDate}.
   * @throws DateTimeParseException if the string cannot be parsed.
   */
  protected LocalDate parseDate(String input) throws DateTimeParseException {
    return LocalDate.parse(input, DATE_FORMATTER);
  }

}
