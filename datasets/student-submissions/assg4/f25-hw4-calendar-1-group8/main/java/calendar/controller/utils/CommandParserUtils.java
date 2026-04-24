package calendar.controller.utils;

import calendar.model.CalendarEvent;
import calendar.model.EventLocation;
import calendar.model.EventStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class that contains all parsing and helper logic for commands.
 */
public class CommandParserUtils {

  private final ZoneId timeZone;
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
  private final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Creates a new utility parser with the given time zone.
   *
   * @param timeZone The time zone to use for date/time parsing.
   */
  public CommandParserUtils(ZoneId timeZone) {
    this.timeZone = timeZone;
  }

  /**
   * Builds a new event by cloning an old event and changing one property.
   *
   * @param oldEvent The original event to clone.
   * @param property The name of the property to change (e.g., "subject", "start").
   * @param newValue The new value for the property.
   * @return A new CalendarEvent instance with the updated property.
   * @throws Exception If the property name is unknown or the new value is invalid.
   */
  public CalendarEvent buildUpdatedEvent(CalendarEvent oldEvent, String property, String newValue)
      throws Exception {

    CalendarEvent.CalendarEventBuilder builder =
        new CalendarEvent.CalendarEventBuilder(oldEvent.getSubject(), oldEvent.getStart())
            .withEnd(oldEvent.getEnd())
            .withDescription(oldEvent.getDescription())
            .withLocation(oldEvent.getLocation())
            .withStatus(oldEvent.getStatus());

    switch (property) {
      case "subject":
        builder = new CalendarEvent.CalendarEventBuilder(newValue, oldEvent.getStart())
            .withEnd(oldEvent.getEnd())
            .withDescription(oldEvent.getDescription())
            .withLocation(oldEvent.getLocation())
            .withStatus(oldEvent.getStatus());
        break;
      case "start":
        ZonedDateTime newStart = parseDateTimeToZonedDateTime(newValue);
        builder = new CalendarEvent.CalendarEventBuilder(oldEvent.getSubject(), newStart)
            .withEnd(oldEvent.getEnd())
            .withDescription(oldEvent.getDescription())
            .withLocation(oldEvent.getLocation())
            .withStatus(oldEvent.getStatus());
        break;
      case "end":
        builder.withEnd(parseDateTimeToZonedDateTime(newValue));
        break;
      case "description":
        builder.withDescription(newValue);
        break;
      case "location":
        EventLocation newLocation = EventLocation.fromString(newValue);
        builder.withLocation(newLocation);
        break;
      case "status":
        builder.withStatus(parseStatus(newValue));
        break;
      default:
        throw new Exception("Unknown property to edit: '" + property + "'");
    }
    return builder.build();
  }

  /**
   * Removes surrounding double quotes from a string.
   *
   * @param token The string to strip.
   * @return The string without surrounding quotes.
   */
  public String stripQuotes(String token) {
    if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
      return token.substring(1, token.length() - 1);
    }
    return token;
  }

  /**
   * Parses a "YYYY-MM-DDTHH:MM" string into a ZonedDateTime.
   *
   * @param token The date/time string to parse.
   * @return The corresponding ZonedDateTime.
   * @throws Exception If the string format is invalid.
   */
  public ZonedDateTime parseDateTimeToZonedDateTime(String token) throws Exception {
    try {
      LocalDateTime ldt = LocalDateTime.parse(token, dateTimeFormatter);
      return ldt.atZone(timeZone);
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date/time format: '"
          + token + "'. Expected format: YYYY-MM-DDTHH:MM");
    }
  }

  /**
   * Parses a "YYYY-MM-DD" string into a ZonedDateTime at 8:00 AM.
   *
   * @param token The date string to parse.
   * @return The corresponding ZonedDateTime at 8:00 AM.
   * @throws Exception If the string format is invalid.
   */
  public ZonedDateTime parseDateToZonedDateTime(String token) throws Exception {
    try {
      LocalDate ld = LocalDate.parse(token, dateFormatter);
      return ld.atTime(8, 0).atZone(timeZone);
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date format: '" + token + "'. Expected format: YYYY-MM-DD");
    }
  }

  /**
   * Parses a "YYYY-MM-DD" string into a LocalDate.
   *
   * @param token The date string to parse.
   * @return The corresponding LocalDate.
   * @throws Exception If the string format is invalid.
   */
  public LocalDate parseDate(String token) throws Exception {
    try {
      return LocalDate.parse(token, dateFormatter);
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date format: '" + token + "'. Expected format: YYYY-MM-DD");
    }
  }

  /**
   * Parses a string into a positive integer for "occurrences".
   *
   * @param token The string to parse.
   * @return A positive integer.
   * @throws Exception If the string is not a positive number.
   */
  public int parseOccurrences(String token) throws Exception {
    try {
      int occurrences = Integer.parseInt(token);
      if (occurrences <= 0) {
        throw new Exception("Occurrences must be a positive number.");
      }
      return occurrences;
    } catch (NumberFormatException e) {
      throw new Exception("Invalid number of occurrences: '" + token + "' must be a whole number.");
    }
  }

  /**
   * Parses a string (e.g., "MTW") into a set of DayOfWeek enums.
   *
   * @param token The string of weekday characters.
   * @return A set of DayOfWeek enums.
   * @throws Exception If the string is empty or contains invalid characters.
   */
  public Set<DayOfWeek> parseWeekdays(String token) throws Exception {
    Set<DayOfWeek> weekdays = new HashSet<>();
    for (char c : token.toCharArray()) {
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
          throw new Exception(
              "Invalid weekday character: '" + c + "'. Use only M, T, W, R, F, S, U.");
      }
    }
    if (weekdays.isEmpty()) {
      throw new Exception("You must specify at least one weekday for a recurring event.");
    }
    return weekdays;
  }

  /**
   * Parses a string into an EventStatus enum ("public" or "private").
   *
   * @param token The status string.
   * @return The corresponding EventStatus.
   * @throws Exception If the status is not "public" or "private".
   */
  public EventStatus parseStatus(String token) throws Exception {
    try {
      return EventStatus.fromString(token.toLowerCase());
    } catch (IllegalArgumentException e) {
      throw new Exception("Invalid status: '" + token + "'. Must be 'public' or 'private'.");
    }
  }

  /**
   * Formats a ZonedDateTime to a "YYYY-MM-DD" string.
   *
   * @param zdt The date/time to format.
   * @return The formatted date string.
   */
  public String formatDate(ZonedDateTime zdt) {
    return zdt.format(dateFormatter);
  }

  /**
   * Formats a ZonedDateTime to a "HH:MM" time string.
   *
   * @param zdt The date/time to format.
   * @return The formatted time string.
   */
  public String formatTime(ZonedDateTime zdt) {
    return zdt.format(timeFormatter);
  }
}