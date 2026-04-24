package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.enums.Location;
import calendarview.CalendarView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An abstract base class for {@link CalendarCommand} implementations.
 *
 * <p>This class provides common utility methods for parsing date/time strings,
 * weekdays, and quoted strings, which are shared by many concrete
 * command classes.</p>
 */
public abstract class AbstractCalendarCommand implements CalendarCommand {

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

  /**
   * Parses a raw string value into the correct object type based on the
   * property being edited.
   *
   * @param property    The name of the property (e.g., "start", "subject", "location").
   * @param valueString The raw string value from the user command.
   * @return A {@link LocalDateTime}, {@link Location}, or {@link String} object.
   * @throws IllegalArgumentException if the property name is unknown or
   *                                  the location value is invalid.
   */
  protected Object parseNewValue(String property, String valueString) {
    switch (property.toLowerCase()) {
      case "start":
      case "end":
        return parseDateTime(unquote(valueString));
      case "subject":
      case "description":
      case "status":
        return unquote(valueString);
      case "location":
        String unquotedValue = unquote(valueString).toUpperCase();
        try {
          return Location.valueOf(unquotedValue);
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(
              "Invalid location. Must be 'PHYSICAL' or 'ONLINE'.");
        }
      default:
        throw new IllegalArgumentException("Property is unknown: " + property);
    }
  }

  /**
   * Parses a string of weekday characters (e.g., "MWF") into a list
   * of {@link DayOfWeek} enums.
   *
   * @param input The string of weekday initials (M, T, W, R, F, S, U).
   * @return A list of {@link DayOfWeek} enums.
   * @throws IllegalArgumentException if any character is not a valid weekday initial.
   */
  protected List<DayOfWeek> parseWeekdays(String input) {
    List<DayOfWeek> weekdays = new ArrayList<>();
    for (char c : input.toUpperCase().toCharArray()) {
      switch (c) {
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
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }
    return weekdays;
  }

  @Override
  public boolean execute(String inputLine, CalendarModel model, CalendarView view) {
    if (model == null) {
      return this.matches(inputLine);
    }
    return this.executeWithModel(inputLine, model, view);
  }

  /**
   * Abstract method for the regex pattern matching.
   *
   * @param inputLine The user input line.
   * @return true if the pattern matches, false otherwise.
   */
  protected abstract boolean matches(String inputLine);

  /**
   * The actual command logic to execute when a model is present.
   *
   * @param inputLine The user input line.
   * @param model     The (non-null) calendar model.
   * @param view      The view.
   * @return true if the command executed, false if the pattern didn't match.
   */
  protected abstract boolean executeWithModel(String inputLine, CalendarModel model,
                                              CalendarView view);
}
