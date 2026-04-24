package calendar.controller;

import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsBetweenCommand;
import calendar.controller.commands.CopyEventsOnDateCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.CreateEventCommand;
import calendar.controller.commands.CreateSeriesCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.EditEventsFromDateCommand;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.commands.ExitCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.PrintEventsInRangeCommand;
import calendar.controller.commands.PrintEventsOnDateCommand;
import calendar.controller.commands.ShowStatusCommand;
import calendar.controller.commands.UseCalendarCommand;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses command strings and creates appropriate ICommand objects.
 * Handles all command syntax defined in the requirements.
 */
public class CommandParser {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Parses a command string and returns the corresponding Command.
   *
   * @param commandLine the command string
   * @return the parsed command
   * @throws IllegalArgumentException if command is invalid
   */
  public Command parse(String commandLine) {
    if (commandLine == null || commandLine.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    String trimmed = commandLine.trim();

    // Check for exit command
    if (trimmed.equalsIgnoreCase("exit")) {
      return new ExitCommand();
    }

    // NEW: Calendar management commands
    if (trimmed.startsWith("create calendar ")) {
      return parseCreateCalendarCommand(trimmed);
    }

    if (trimmed.startsWith("edit calendar ")) {
      return parseEditCalendarCommand(trimmed);
    }

    if (trimmed.startsWith("use calendar ")) {
      return parseUseCalendarCommand(trimmed);
    }

    // NEW: Copy commands
    if (trimmed.startsWith("copy event ") && !trimmed.startsWith("copy events ")) {
      return parseCopyEventCommand(trimmed);
    }

    if (trimmed.startsWith("copy events on ")) {
      return parseCopyEventsOnDateCommand(trimmed);
    }

    if (trimmed.startsWith("copy events between ")) {
      return parseCopyEventsBetweenCommand(trimmed);
    }

    // Check for export command
    if (trimmed.startsWith("export cal ")) {
      return parseExportCommand(trimmed);
    }

    // Check for show status command
    if (trimmed.startsWith("show status on ")) {
      return parseShowStatusCommand(trimmed);
    }

    // Check for print commands
    if (trimmed.startsWith("print events ")) {
      return parsePrintCommand(trimmed);
    }

    // Check for create commands
    if (trimmed.startsWith("create event ")) {
      return parseCreateCommand(trimmed);
    }

    // Check for edit commands
    if (trimmed.startsWith("edit ")) {
      return parseEditCommand(trimmed);
    }

    throw new IllegalArgumentException("Invalid command: " + trimmed);
  }

  private Command parseExportCommand(String cmd) {
    String fileName = cmd.substring("export cal ".length()).trim();
    return new ExportCommand(fileName);
  }

  private Command parseShowStatusCommand(String cmd) {
    String dateTimeStr = cmd.substring("show status on ".length()).trim();
    LocalDateTime dateTime = parseDateTime(dateTimeStr);
    return new ShowStatusCommand(dateTime);
  }

  private Command parsePrintCommand(String cmd) {
    if (cmd.startsWith("print events on ")) {
      String dateStr = cmd.substring("print events on ".length()).trim();
      LocalDate date = parseDate(dateStr);
      return new PrintEventsOnDateCommand(date);
    } else if (cmd.startsWith("print events from ")) {
      Pattern pattern = Pattern.compile(
          "print events from (.+?) to (.+)");
      Matcher matcher = pattern.matcher(cmd);
      if (matcher.matches()) {
        LocalDateTime start = parseDateTime(matcher.group(1).trim());
        LocalDateTime end = parseDateTime(matcher.group(2).trim());
        return new PrintEventsInRangeCommand(start, end);
      }
    }

    throw new IllegalArgumentException("Invalid print command: " + cmd);
  }

  private Command parseCreateCommand(String cmd) {
    String rest = cmd.substring("create event ".length());

    // Check if subject is quoted
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
      int firstSpace = rest.indexOf(" ");
      if (firstSpace == -1) {
        throw new IllegalArgumentException("Invalid create command format");
      }
      subject = rest.substring(0, firstSpace);
      remainder = rest.substring(firstSpace + 1).trim();
    }

    // Check for "on" (all day event) vs "from" (timed event)
    if (remainder.startsWith("on ")) {
      return parseAllDayCreateCommand(subject, remainder);
    } else if (remainder.startsWith("from ")) {
      return parseTimedCreateCommand(subject, remainder);
    }

    throw new IllegalArgumentException("Invalid create command format");
  }

  private Command parseAllDayCreateCommand(String subject, String remainder) {
    remainder = remainder.substring("on ".length()).trim();

    // Check for repeats
    if (remainder.contains(" repeats ")) {
      String[] parts = remainder.split(" repeats ");
      LocalDate startDate = parseDate(parts[0].trim());

      return parseRecurrence(subject, startDate, parts[1].trim());
    } else {
      LocalDate date = parseDate(remainder);
      return new CreateEventCommand(subject, date);
    }
  }

  private Command parseTimedCreateCommand(String subject, String remainder) {
    remainder = remainder.substring("from ".length()).trim();

    // Pattern: datetime to datetime [repeats ...]
    Pattern pattern = Pattern.compile("(.+?) to (.+)");
    Matcher matcher = pattern.matcher(remainder);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time format");
    }

    String startStr = matcher.group(1).trim();
    String endAndRest = matcher.group(2).trim();

    // Check if there's a repeats clause
    if (endAndRest.contains(" repeats ")) {
      String[] parts = endAndRest.split(" repeats ");
      LocalDateTime start = parseDateTime(startStr);
      LocalDateTime end = parseDateTime(parts[0].trim());

      return parseRecurrence(subject, start, end, parts[1].trim());
    } else {
      LocalDateTime start = parseDateTime(startStr);
      LocalDateTime end = parseDateTime(endAndRest);
      return new CreateEventCommand(subject, start, end);
    }
  }

  private Command parseRecurrence(String subject, LocalDate startDate,
                                  String recurrenceStr) {
    LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(startDate, LocalTime.of(17, 0));
    return parseRecurrence(subject, start, end, recurrenceStr);
  }

  private Command parseRecurrence(String subject, LocalDateTime start,
                                  LocalDateTime end, String recurrenceStr) {
    // Pattern: weekdays for N times OR weekdays until date
    if (recurrenceStr.contains(" for ")) {
      String[] parts = recurrenceStr.split(" for ");
      Set<DayOfWeek> days = parseDays(parts[0].trim());
      String[] countParts = parts[1].trim().split(" ");
      int occurrences = Integer.parseInt(countParts[0]);
      return new CreateSeriesCommand(subject, start, end, days,
          occurrences, null);
    } else if (recurrenceStr.contains(" until ")) {
      String[] parts = recurrenceStr.split(" until ");
      Set<DayOfWeek> days = parseDays(parts[0].trim());
      LocalDate untilDate = parseDate(parts[1].trim());
      return new CreateSeriesCommand(subject, start, end, days,
          null, untilDate);
    }

    throw new IllegalArgumentException("Invalid recurrence format");
  }

  private Command parseEditCommand(String cmd) {
    if (cmd.startsWith("edit event ")) {
      return parseSingleEditCommand(cmd);
    } else if (cmd.startsWith("edit events ")) {
      return parseMultipleEditCommand(cmd);
    } else if (cmd.startsWith("edit series ")) {
      return parseSeriesEditCommand(cmd);
    }

    throw new IllegalArgumentException("Invalid edit command: " + cmd);
  }

  private Command parseSingleEditCommand(String cmd) {
    // edit event <property> <subject> from <datetime> to <datetime> with <value>
    Pattern pattern = Pattern.compile(
        "edit event (\\w+) (.+?) from (.+?) to (.+?) with (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid edit event format");
    }

    String property = matcher.group(1);
    String subject = parseSubject(matcher.group(2).trim());
    LocalDateTime start = parseDateTime(matcher.group(3).trim());
    String newValue = matcher.group(5).trim();

    return new EditEventCommand(subject, start, property, newValue);
  }

  private Command parseMultipleEditCommand(String cmd) {
    // edit events <property> <subject> from <datetime> with <value>
    Pattern pattern = Pattern.compile(
        "edit events (\\w+) (.+?) from (.+?) with (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid edit events format");
    }

    String property = matcher.group(1);
    String subject = parseSubject(matcher.group(2).trim());
    LocalDateTime start = parseDateTime(matcher.group(3).trim());
    String newValue = matcher.group(4).trim();

    return new EditEventsFromDateCommand(subject, start, property, newValue);
  }

  private Command parseSeriesEditCommand(String cmd) {
    // edit series <property> <subject> from <datetime> with <value>
    Pattern pattern = Pattern.compile(
        "edit series (\\w+) (.+?) from (.+?) with (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid edit series format");
    }

    String property = matcher.group(1);
    String subject = parseSubject(matcher.group(2).trim());
    LocalDateTime start = parseDateTime(matcher.group(3).trim());
    String newValue = matcher.group(4).trim();

    return new EditSeriesCommand(subject, start, property, newValue);
  }

  private String parseSubject(String str) {
    if (str.startsWith("\"") && str.contains("\"")) {
      int endQuote = str.indexOf("\"", 1);
      return str.substring(1, endQuote);
    }
    return str.split(" ")[0];
  }

  private LocalDate parseDate(String dateStr) {
    return LocalDate.parse(dateStr, DATE_FORMATTER);
  }

  private LocalDateTime parseDateTime(String dateTimeStr) {
    String[] parts = dateTimeStr.split("T");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid datetime format: " + dateTimeStr);
    }
    LocalDate date = parseDate(parts[0]);
    LocalTime time = LocalTime.parse(parts[1], TIME_FORMATTER);
    return LocalDateTime.of(date, time);
  }

  private Set<DayOfWeek> parseDays(String dayStr) {
    Set<DayOfWeek> days = new HashSet<>();
    for (char c : dayStr.toCharArray()) {
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

  // NEW PARSER METHODS FOR PART 2

  private Command parseCreateCalendarCommand(String cmd) {
    // create calendar --name <name> --timezone area/location
    Pattern pattern = Pattern.compile(
        "create calendar --name (\\S+) --timezone (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid create calendar format. "
          + "Expected: create calendar --name <name> --timezone <area/location>");
    }

    String name = matcher.group(1).trim();
    String timezone = matcher.group(2).trim();

    return new CreateCalendarCommand(name, timezone);
  }

  private Command parseEditCalendarCommand(String cmd) {
    // edit calendar --name <name> --property <property> <value>
    Pattern pattern = Pattern.compile(
        "edit calendar --name (\\S+) --property (\\S+) (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid edit calendar format. "
          + "Expected: edit calendar --name <name> --property <property> <value>");
    }

    String name = matcher.group(1).trim();
    String property = matcher.group(2).trim();
    String newValue = matcher.group(3).trim();

    return new EditCalendarCommand(name, property, newValue);
  }

  private Command parseUseCalendarCommand(String cmd) {
    // use calendar --name <name>
    Pattern pattern = Pattern.compile(
        "use calendar --name (\\S+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid use calendar format. "
          + "Expected: use calendar --name <name>");
    }

    String name = matcher.group(1).trim();
    return new UseCalendarCommand(name);
  }

  private Command parseCopyEventCommand(String cmd) {
    // copy event <name> on <datetime> --target <calendar> to <datetime>
    Pattern pattern = Pattern.compile(
        "copy event (.+?) on (.+?) --target (\\S+) to (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid copy event format. "
          + "Expected: copy event <name> on <datetime> --target <calendar> to <datetime>");
    }

    String eventName = parseSubject(matcher.group(1).trim());
    LocalDateTime sourceStart = parseDateTime(matcher.group(2).trim());
    String targetCalendar = matcher.group(3).trim();
    LocalDateTime targetStart = parseDateTime(matcher.group(4).trim());

    return new CopyEventCommand(eventName, sourceStart, targetCalendar, targetStart);
  }

  private Command parseCopyEventsOnDateCommand(String cmd) {
    // copy events on <date> --target <calendar> to <date>
    Pattern pattern = Pattern.compile(
        "copy events on (.+?) --target (\\S+) to (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid copy events format. "
          + "Expected: copy events on <date> --target <calendar> to <date>");
    }

    LocalDate sourceDate = parseDate(matcher.group(1).trim());
    String targetCalendar = matcher.group(2).trim();
    LocalDate targetDate = parseDate(matcher.group(3).trim());

    return new CopyEventsOnDateCommand(sourceDate, targetCalendar, targetDate);
  }

  private Command parseCopyEventsBetweenCommand(String cmd) {
    // copy events between <date> and <date> --target <calendar> to <date>
    Pattern pattern = Pattern.compile(
        "copy events between (.+?) and (.+?) --target (\\S+) to (.+)");
    Matcher matcher = pattern.matcher(cmd);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid copy events between format. "
          + "Expected: copy events between <date> and <date> --target <calendar> to <date>");
    }

    LocalDate startDate = parseDate(matcher.group(1).trim());
    LocalDate endDate = parseDate(matcher.group(2).trim());
    String targetCalendar = matcher.group(3).trim();
    LocalDate targetStartDate = parseDate(matcher.group(4).trim());

    return new CopyEventsBetweenCommand(startDate, endDate, targetCalendar, targetStartDate);
  }
}