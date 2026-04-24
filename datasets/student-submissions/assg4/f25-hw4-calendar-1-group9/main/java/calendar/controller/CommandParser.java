package calendar.controller;

import calendar.command.CreateEventCommand;
import calendar.command.EditEventCommand;
import calendar.command.ExportCommand;
import calendar.command.InCommand;
import calendar.command.QueryEventCommand;
import calendar.command.ShowStatusCommand;
import calendar.exception.InvalidCommandException;
import calendar.exception.InvalidDateTimeException;
import calendar.model.Weekday;
import calendar.service.InEventService;
import calendar.service.InExportService;
import calendar.util.DateTimeParser;
import calendar.view.InCalendarView;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses command strings into executable Command objects.
 * Uses regex patterns and factory methods for command creation.
 */
public class CommandParser {

  private final InEventService eventService;
  private final InExportService exportService;
  private final InCalendarView view;

  /**
   * Constructs a CommandParser with required services.
   *
   * @param eventService  the event service
   * @param exportService the export service
   * @param view          the view for output
   */
  public CommandParser(InEventService eventService,
                       InExportService exportService,
                       InCalendarView view) {
    this.eventService = eventService;
    this.exportService = exportService;
    this.view = view;
  }

  /**
   * Parses a command string into an executable command.
   *
   * @param commandString the command string
   * @return the parsed command
   * @throws InvalidCommandException if command is invalid
   */
  public InCommand parse(String commandString) throws InvalidCommandException {
    if (commandString == null || commandString.trim().isEmpty()) {
      throw new InvalidCommandException("Command cannot be empty");
    }

    String cmd = commandString.trim();

    if (cmd.equalsIgnoreCase("exit")) {
      return null;
    }

    try {
      if (cmd.startsWith("create event")) {
        return parseCreateEvent(cmd);
      } else if (cmd.startsWith("edit event ") || cmd.startsWith("edit events ")
          || cmd.startsWith("edit series ")) {
        return parseEditEvent(cmd);
      } else if (cmd.startsWith("print events")) {
        return parseQueryEvent(cmd);
      } else if (cmd.startsWith("show status")) {
        return parseShowStatus(cmd);
      } else if (cmd.startsWith("export cal")) {
        return parseExportCal(cmd);
      } else {
        throw new InvalidCommandException("Unknown command: " + cmd);
      }
    } catch (InvalidDateTimeException e) {
      throw new InvalidCommandException("Invalid date/time in command: "
          + e.getMessage(), e);
    }
  }

  private InCommand parseCreateEvent(String cmd) throws InvalidCommandException,
      InvalidDateTimeException {
    Pattern singleWithTime = Pattern.compile(
        "create event (.+?) from ([\\d-T:]+) to ([\\d-T:]+)$");
    Matcher m = singleWithTime.matcher(cmd);
    if (m.matches()) {
      String subject = cleanSubject(m.group(1));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(2));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(3));
      return CreateEventCommand.forSingleEvent(eventService, view, subject,
          start, end, new HashMap<>());
    }

    Pattern recurringCount = Pattern.compile(
        "create event (.+?) from ([\\d-T:]+) to ([\\d-T:]+) "
            + "repeats ([MTWRFSU]+) for (\\d+) times$");
    m = recurringCount.matcher(cmd);
    if (m.matches()) {
      String subject = cleanSubject(m.group(1));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(2));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(3));
      Set<Weekday> weekdays = parseWeekdays(m.group(4));
      int count = Integer.parseInt(m.group(5));
      return CreateEventCommand.forSeriesWithCount(eventService, view, subject,
          start, end, weekdays, count, new HashMap<>());
    }

    Pattern recurringUntil = Pattern.compile(
        "create event (.+?) from ([\\d-T:]+) to ([\\d-T:]+) "
            + "repeats ([MTWRFSU]+) until ([\\d-]+)$");
    m = recurringUntil.matcher(cmd);
    if (m.matches()) {
      String subject = cleanSubject(m.group(1));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(2));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(3));
      Set<Weekday> weekdays = parseWeekdays(m.group(4));
      LocalDate endDate = DateTimeParser.parseDate(m.group(5));
      return CreateEventCommand.forSeriesWithEndDate(eventService, view, subject,
          start, end, weekdays, endDate, new HashMap<>());
    }

    Pattern allDaySingle = Pattern.compile(
        "create event (.+?) on ([\\d-]+)$");
    m = allDaySingle.matcher(cmd);
    if (m.matches()) {
      String subject = cleanSubject(m.group(1));
      LocalDate date = DateTimeParser.parseDate(m.group(2));
      LocalDateTime start = date.atTime(8, 0);
      return CreateEventCommand.forSingleEvent(eventService, view, subject,
          start, null, new HashMap<>());
    }

    Pattern allDayCount = Pattern.compile(
        "create event (.+?) on ([\\d-]+) repeats ([MTWRFSU]+) for (\\d+) times$");
    m = allDayCount.matcher(cmd);
    if (m.matches()) {
      String subject = cleanSubject(m.group(1));
      LocalDate date = DateTimeParser.parseDate(m.group(2));
      LocalDateTime start = date.atTime(8, 0);
      Set<Weekday> weekdays = parseWeekdays(m.group(3));
      int count = Integer.parseInt(m.group(4));
      return CreateEventCommand.forSeriesWithCount(eventService, view, subject,
          start, null, weekdays, count, new HashMap<>());
    }

    Pattern allDayUntil = Pattern.compile(
        "create event (.+?) on ([\\d-]+) repeats ([MTWRFSU]+) until ([\\d-]+)$");
    m = allDayUntil.matcher(cmd);
    if (m.matches()) {
      String subject = cleanSubject(m.group(1));
      LocalDate date = DateTimeParser.parseDate(m.group(2));
      LocalDateTime start = date.atTime(8, 0);
      Set<Weekday> weekdays = parseWeekdays(m.group(3));
      LocalDate endDate = DateTimeParser.parseDate(m.group(4));
      return CreateEventCommand.forSeriesWithEndDate(eventService, view, subject,
          start, null, weekdays, endDate, new HashMap<>());
    }

    throw new InvalidCommandException("Invalid create event command format");
  }

  private InCommand parseEditEvent(String cmd) throws InvalidCommandException,
      InvalidDateTimeException {
    String editType = "single";
    if (cmd.startsWith("edit events ")) {
      editType = "from";
    } else if (cmd.startsWith("edit series ")) {
      editType = "entire";
    }

    Pattern pattern = Pattern.compile(
        "edit (?:event|events|series) (\\w+) (.+?) from ([\\d-T:]+) with (.+)$");
    Matcher m = pattern.matcher(cmd);

    if (m.matches()) {
      String property = m.group(1);
      String subject = cleanSubject(m.group(2));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(3));
      String newValue = m.group(4).trim();

      return new EditEventCommand(eventService, view, subject, start,
          property, newValue, editType);
    }

    throw new InvalidCommandException("Invalid edit command format");
  }

  private InCommand parseQueryEvent(String cmd) throws InvalidCommandException,
      InvalidDateTimeException {
    Pattern onDate = Pattern.compile("print events on ([\\d-]+)$");
    Matcher m = onDate.matcher(cmd);
    if (m.matches()) {
      LocalDate date = DateTimeParser.parseDate(m.group(1));
      return new QueryEventCommand(eventService, view, date, null, null);
    }

    Pattern fromTo = Pattern.compile(
        "print events from ([\\d-T:]+) to ([\\d-T:]+)$");
    m = fromTo.matcher(cmd);
    if (m.matches()) {
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(1));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(2));
      return new QueryEventCommand(eventService, view, null, start, end);
    }

    throw new InvalidCommandException("Invalid print events command format");
  }

  private InCommand parseShowStatus(String cmd) throws InvalidCommandException,
      InvalidDateTimeException {
    Pattern pattern = Pattern.compile("show status on ([\\d-T:]+)$");
    Matcher m = pattern.matcher(cmd);

    if (m.matches()) {
      LocalDateTime dateTime = DateTimeParser.parseDateTime(m.group(1));
      return new ShowStatusCommand(eventService, view, dateTime);
    }

    throw new InvalidCommandException("Invalid show status command format");
  }

  private InCommand parseExportCal(String cmd) throws InvalidCommandException {
    Pattern pattern = Pattern.compile("export cal (.+)$");
    Matcher m = pattern.matcher(cmd);

    if (m.matches()) {
      String filename = m.group(1).trim();
      return new ExportCommand(exportService, view, Paths.get(filename));
    }

    throw new InvalidCommandException("Invalid export command format");
  }

  private String cleanSubject(String subject) {
    String trimmed = subject.trim();
    if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
      return trimmed.substring(1, trimmed.length() - 1);
    }
    return trimmed;
  }

  private Set<Weekday> parseWeekdays(String weekdayStr)
      throws InvalidCommandException {
    Set<Weekday> weekdays = new HashSet<>();
    for (char c : weekdayStr.toCharArray()) {
      try {
        weekdays.add(Weekday.fromChar(c));
      } catch (IllegalArgumentException e) {
        throw new InvalidCommandException(
            "Invalid weekday character: '" + c + "'. "
                + "Valid weekdays: M(Monday), T(Tuesday), W(Wednesday), "
                + "R(Thursday), F(Friday), S(Saturday), U(Sunday)");
      }
    }

    if (weekdays.isEmpty()) {
      throw new InvalidCommandException("Weekdays cannot be empty");
    }

    return weekdays;
  }
}