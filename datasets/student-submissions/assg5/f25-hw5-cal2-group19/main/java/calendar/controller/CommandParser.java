package calendar.controller;

import calendar.command.CopyEventCommand;
import calendar.command.CopyEventsBetweenDatesCommand;
import calendar.command.CopyEventsOnDateCommand;
import calendar.command.CreateCalendarCommand;
import calendar.command.CreateEventCommand;
import calendar.command.EditCalendarCommand;
import calendar.command.EditEventCommand;
import calendar.command.ExportCommand;
import calendar.command.InCommand;
import calendar.command.QueryEventCommand;
import calendar.command.ShowStatusCommand;
import calendar.command.UseCalendarCommand;
import calendar.exception.ActiveCalendarRequiredException;
import calendar.exception.InvalidCommandException;
import calendar.exception.InvalidDateTimeException;
import calendar.model.CalendarDatabase;
import calendar.model.InCalendar;
import calendar.model.Weekday;
import calendar.service.EventService;
import calendar.service.InEventService;
import calendar.service.InExportService;
import calendar.util.DateTimeParser;
import calendar.view.InCalendarView;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses command strings into executable Command objects.
 * Uses Map-based pattern matching instead of if-else chains.
 * - Added calendar management commands (create, edit, use)
 * - Added copy event commands
 * - Now validates that a calendar is active before event operations
 * - Works with CalendarDatabase for multi-calendar support
 */
public class CommandParser {

  private final CalendarDatabase calendarDatabase;
  private final InExportService exportService;
  private final InCalendarView view;
  private final Map<Pattern, Function<Matcher, InCommand>> commandFactories;

  /**
   * Constructs a CommandParser with required services.
   * Now accepts CalendarDatabase instead of single EventService.
   *
   * @param calendarDatabase the calendar database
   * @param exportService    the export service
   * @param view             the view for output
   */
  public CommandParser(CalendarDatabase calendarDatabase,
                       InExportService exportService,
                       InCalendarView view) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(exportService, "Export service cannot be null");
    Objects.requireNonNull(view, "View cannot be null");

    this.calendarDatabase = calendarDatabase;
    this.exportService = exportService;
    this.view = view;
    this.commandFactories = initializeCommandFactories();
  }

  /**
   * Initializes the command factory map with all supported patterns.
   * Using LinkedHashMap to maintain insertion order for matching priority.
   *
   * @return map of patterns to command factory functions
   */
  private Map<Pattern, Function<Matcher, InCommand>> initializeCommandFactories() {
    Map<Pattern, Function<Matcher, InCommand>> factories = new LinkedHashMap<>();

    factories.put(
        Pattern.compile(
            "create calendar --name ([\\w-]+) --timezone ([\\w/]+)$",
            Pattern.CASE_INSENSITIVE),
        this::createCalendar
    );

    factories.put(
        Pattern.compile(
            "edit calendar --name ([\\w-]+) --property (name|timezone) (.+)$",
            Pattern.CASE_INSENSITIVE),
        this::editCalendar
    );

    factories.put(
        Pattern.compile(
            "use calendar --name ([\\w-]+)$",
            Pattern.CASE_INSENSITIVE),
        this::useCalendar
    );

    factories.put(
        Pattern.compile(
            "copy event (.+?) on ([\\d-T:]+) --target ([\\w-]+) to ([\\d-T:]+)$",
            Pattern.CASE_INSENSITIVE),
        this::createCopyEvent
    );

    factories.put(
        Pattern.compile(
            "copy events on ([\\d-]+) --target ([\\w-]+) to ([\\d-]+)$",
            Pattern.CASE_INSENSITIVE),
        this::createCopyEventsOnDate
    );

    factories.put(
        Pattern.compile(
            "copy events between ([\\d-]+) and ([\\d-]+) --target ([\\w-]+) to ([\\d-]+)$",
            Pattern.CASE_INSENSITIVE),
        this::createCopyEventsBetween
    );

    factories.put(
        Pattern.compile(
            "create event (.+?) from ([\\d-T:]+) to ([\\d-T:]+) "
                + "repeats ([MTWRFSU]+) for (\\d+) times$"),
        this::createRecurringEventWithCount
    );

    factories.put(
        Pattern.compile(
            "create event (.+?) from ([\\d-T:]+) to ([\\d-T:]+) "
                + "repeats ([MTWRFSU]+) until ([\\d-]+)$"),
        this::createRecurringEventUntilDate
    );

    factories.put(
        Pattern.compile(
            "create event (.+?) from ([\\d-T:]+)$"),
        this::createAllDayFromDateTime
    );

    factories.put(
        Pattern.compile(
            "create event (.+?) from ([\\d-T:]+) to ([\\d-T:]+)$"),
        this::createSingleEventWithTime
    );

    factories.put(
        Pattern.compile(
            "create event (.+?) on ([\\d-]+) repeats ([MTWRFSU]+) "
                + "for (\\d+) times$"),
        this::createAllDayWithCount
    );

    factories.put(
        Pattern.compile(
            "create event (.+?) on ([\\d-]+) repeats ([MTWRFSU]+) "
                + "until ([\\d-]+)$"),
        this::createAllDayUntilDate
    );

    factories.put(
        Pattern.compile("create event (.+?) on ([\\d-]+)$"),
        this::createAllDaySingle
    );

    factories.put(
        Pattern.compile(
            "edit\\s+event\\s+subject\\s+\"([^\"]+)\"\\s+from\\s+([\\d-T:]+)"
                + "\\s+to\\s+([\\d-T:]+)\\s+with\\s+\"([^\"]+)\"$",
            Pattern.CASE_INSENSITIVE),
        this::createEditEventBySubjectAndTimeRange
    );

    factories.put(
        Pattern.compile(
            "edit event (\\w+) (.+?) from ([\\d-T:]+) with (.+)$"),
        m -> createEditCommand(m, "single")
    );

    factories.put(
        Pattern.compile(
            "edit events (\\w+) (.+?) from ([\\d-T:]+) with (.+)$"),
        m -> createEditCommand(m, "from")
    );

    factories.put(
        Pattern.compile(
            "edit series (\\w+) (.+?) from ([\\d-T:]+) with (.+)$"),
        m -> createEditCommand(m, "entire")
    );

    factories.put(
        Pattern.compile("print events from ([\\d-T:]+) to ([\\d-T:]+)$"),
        this::createQueryByRange
    );

    factories.put(
        Pattern.compile("print events on ([\\d-]+)$"),
        this::createQueryByDate
    );

    factories.put(
        Pattern.compile("show status on ([\\d-T:]+)$"),
        this::createShowStatus
    );

    factories.put(
        Pattern.compile("export cal (.+)$"),
        this::createExport
    );

    return factories;
  }

  /**
   * Parses a command string into an executable command.
   *
   * @param commandString the command string
   * @return the parsed command, or null for exit command
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

    for (Map.Entry<Pattern, Function<Matcher, InCommand>> entry :
        commandFactories.entrySet()) {
      Matcher matcher = entry.getKey().matcher(cmd);
      if (matcher.matches()) {
        try {
          return entry.getValue().apply(matcher);
        } catch (ActiveCalendarRequiredException e) {
          throw new InvalidCommandException(e.getMessage(), e);
        } catch (Exception e) {
          throw new InvalidCommandException(
              "Error parsing command: " + e.getMessage(), e);
        }
      }
    }

    throw new InvalidCommandException("Unknown command: " + cmd);
  }

  /**
   * Creates a command to create a new calendar.
   * Syntax: create calendar --name MyCalendar --timezone America/New_York
   */
  private InCommand createCalendar(Matcher m) {
    String name = m.group(1);
    String timezoneStr = m.group(2);

    try {
      ZoneId timezone = ZoneId.of(timezoneStr);
      return new CreateCalendarCommand(calendarDatabase, view, name, timezone);
    } catch (Exception e) {
      throw new RuntimeException("Invalid timezone: " + timezoneStr, e);
    }
  }

  /**
   * Creates a command to edit a calendar property.
   * Syntax: edit calendar --name MyCalendar --property name NewName
   *         edit calendar --name MyCalendar --property timezone Europe/Paris
   */
  private InCommand editCalendar(Matcher m) {
    String name = m.group(1);
    String property = m.group(2);
    String newValue = m.group(3);

    return new EditCalendarCommand(calendarDatabase, view, name, property, newValue);
  }

  /**
   * Creates a command to set the active calendar.
   * Syntax: use calendar --name MyCalendar
   */
  private InCommand useCalendar(Matcher m) {
    String name = m.group(1);
    return new UseCalendarCommand(calendarDatabase, view, name);
  }

  /**
   * Creates a command to copy a single event.
   * Syntax: copy event Meeting on 2025-05-05T10:00 --target WorkCal to 2025-06-01T10:00
   */
  private InCommand createCopyEvent(Matcher m) {
    try {
      String eventSubject = cleanSubject(m.group(1));
      LocalDateTime sourceDateTime = DateTimeParser.parseDateTime(m.group(2));
      String targetCalendar = m.group(3);
      LocalDateTime targetDateTime = DateTimeParser.parseDateTime(m.group(4));

      return new CopyEventCommand(
          calendarDatabase, view, eventSubject, sourceDateTime,
          targetCalendar, targetDateTime);
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException("Invalid date/time format in copy command", e);
    }
  }

  /**
   * Creates a command to copy all events on a date.
   * Syntax: copy events on 2025-05-05 --target WorkCal to 2025-06-01
   */
  private InCommand createCopyEventsOnDate(Matcher m) {
    try {
      LocalDate sourceDate = DateTimeParser.parseDate(m.group(1));
      String targetCalendar = m.group(2);
      LocalDate targetDate = DateTimeParser.parseDate(m.group(3));

      return new CopyEventsOnDateCommand(
          calendarDatabase, view, sourceDate, targetCalendar, targetDate);
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException("Invalid date format in copy command", e);
    }
  }

  /**
   * Creates a command to copy events between dates.
   * Syntax: copy events between 2025-05-05 and 2025-05-10 --target WorkCal to 2025-06-01
   */
  private InCommand createCopyEventsBetween(Matcher m) {
    try {
      LocalDate sourceStart = DateTimeParser.parseDate(m.group(1));
      LocalDate sourceEnd = DateTimeParser.parseDate(m.group(2));
      String targetCalendar = m.group(3);
      LocalDate targetStart = DateTimeParser.parseDate(m.group(4));

      return new CopyEventsBetweenDatesCommand(
          calendarDatabase, view, sourceStart, sourceEnd,
          targetCalendar, targetStart);
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException("Invalid date format in copy command", e);
    }
  }

  /**
   * Gets the event service for the active calendar.
   * FIXED: Now throws RuntimeException instead of checked exception
   * to work properly with lambda expressions in command factory map.
   *
   * @return the event service for active calendar
   * @throws ActiveCalendarRequiredException if no calendar is active
   */
  private InEventService getActiveEventService() {
    InCalendar activeCalendar = calendarDatabase.getActiveCalendar();
    if (activeCalendar == null) {
      throw new ActiveCalendarRequiredException(
          "No calendar is active. Use 'use calendar --name <name>' first.");
    }
    return new EventService(activeCalendar);
  }

  /**
   * Creates a single event with specific start and end times.
   */
  private InCommand createSingleEventWithTime(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String subject = cleanSubject(m.group(1));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(2));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(3));
      return CreateEventCommand.forSingleEvent(
          eventService, view, subject, start, end, new HashMap<>());
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in create command", e);
    }
  }

  /**
   * Creates a recurring event series with occurrence count.
   */
  private InCommand createRecurringEventWithCount(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String subject = cleanSubject(m.group(1));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(2));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(3));
      Set<Weekday> weekdays = parseWeekdays(m.group(4));
      int count = Integer.parseInt(m.group(5));
      return CreateEventCommand.forSeriesWithCount(
          eventService, view, subject, start, end, weekdays, count,
          new HashMap<>());
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in recurring command", e);
    } catch (InvalidCommandException e) {
      throw new RuntimeException(
          "Invalid weekday specification", e);
    } catch (NumberFormatException e) {
      throw new RuntimeException(
          "Invalid occurrence count", e);
    }
  }

  /**
   * Creates a recurring event series until a specific date.
   */
  private InCommand createRecurringEventUntilDate(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String subject = cleanSubject(m.group(1));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(2));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(3));
      Set<Weekday> weekdays = parseWeekdays(m.group(4));
      LocalDate endDate = DateTimeParser.parseDate(m.group(5));
      return CreateEventCommand.forSeriesWithEndDate(
          eventService, view, subject, start, end, weekdays, endDate,
          new HashMap<>());
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in recurring command", e);
    } catch (InvalidCommandException e) {
      throw new RuntimeException(
          "Invalid weekday specification", e);
    }
  }

  /**
   * Creates a single all-day event.
   */
  private InCommand createAllDaySingle(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String subject = cleanSubject(m.group(1));
      LocalDate date = DateTimeParser.parseDate(m.group(2));
      LocalDateTime start = date.atTime(8, 0);
      return CreateEventCommand.forSingleEvent(
          eventService, view, subject, start, null, new HashMap<>());
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date format in all-day event command", e);
    }
  }

  /**
   * Creates a recurring all-day event series with occurrence count.
   */
  private InCommand createAllDayWithCount(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String subject = cleanSubject(m.group(1));
      LocalDate date = DateTimeParser.parseDate(m.group(2));
      LocalDateTime start = date.atTime(8, 0);
      Set<Weekday> weekdays = parseWeekdays(m.group(3));
      int count = Integer.parseInt(m.group(4));
      return CreateEventCommand.forSeriesWithCount(
          eventService, view, subject, start, null, weekdays, count,
          new HashMap<>());
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date format in all-day recurring command", e);
    } catch (InvalidCommandException e) {
      throw new RuntimeException(
          "Invalid weekday specification", e);
    } catch (NumberFormatException e) {
      throw new RuntimeException(
          "Invalid occurrence count", e);
    }
  }

  /**
   * Creates a recurring all-day event series until a specific date.
   */
  private InCommand createAllDayUntilDate(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String subject = cleanSubject(m.group(1));
      LocalDate date = DateTimeParser.parseDate(m.group(2));
      LocalDateTime start = date.atTime(8, 0);
      Set<Weekday> weekdays = parseWeekdays(m.group(3));
      LocalDate endDate = DateTimeParser.parseDate(m.group(4));
      return CreateEventCommand.forSeriesWithEndDate(
          eventService, view, subject, start, null, weekdays, endDate,
          new HashMap<>());
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date format in all-day recurring command", e);
    } catch (InvalidCommandException e) {
      throw new RuntimeException(
          "Invalid weekday specification", e);
    }
  }

  /**
   * Creates an edit command using subject and time range for identification.
   * Syntax: edit event subject "Old Subject" from START to END with "New Subject"
   */
  private InCommand createEditEventBySubjectAndTimeRange(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String oldSubject = m.group(1);
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(2));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(3));
      String newSubject = m.group(4);

      return new EditEventCommand(
          eventService, view, oldSubject, start, "subject", newSubject, "single");
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in edit command", e);
    }
  }

  /**
   * Creates an edit command with specified edit type.
   */
  private InCommand createEditCommand(Matcher m, String editType) {
    try {
      InEventService eventService = getActiveEventService();
      String property = m.group(1);
      String subject = cleanSubject(m.group(2));
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(3));
      String newValue = m.group(4).trim();

      return new EditEventCommand(
          eventService, view, subject, start, property, newValue, editType);
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in edit command", e);
    }
  }

  /**
   * Creates a query command for a specific date.
   */
  private InCommand createQueryByDate(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      LocalDate date = DateTimeParser.parseDate(m.group(1));
      return new QueryEventCommand(eventService, view, date, null, null);
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date format in query command", e);
    }
  }

  /**
   * Creates a query command for a date range.
   */
  private InCommand createQueryByRange(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      LocalDateTime start = DateTimeParser.parseDateTime(m.group(1));
      LocalDateTime end = DateTimeParser.parseDateTime(m.group(2));
      return new QueryEventCommand(eventService, view, null, start, end);
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in query command", e);
    }
  }

  /**
   * Creates a show status command.
   */
  private InCommand createShowStatus(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      LocalDateTime dateTime = DateTimeParser.parseDateTime(m.group(1));
      return new ShowStatusCommand(eventService, view, dateTime);
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in show status command", e);
    }
  }

  /**
   * Creates an export command.
   */
  private InCommand createExport(Matcher m) {
    InCalendar activeCalendar = calendarDatabase.getActiveCalendar();
    if (activeCalendar == null) {
      throw new ActiveCalendarRequiredException(
          "No calendar is active. Use 'use calendar --name <name>' first.");
    }

    String filename = m.group(1).trim();
    return new ExportCommand(exportService, view, activeCalendar, Paths.get(filename));
  }

  /**
   * Creates an all-day event when only start date-time is provided (no end time).
   */
  private InCommand createAllDayFromDateTime(Matcher m) {
    try {
      InEventService eventService = getActiveEventService();
      String subject = cleanSubject(m.group(1));
      LocalDateTime startDateTime = DateTimeParser.parseDateTime(m.group(2));

      LocalDate date = startDateTime.toLocalDate();
      LocalDateTime start = date.atTime(8, 0);

      return CreateEventCommand.forSingleEvent(
          eventService, view, subject, start, null, new HashMap<>());
    } catch (InvalidDateTimeException e) {
      throw new RuntimeException(
          "Invalid date/time format in all-day event command", e);
    }
  }

  /**
   * Cleans the subject string by removing surrounding quotes if present.
   */
  private String cleanSubject(String subject) {
    String trimmed = subject.trim();
    if (trimmed.startsWith("\"") && trimmed.endsWith("\"")
        && trimmed.length() >= 2) {
      return trimmed.substring(1, trimmed.length() - 1);
    }
    return trimmed;
  }

  /**
   * Parses a weekday string into a set of Weekday enums.
   */
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

    return weekdays;
  }
}