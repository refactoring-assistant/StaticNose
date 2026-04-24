package calendar.controller;

import calendar.model.MyEventSeries;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses command strings into Command objects.
 */
public class MyCommandInterpreter {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Parses a command string and returns the appropriate Command object.
   *
   * @param input the command string
   * @return the parsed Command
   * @throws IllegalArgumentException if command is invalid
   */
  public Command parseCommand(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    // Normalize whitespace: replace multiple spaces with single space
    String trimmed = input.trim().replaceAll("\\s+", " ");

    if (trimmed.equalsIgnoreCase("exit")) {
      return new ExitingTheCommand();
    }

    if (trimmed.startsWith("create event ")) {
      return parseCreateCommand(trimmed);
    }

    if (trimmed.startsWith("edit event ")
        || trimmed.startsWith("edit events ")
        || trimmed.startsWith("edit series ")) {
      return parseEditCommand(trimmed);
    }

    if (trimmed.startsWith("print events ")) {
      return parseQueryCommand(trimmed);
    }

    if (trimmed.startsWith("export cal ")) {
      return parseExportCommand(trimmed);
    }

    if (trimmed.startsWith("show status on ")) {
      return parseStatusCommand(trimmed);
    }

    throw new IllegalArgumentException("Invalid command: " + input);
  }

  private Command parseCreateCommand(String input) {
    String rest = input.substring(13).trim();

    String subject;
    String remainder;

    if (rest.startsWith("\"")) {
      int endQuote = rest.indexOf("\"", 1);
      if (endQuote == -1) {
        throw new IllegalArgumentException("Unclosed quote in subject");
      }
      subject = rest.substring(1, endQuote);
      remainder = rest.substring(endQuote + 1).trim();
    } else {
      int spaceIdx = rest.indexOf(" ");
      if (spaceIdx == -1) {
        throw new IllegalArgumentException("Invalid create command format");
      }
      subject = rest.substring(0, spaceIdx);
      remainder = rest.substring(spaceIdx + 1).trim();
    }

    if (remainder.startsWith("on ")) {
      return parseAllDayCreateCommand(subject, remainder.substring(3).trim());
    } else if (remainder.startsWith("from ")) {
      return parseTimedCreateCommand(subject, remainder.substring(5).trim());
    } else {
      throw new IllegalArgumentException("Expected 'on' or 'from' after subject");
    }
  }

  private Command parseAllDayCreateCommand(String subject, String rest) {
    Pattern singlePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})$");
    Pattern repeatTimesPattern = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times$");
    Pattern repeatUntilPattern = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})$");

    Matcher m;

    if ((m = singlePattern.matcher(rest)).matches()) {
      LocalDate date = parseDate(m.group(1));
      return new CreatingAnEventCommand(subject, date.atTime(8, 0), null, true, false);
    }

    if ((m = repeatTimesPattern.matcher(rest)).matches()) {
      LocalDate startDate = parseDate(m.group(1));
      Set<DayOfWeek> weekdays = MyEventSeries.parseWeekdays(m.group(2));
      int times = Integer.parseInt(m.group(3));
      return new CreatingAnEventCommand(subject, startDate.atTime(8, 0), null,
          weekdays, times, null, true);
    }

    if ((m = repeatUntilPattern.matcher(rest)).matches()) {
      LocalDate startDate = parseDate(m.group(1));
      Set<DayOfWeek> weekdays = MyEventSeries.parseWeekdays(m.group(2));
      LocalDate endDate = parseDate(m.group(3));
      return new CreatingAnEventCommand(subject, startDate.atTime(8, 0), null,
          weekdays, null, endDate, true);
    }

    throw new IllegalArgumentException("Invalid all-day event format");
  }

  private Command parseTimedCreateCommand(String subject, String rest) {
    String dateTimePattern = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}";
    Pattern singlePattern = Pattern.compile(
        "^(" + dateTimePattern + ") to (" + dateTimePattern + ")$");
    Pattern repeatTimesPattern = Pattern.compile(
        "^(" + dateTimePattern + ") to (" + dateTimePattern
            + ") repeats ([MTWRFSU]+) for (\\d+) times$");
    Pattern repeatUntilPattern = Pattern.compile(
        "^(" + dateTimePattern + ") to (" + dateTimePattern
            + ") repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})$");

    Matcher m;

    if ((m = singlePattern.matcher(rest)).matches()) {
      LocalDateTime start = parseDateTime(m.group(1));
      LocalDateTime end = parseDateTime(m.group(2));
      return new CreatingAnEventCommand(subject, start, end, false, false);
    }

    if ((m = repeatTimesPattern.matcher(rest)).matches()) {
      LocalDateTime start = parseDateTime(m.group(1));
      LocalDateTime end = parseDateTime(m.group(2));
      Set<DayOfWeek> weekdays = MyEventSeries.parseWeekdays(m.group(3));
      int times = Integer.parseInt(m.group(4));
      return new CreatingAnEventCommand(subject, start, end, weekdays, times, null, false);
    }

    if ((m = repeatUntilPattern.matcher(rest)).matches()) {
      LocalDateTime start = parseDateTime(m.group(1));
      LocalDateTime end = parseDateTime(m.group(2));
      Set<DayOfWeek> weekdays = MyEventSeries.parseWeekdays(m.group(3));
      LocalDate endDate = parseDate(m.group(4));
      return new CreatingAnEventCommand(subject, start, end, weekdays, null, endDate, false);
    }

    throw new IllegalArgumentException("Invalid timed event format");
  }

  private Command parseEditCommand(String input) {
    final EditType editType;
    String rest;

    if (input.startsWith("edit event ")) {
      editType = EditType.SINGLE;
      rest = input.substring(11).trim();
    } else if (input.startsWith("edit events ")) {
      editType = EditType.FUTURE;
      rest = input.substring(12).trim();
    } else {
      editType = EditType.ALL;
      rest = input.substring(12).trim();
    }

    int spaceIdx = rest.indexOf(" ");
    if (spaceIdx == -1) {
      throw new IllegalArgumentException("Invalid edit command format");
    }
    final String property = rest.substring(0, spaceIdx);
    String remaining = rest.substring(spaceIdx + 1).trim();

    String subject;
    if (remaining.startsWith("\"")) {
      int endQuote = remaining.indexOf("\"", 1);
      if (endQuote == -1) {
        throw new IllegalArgumentException("Unclosed quote in subject");
      }
      subject = remaining.substring(1, endQuote);
      remaining = remaining.substring(endQuote + 1).trim();
    } else {
      spaceIdx = remaining.indexOf(" from ");
      if (spaceIdx == -1) {
        throw new IllegalArgumentException("Expected 'from' after subject");
      }
      subject = remaining.substring(0, spaceIdx);
      remaining = remaining.substring(spaceIdx + 1).trim();
    }

    if (!remaining.startsWith("from ")) {
      throw new IllegalArgumentException("Expected 'from' after subject");
    }
    remaining = remaining.substring(5).trim();

    int withIdx = remaining.indexOf(" with ");
    if (withIdx == -1) {
      throw new IllegalArgumentException("Expected 'with' in edit command");
    }

    String dateTimePart = remaining.substring(0, withIdx).trim();
    String newValue = remaining.substring(withIdx + 6).trim();

    // Remove quotes from newValue if present
    if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
      newValue = newValue.substring(1, newValue.length() - 1);
    }

    LocalDateTime start = parseDateTime(dateTimePart);

    return new EditingAnEventCommand(editType, property, subject, start, newValue);
  }

  private Command parseQueryCommand(String input) {
    String rest = input.substring(13).trim();

    if (rest.startsWith("on ")) {
      LocalDate date = parseDate(rest.substring(3).trim());
      return new QueryEventCom(date);
    } else if (rest.startsWith("from ")) {
      String dateTimePattern = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}";
      Pattern pattern = Pattern.compile(
          "^from (" + dateTimePattern + ") to (" + dateTimePattern + ")$");
      Matcher m = pattern.matcher(rest);
      if (m.matches()) {
        LocalDateTime start = parseDateTime(m.group(1));
        LocalDateTime end = parseDateTime(m.group(2));
        return new QueryEventCom(start, end);
      }
    }

    throw new IllegalArgumentException("Invalid query format");
  }

  private Command parseExportCommand(String input) {
    String filename = input.substring(11).trim();
    if (filename.isEmpty()) {
      throw new IllegalArgumentException("Filename required for export");
    }
    return new ExportingTheCommand(filename);
  }

  private Command parseStatusCommand(String input) {
    String dateTimeStr = input.substring(15).trim();
    LocalDateTime dateTime = parseDateTime(dateTimeStr);
    return new ShowStatus(dateTime);
  }

  /**
   * Parses a date/time string into LocalDateTime.
   *
   * @param dateTimeStr the date/time string
   * @return the parsed LocalDateTime
   * @throws IllegalArgumentException if format is invalid
   */
  public LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      String message = "Invalid date/time format: " + dateTimeStr
          + ". Expected format: yyyy-MM-ddTHH:mm";
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Parses a date string into LocalDate.
   *
   * @param dateStr the date string
   * @return the parsed LocalDate
   * @throws IllegalArgumentException if format is invalid
   */
  public LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      String message = "Invalid date format: " + dateStr
          + ". Expected format: yyyy-MM-dd";
      throw new IllegalArgumentException(message);
    }
  }
}