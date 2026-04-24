package calendar.controller;

import calendar.controller.commands.CreateEventCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.EditEventsCommand;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.commands.EventSeriesCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.PrintEventsBetweenCommand;
import calendar.controller.commands.PrintEventsOnCommand;
import calendar.controller.commands.ShowStatusCommand;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses user command strings into corresponding Command objects.
 * Uses regular expressions to match specific command patterns and
 * extract their parameters.
 */
public class CommandParser {
  private static final String SUBJECT_PATTERN = "(?:\"([^\"]+)\"|(\\S+))";

  /**
   * Parses the given command line string into a Command object.
   *
   * @param commandLine the input command string from the user
   * @return the corresponding Command object
   * @throws IllegalArgumentException if the command format is invalid
   */
  public Command parseCommand(String commandLine) throws IllegalArgumentException {
    if (commandLine == null || commandLine.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }
    commandLine = commandLine.trim();

    if (commandLine.startsWith("create event ")) {
      return parseCreateEvent(commandLine);
    } else if (commandLine.startsWith("edit event ")) {
      return parseEditEvent(commandLine);
    } else if (commandLine.startsWith("edit events ")) {
      return parseEditEvents(commandLine);
    } else if (commandLine.startsWith("edit series ")) {
      return parseEditSeries(commandLine);
    } else if (commandLine.startsWith("print events on ")) {
      return parsePrintEventsOn(commandLine);
    } else if (commandLine.startsWith("print events from ")) {
      return parsePrintEventsFrom(commandLine);
    } else if (commandLine.startsWith("show status on ")) {
      return parseShowStatus(commandLine);
    } else if (commandLine.startsWith("export cal ")) {
      return parseExportCal(commandLine);
    } else {
      throw new IllegalArgumentException("Unknown command: " + commandLine);
    }
  }

  /**
   * Parses a "create event" command to determine whether it represents
   * a single event or a repeating event series.
   *
   * @param commandLine the full command string
   * @return the appropriate CreateEventCommand or EventSeriesCommand
   */
  private Command parseCreateEvent(String commandLine) {
    Command command;

    command = tryParseTimedSeries(commandLine);
    if (command != null) {
      return command;
    }

    command = tryParseSingleTimedEvent(commandLine);
    if (command != null) {
      return command;
    }

    command = tryParseAllDaySeries(commandLine);
    if (command != null) {
      return command;
    }

    command = tryParseSingleAllDayEvent(commandLine);
    if (command != null) {
      return command;
    }

    throw new IllegalArgumentException("Invalid create event syntax");
  }

  /**
   * Tries to parse a repeating event series that has specific start and end times.
   *
   * @param commandLine the command to parse
   * @return an EventSeriesCommand if matched, otherwise null
   */
  private Command tryParseTimedSeries(String commandLine) {
    Pattern seriesWithCount = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " from (\\S+) to (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$");
    Matcher matcher = seriesWithCount.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(3));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(4));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(5));
      int count = Integer.parseInt(matcher.group(6));
      return new EventSeriesCommand(subject, start, end, days, count, null);
    }

    Pattern seriesWithUntil = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " from (\\S+) to (\\S+) repeats ([MTWRFSU]+) until (\\S+)$");
    matcher = seriesWithUntil.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(3));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(4));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(5));
      LocalDate until = CommandParserUtils.parseDate(matcher.group(6));
      return new EventSeriesCommand(subject, start, end, days, null, until);
    }

    return null;
  }

  /**
   * Tries to parse a single timed event (non-repeating).
   *
   * @param commandLine the command string
   * @return a CreateEventCommand if matched, otherwise null
   */
  private Command tryParseSingleTimedEvent(String commandLine) {
    Pattern pattern = Pattern.compile(
        "create event " + SUBJECT_PATTERN + " from (\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(3));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(4));
      return new CreateEventCommand(subject, start, end);
    }

    return null;
  }

  /**
   * Tries to parse an all-day event series without explicit start or end times.
   *
   * @param commandLine the command string
   * @return an EventSeriesCommand if matched, otherwise null
   */
  private Command tryParseAllDaySeries(String commandLine) {
    Pattern seriesWithCount = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " on (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$");
    Matcher matcher = seriesWithCount.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDate date = CommandParserUtils.parseDate(matcher.group(3));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(4));
      int count = Integer.parseInt(matcher.group(5));

      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      return new EventSeriesCommand(subject, start, end, days, count, null);
    }

    Pattern seriesWithUntil = Pattern.compile(
        "create event " + SUBJECT_PATTERN
            + " on (\\S+) repeats ([MTWRFSU]+) until (\\S+)$");
    matcher = seriesWithUntil.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDate date = CommandParserUtils.parseDate(matcher.group(3));
      Set<DayOfWeek> days = CommandParserUtils.parseDays(matcher.group(4));
      LocalDate until = CommandParserUtils.parseDate(matcher.group(5));

      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      return new EventSeriesCommand(subject, start, end, days, null, until);
    }

    return null;
  }

  /**
   * Tries to parse a single all-day event.
   *
   * @param commandLine the command string
   * @return a CreateEventCommand if matched, otherwise null
   */
  private Command tryParseSingleAllDayEvent(String commandLine) {
    Pattern pattern = Pattern.compile(
        "create event " + SUBJECT_PATTERN + " on (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String subject = CommandParserUtils.extractSubject(matcher, 1, 2);
      LocalDate date = CommandParserUtils.parseDate(matcher.group(3));

      LocalDateTime start = date.atTime(8, 0);
      LocalDateTime end = date.atTime(17, 0);
      return new CreateEventCommand(subject, start, end);
    }

    return null;
  }

  /**
   * Parses the command to edit a single event.
   *
   * @param commandLine the command string
   * @return an EditEventCommand
   */
  private Command parseEditEvent(String commandLine) {
    Pattern pattern = Pattern.compile(
        "edit event (\\S+) " + SUBJECT_PATTERN
            + " from (\\S+) to (\\S+) with (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = CommandParserUtils.extractSubject(matcher, 2, 3);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(4));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(5));
      String newValue = matcher.group(6).trim();

      newValue = stripQuotes(newValue);

      return new EditEventCommand(subject, start, end, property, newValue);
    }

    throw new IllegalArgumentException("Invalid edit event syntax");
  }

  /**
   * Strips surrounding quotes from a string if present.
   * "Project Review" → Project Review
   * Project Review → Project Review
   */
  private String stripQuotes(String value) {
    if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  /**
   * Parses the command to edit multiple events that share a subject.
   *
   * @param commandLine the command string
   * @return an EditEventsCommand
   */
  private Command parseEditEvents(String commandLine) {
    Pattern pattern = Pattern.compile(
        "edit events (\\w+) " + SUBJECT_PATTERN + " from (\\S+) with (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = CommandParserUtils.extractSubject(matcher, 2, 3);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(4));
      String newValue = matcher.group(5).trim();

      newValue = stripQuotes(newValue);

      return new EditEventsCommand(subject, start, property, newValue);
    }

    throw new IllegalArgumentException("Invalid edit events syntax");
  }

  /**
   * Parses the command to edit an entire event series.
   *
   * @param commandLine the command string
   * @return an EditSeriesCommand
   */
  private Command parseEditSeries(String commandLine) {
    Pattern pattern = Pattern.compile(
        "edit series (\\w+) " + SUBJECT_PATTERN + " from (\\S+) with (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String property = matcher.group(1);
      String subject = CommandParserUtils.extractSubject(matcher, 2, 3);
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(4));
      String newValue = matcher.group(5).trim();

      newValue = stripQuotes(newValue);

      return new EditSeriesCommand(subject, start, property, newValue);
    }

    throw new IllegalArgumentException("Invalid edit series syntax");
  }

  /**
   * Parses the command to print events on a specific date.
   *
   * @param commandLine the command string
   * @return a PrintEventsOnCommand
   */
  private Command parsePrintEventsOn(String commandLine) {
    Pattern pattern = Pattern.compile("print events on (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDate date = CommandParserUtils.parseDate(matcher.group(1));
      return new PrintEventsOnCommand(date);
    }

    throw new IllegalArgumentException("Invalid print events on syntax");
  }

  /**
   * Parses the command to print events between two dates.
   *
   * @param commandLine the command string
   * @return a PrintEventsBetweenCommand
   */
  private Command parsePrintEventsFrom(String commandLine) {
    Pattern pattern = Pattern.compile("print events from (\\S+) to (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDateTime start = CommandParserUtils.parseDateTime(matcher.group(1));
      LocalDateTime end = CommandParserUtils.parseDateTime(matcher.group(2));
      return new PrintEventsBetweenCommand(start, end);
    }

    throw new IllegalArgumentException("Invalid print events from syntax");
  }

  /**
   * Parses the command to show the status of the calendar on a specific date.
   *
   * @param commandLine the command string
   * @return a ShowStatusCommand
   */
  private Command parseShowStatus(String commandLine) {
    Pattern pattern = Pattern.compile("show status on (\\S+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      LocalDateTime dateTime = CommandParserUtils.parseDateTime(matcher.group(1));
      return new ShowStatusCommand(dateTime);
    }

    throw new IllegalArgumentException("Invalid show status syntax");
  }

  /**
   * Parses the command to export the calendar to a file.
   *
   * @param commandLine the command string
   * @return an ExportCommand
   */
  private Command parseExportCal(String commandLine) {
    Pattern pattern = Pattern.compile("export cal (.+)$");
    Matcher matcher = pattern.matcher(commandLine);

    if (matcher.matches()) {
      String fileName = matcher.group(1).trim();
      return new ExportCommand(fileName);
    }

    throw new IllegalArgumentException("Invalid export syntax");
  }
}