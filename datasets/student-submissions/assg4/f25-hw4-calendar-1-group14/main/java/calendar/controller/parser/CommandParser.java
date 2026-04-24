package calendar.controller.parser;

import calendar.controller.command.CreateEventCommand;
import calendar.controller.command.EditEventCommand;
import calendar.controller.command.EditEventCommand.EditMode;
import calendar.controller.command.ExportCommand;
import calendar.controller.command.Icommand;
import calendar.controller.command.QueryCommand;
import calendar.controller.command.ShowStatusCommand;
import calendar.exceptions.InvalidCommandException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for calendar commands.
 * Converts text commands into Command objects.
 */
public class CommandParser implements IcommandParser {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private static final Pattern CREATE_SINGLE_TIMED = Pattern.compile(
      "^create event \"(.+?)\" from (\\S+) to (\\S+)$"
  );

  private static final Pattern CREATE_SINGLE_TIMED_NO_QUOTES = Pattern.compile(
      "^create event (\\S+) from (\\S+) to (\\S+)$"
  );

  private static final Pattern CREATE_SINGLE_ALLDAY = Pattern.compile(
      "^create event \"(.+?)\" on (\\S+)$"
  );

  private static final Pattern CREATE_SINGLE_ALLDAY_NO_QUOTES = Pattern.compile(
      "^create event (\\S+) on (\\S+)$"
  );

  private static final Pattern CREATE_RECURRING_TIMED_TIMES = Pattern.compile(
      "^create event \"(.+?)\" from (\\S+) to (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$"
  );

  private static final Pattern CREATE_RECURRING_TIMED_TIMES_NO_QUOTES = Pattern.compile(
      "^create event (\\S+) from (\\S+) to (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$"
  );

  private static final Pattern CREATE_RECURRING_TIMED_UNTIL = Pattern.compile(
      "^create event \"(.+?)\" from (\\S+) to (\\S+) repeats ([MTWRFSU]+) until (\\S+)$"
  );

  private static final Pattern CREATE_RECURRING_TIMED_UNTIL_NO_QUOTES = Pattern.compile(
      "^create event (\\S+) from (\\S+) to (\\S+) repeats ([MTWRFSU]+) until (\\S+)$"
  );

  private static final Pattern CREATE_RECURRING_ALLDAY_TIMES = Pattern.compile(
      "^create event \"(.+?)\" on (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$"
  );

  private static final Pattern CREATE_RECURRING_ALLDAY_TIMES_NO_QUOTES = Pattern.compile(
      "^create event (\\S+) on (\\S+) repeats ([MTWRFSU]+) for (\\d+) times$"
  );

  private static final Pattern CREATE_RECURRING_ALLDAY_UNTIL = Pattern.compile(
      "^create event \"(.+?)\" on (\\S+) repeats ([MTWRFSU]+) until (\\S+)$"
  );

  private static final Pattern CREATE_RECURRING_ALLDAY_UNTIL_NO_QUOTES = Pattern.compile(
      "^create event (\\S+) on (\\S+) repeats ([MTWRFSU]+) until (\\S+)$"
  );

  private static final Pattern EDIT_EVENT = Pattern.compile(
      "^edit event (\\w+) \"(.+?)\" from (\\S+) to (\\S+) with (.+)$"
  );

  private static final Pattern EDIT_EVENT_NO_QUOTES = Pattern.compile(
      "^edit event (\\w+) (\\S+) from (\\S+) to (\\S+) with (\\S+)$"
  );

  private static final Pattern EDIT_EVENTS = Pattern.compile(
      "^edit events (\\w+) \"(.+?)\" from (\\S+) with (.+)$"
  );

  private static final Pattern EDIT_EVENTS_NO_QUOTES = Pattern.compile(
      "^edit events (\\w+) (\\S+) from (\\S+) with (\\S+)$"
  );

  private static final Pattern EDIT_SERIES = Pattern.compile(
      "^edit series (\\w+) \"(.+?)\" from (\\S+) with (.+)$"
  );

  private static final Pattern EDIT_SERIES_NO_QUOTES = Pattern.compile(
      "^edit series (\\w+) (\\S+) from (\\S+) with (\\S+)$"
  );

  private static final Pattern PRINT_EVENTS_ON = Pattern.compile(
      "^print events on (\\S+)$"
  );

  private static final Pattern PRINT_EVENTS_FROM_TO = Pattern.compile(
      "^print events from (\\S+) to (\\S+)$"
  );

  private static final Pattern EXPORT_CAL = Pattern.compile(
      "^export cal (\\S+)$"
  );

  private static final Pattern SHOW_STATUS = Pattern.compile(
      "^show status on (\\S+)$"
  );

  private static final Pattern EXIT = Pattern.compile("^exit$");

  /**
   * Parse a command string and return the appropriate Command object.
   *
   * @param commandString the command to parse
   * @return the Command object, or null for exit command
   * @throws InvalidCommandException if command cannot be parsed
   */
  @Override
  public Icommand parseCommand(String commandString) throws InvalidCommandException {
    if (commandString == null || commandString.trim().isEmpty()) {
      throw new InvalidCommandException("Command cannot be empty");
    }

    commandString = commandString.trim();
    commandString = commandString.replaceAll("\\s+", " ");

    if (commandString.equalsIgnoreCase("exit")) {
      return null;
    }

    Matcher matcher;

    matcher = CREATE_RECURRING_TIMED_TIMES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringTimedTimes(matcher);
    }

    matcher = CREATE_RECURRING_TIMED_TIMES_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringTimedTimes(matcher);
    }

    matcher = CREATE_RECURRING_TIMED_UNTIL.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringTimedUntil(matcher);
    }

    matcher = CREATE_RECURRING_TIMED_UNTIL_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringTimedUntil(matcher);
    }

    matcher = CREATE_RECURRING_ALLDAY_TIMES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringAllDayTimes(matcher);
    }

    matcher = CREATE_RECURRING_ALLDAY_TIMES_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringAllDayTimes(matcher);
    }

    matcher = CREATE_RECURRING_ALLDAY_UNTIL.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringAllDayUntil(matcher);
    }

    matcher = CREATE_RECURRING_ALLDAY_UNTIL_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateRecurringAllDayUntil(matcher);
    }

    matcher = CREATE_SINGLE_TIMED.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateSingleTimed(matcher);
    }

    matcher = CREATE_SINGLE_TIMED_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateSingleTimed(matcher);
    }

    matcher = CREATE_SINGLE_ALLDAY.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateSingleAllDay(matcher);
    }

    matcher = CREATE_SINGLE_ALLDAY_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseCreateSingleAllDay(matcher);
    }

    matcher = EDIT_EVENT.matcher(commandString);
    if (matcher.matches()) {
      return parseEditEvent(matcher);
    }

    matcher = EDIT_EVENT_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseEditEvent(matcher);
    }

    matcher = EDIT_EVENTS.matcher(commandString);
    if (matcher.matches()) {
      return parseEditEvents(matcher);
    }

    matcher = EDIT_EVENTS_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseEditEvents(matcher);
    }

    matcher = EDIT_SERIES.matcher(commandString);
    if (matcher.matches()) {
      return parseEditSeries(matcher);
    }

    matcher = EDIT_SERIES_NO_QUOTES.matcher(commandString);
    if (matcher.matches()) {
      return parseEditSeries(matcher);
    }

    matcher = PRINT_EVENTS_ON.matcher(commandString);
    if (matcher.matches()) {
      return parsePrintEventsOn(matcher);
    }

    matcher = PRINT_EVENTS_FROM_TO.matcher(commandString);
    if (matcher.matches()) {
      return parsePrintEventsFromTo(matcher);
    }

    matcher = EXPORT_CAL.matcher(commandString);
    if (matcher.matches()) {
      return parseExportCal(matcher);
    }

    matcher = SHOW_STATUS.matcher(commandString);
    if (matcher.matches()) {
      return parseShowStatus(matcher);
    }

    throw new InvalidCommandException("Unknown or invalid command: " + commandString);
  }

  /**
   * Parse create command for single timed event.
   *
   * @param matcher the regex matcher containing command groups
   * @return the create event command
   * @throws InvalidCommandException if date or time format is invalid
   */
  private Icommand parseCreateSingleTimed(Matcher matcher) throws InvalidCommandException {
    try {
      String subject = matcher.group(1);
      LocalDateTime start = parseDateTime(matcher.group(2));
      LocalDateTime end = parseDateTime(matcher.group(3));

      return new CreateEventCommand(subject, start, end);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid date/time format in create command: "
          + e.getMessage());
    }
  }

  /**
   * Parse create command for single all-day event.
   *
   * @param matcher the regex matcher containing command groups
   * @return the create event command
   * @throws InvalidCommandException if date format is invalid
   */
  private Icommand parseCreateSingleAllDay(Matcher matcher) throws InvalidCommandException {
    try {
      String subject = matcher.group(1);
      LocalDate date = parseDate(matcher.group(2));

      return new CreateEventCommand(subject, date);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid date format in create command: " + e.getMessage());
    }
  }

  /**
   * Parse create command for recurring timed event with repeat count.
   *
   * @param matcher the regex matcher containing command groups
   * @return the create event command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseCreateRecurringTimedTimes(Matcher matcher) throws InvalidCommandException {
    try {
      String subject = matcher.group(1);
      LocalDateTime start = parseDateTime(matcher.group(2));
      LocalDateTime end = parseDateTime(matcher.group(3));
      Set<String> weekdays = parseWeekdays(matcher.group(4));
      int times = Integer.parseInt(matcher.group(5));

      return new CreateEventCommand(subject, start, end, weekdays, times);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid format in recurring create command: "
          + e.getMessage());
    }
  }

  /**
   * Parse create command for recurring timed event with end date.
   *
   * @param matcher the regex matcher containing command groups
   * @return the create event command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseCreateRecurringTimedUntil(Matcher matcher) throws InvalidCommandException {
    try {
      String subject = matcher.group(1);
      LocalDateTime start = parseDateTime(matcher.group(2));
      LocalDateTime end = parseDateTime(matcher.group(3));
      Set<String> weekdays = parseWeekdays(matcher.group(4));
      LocalDate untilDate = parseDate(matcher.group(5));

      return new CreateEventCommand(subject, start, end, weekdays, untilDate);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid format in recurring create command: "
          + e.getMessage());
    }
  }

  /**
   * Parse create command for recurring all-day event with repeat count.
   *
   * @param matcher the regex matcher containing command groups
   * @return the create event command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseCreateRecurringAllDayTimes(Matcher matcher) throws InvalidCommandException {
    try {
      String subject = matcher.group(1);
      LocalDate date = parseDate(matcher.group(2));
      Set<String> weekdays = parseWeekdays(matcher.group(3));
      int times = Integer.parseInt(matcher.group(4));

      return new CreateEventCommand(subject, date, weekdays, times);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid format in recurring create command: "
          + e.getMessage());
    }
  }

  /**
   * Parse create command for recurring all-day event with end date.
   *
   * @param matcher the regex matcher containing command groups
   * @return the create event command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseCreateRecurringAllDayUntil(Matcher matcher) throws InvalidCommandException {
    try {
      String subject = matcher.group(1);
      LocalDate date = parseDate(matcher.group(2));
      Set<String> weekdays = parseWeekdays(matcher.group(3));
      LocalDate untilDate = parseDate(matcher.group(4));

      return new CreateEventCommand(subject, date, weekdays, untilDate);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid format in recurring create command: "
          + e.getMessage());
    }
  }

  /**
   * Parse edit event command for single instance.
   *
   * @param matcher the regex matcher containing command groups
   * @return the edit event command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseEditEvent(Matcher matcher) throws InvalidCommandException {
    try {
      String property = matcher.group(1);
      String subject = matcher.group(2);
      LocalDateTime start = parseDateTime(matcher.group(3));
      String newValue = cleanQuotes(matcher.group(5));

      return new EditEventCommand(EditMode.SINGLE, property, subject, start, newValue);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid format in edit event command: " + e.getMessage());
    }
  }

  /**
   * Parse edit events command for this event and future events.
   *
   * @param matcher the regex matcher containing command groups
   * @return the edit event command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseEditEvents(Matcher matcher) throws InvalidCommandException {
    try {
      String property = matcher.group(1);
      String subject = matcher.group(2);
      LocalDateTime start = parseDateTime(matcher.group(3));
      String newValue = cleanQuotes(matcher.group(4));

      return new EditEventCommand(EditMode.FROM_THIS, property, subject, start, newValue);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid format in edit events command: "
          + e.getMessage());
    }
  }

  /**
   * Parse edit series command for all events in series.
   *
   * @param matcher the regex matcher containing command groups
   * @return the edit event command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseEditSeries(Matcher matcher) throws InvalidCommandException {
    try {
      String property = matcher.group(1);
      String subject = matcher.group(2);
      LocalDateTime start = parseDateTime(matcher.group(3));
      String newValue = cleanQuotes(matcher.group(4));

      return new EditEventCommand(EditMode.ALL_SERIES, property, subject, start, newValue);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid format in edit series command: " + e.getMessage());
    }
  }

  /**
   * Parse print events on date command.
   *
   * @param matcher the regex matcher containing command groups
   * @return the query command
   * @throws InvalidCommandException if date format is invalid
   */
  private Icommand parsePrintEventsOn(Matcher matcher) throws InvalidCommandException {
    try {
      LocalDate date = parseDate(matcher.group(1));
      return new QueryCommand(date);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid date format in query command: " + e.getMessage());
    }
  }

  /**
   * Parse print events in range command.
   *
   * @param matcher the regex matcher containing command groups
   * @return the query command
   * @throws InvalidCommandException if date or time format is invalid
   */
  private Icommand parsePrintEventsFromTo(Matcher matcher) throws InvalidCommandException {
    try {
      LocalDateTime start = parseDateTime(matcher.group(1));
      LocalDateTime end = parseDateTime(matcher.group(2));
      return new QueryCommand(start, end);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid date/time format in query command: "
          + e.getMessage());
    }
  }

  /**
   * Parse export calendar command.
   *
   * @param matcher the regex matcher containing command groups
   * @return the export command
   * @throws InvalidCommandException if format is invalid
   */
  private Icommand parseExportCal(Matcher matcher) throws InvalidCommandException {
    String fileName = matcher.group(1);
    return new ExportCommand(fileName);
  }

  /**
   * Parse show status command.
   *
   * @param matcher the regex matcher containing command groups
   * @return the show status command
   * @throws InvalidCommandException if date or time format is invalid
   */
  private Icommand parseShowStatus(Matcher matcher) throws InvalidCommandException {
    try {
      LocalDateTime dateTime = parseDateTime(matcher.group(1));
      return new ShowStatusCommand(dateTime);
    } catch (Exception e) {
      throw new InvalidCommandException("Invalid date/time format in status command: "
          + e.getMessage());
    }
  }

  /**
   * Parse a date string in format YYYY-MM-DD.
   *
   * @param dateString the date string to parse
   * @return the parsed LocalDate
   * @throws DateTimeParseException if the format is invalid
   */
  private LocalDate parseDate(String dateString) throws DateTimeParseException {
    return LocalDate.parse(dateString, DATE_FORMATTER);
  }

  /**
   * Parse a date-time string in format YYYY-MM-DDThh:mm.
   *
   * @param dateTimeString the date-time string to parse
   * @return the parsed LocalDateTime
   * @throws DateTimeParseException if the format is invalid
   */
  private LocalDateTime parseDateTime(String dateTimeString) throws DateTimeParseException {
    return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
  }

  /**
   * Parse weekdays string into a set of weekday characters.
   *
   * @param weekdaysString the weekdays string (e.g., MWF)
   * @return the set of weekday characters
   * @throws InvalidCommandException if any weekday character is invalid
   */
  private Set<String> parseWeekdays(String weekdaysString) throws InvalidCommandException {
    Set<String> weekdays = new HashSet<>();

    for (char c : weekdaysString.toCharArray()) {
      String day = String.valueOf(c);



      weekdays.add(day);
    }

    return weekdays;
  }


  /**
   * Remove surrounding quotes from a string if present.
   *
   * @param value the string to clean
   * @return the string without surrounding quotes
   */
  private String cleanQuotes(String value) {
    if (value.startsWith("\"") && value.endsWith("\"")) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }
}