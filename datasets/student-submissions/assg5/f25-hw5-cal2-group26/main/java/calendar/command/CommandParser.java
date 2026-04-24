package calendar.command;

import calendar.command.impl.CopyEventCommand;
import calendar.command.impl.CopyEventsBetweenCommand;
import calendar.command.impl.CopyEventsOnCommand;
import calendar.command.impl.CreateAllDayEventCommand;
import calendar.command.impl.CreateCalendarCommand;
import calendar.command.impl.CreateEventCommand;
import calendar.command.impl.CreateRecurringAllDayEventCommand;
import calendar.command.impl.CreateRecurringEventCommand;
import calendar.command.impl.DeleteCalendarCommand;
import calendar.command.impl.EditCalendarCommand;
import calendar.command.impl.EditEventsFromCommand;
import calendar.command.impl.EditSeriesCommand;
import calendar.command.impl.EditSeriesConditionalCommand;
import calendar.command.impl.EditSingleEventCommand;
import calendar.command.impl.ExitCommand;
import calendar.command.impl.ExportCalendarCommand;
import calendar.command.impl.GetActiveCalendarCommand;
import calendar.command.impl.ListCalendarsCommand;
import calendar.command.impl.PrintEventsBetweenCommand;
import calendar.command.impl.PrintEventsOnCommand;
import calendar.command.impl.ShowStatusCommand;
import calendar.command.impl.UseCalendarCommand;
import calendar.controller.CalendarControllerImpl;
import calendar.controller.CalendarManagerControllerImpl;
import calendar.model.EventStatus;
import calendar.model.Weekday;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses text input and creates CommandInterface objects.
 * This is the factory for commands.
 */
public class CommandParser {

  private static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("America/New_York");

  private final CalendarControllerImpl eventController;
  private final CalendarManagerControllerImpl managerController;

  private static class CommandEntry {
    final Pattern pattern;
    final Function<Matcher, CommandInterface> parser;
    final String description;

    CommandEntry(Pattern pattern, Function<Matcher, CommandInterface> parser, String description) {
      this.pattern = pattern;
      this.parser = parser;
      this.description = description;
    }
  }

  private final List<CommandEntry> commandRegistry = new ArrayList<>();

  /**
   * Create parser with both controllers injected.
   * Commands get the controller they need at construction time.
   */
  public CommandParser(CalendarControllerImpl eventController,
                       CalendarManagerControllerImpl managerController) {
    this.eventController = eventController;
    this.managerController = managerController;
    registerCommands();
  }

  /**
   * Parses the given input string into a CommandInterface. Returns null if the input is
   * blank or does not match any registered command pattern.
   *
   * @param input the raw command string to parse.
   * @return a command object if matched, null otherwise.
   */
  public CommandInterface parse(String input) {
    if (input == null || input.trim().isEmpty()) {
      return null;
    }
    input = input.trim();

    for (CommandEntry entry : commandRegistry) {
      Matcher matcher = entry.pattern.matcher(input);
      if (matcher.matches()) {
        return entry.parser.apply(matcher);
      }
    }
    return null;
  }

  /**
   * Returns a list of all command descriptions in the order they were registered.
   *
   * @return unmodifiable list of command help strings.
   */
  public List<String> getAvailableCommands() {
    List<String> help = new ArrayList<>();
    for (CommandEntry entry : commandRegistry) {
      help.add(entry.description);
    }
    return help;
  }

  private void registerCommands() {
    register("^exit$", m -> new ExitCommand(), "exit");

    register("^create\\s+calendar\\s+--name\\s+(\\S+)\\s+--timezone\\s+(\\S+)$",
        m -> new CreateCalendarCommand(managerController, m.group(1), ZoneId.of(m.group(2))),
        "create calendar --name <name> --timezone <zone>");

    register("^edit\\s+calendar\\s+--name\\s+(\\S+)\\s+--property\\s+(\\S+)\\s+(.+)$",
        m -> new EditCalendarCommand(managerController, m.group(1), m.group(2), m.group(3).trim()),
        "edit calendar --name <name> --property <property> <value>");

    register("^use\\s+calendar\\s+--name\\s+(\\S+)$",
        m -> new UseCalendarCommand(managerController, m.group(1)),
        "use calendar --name <name>");

    register("^list\\s+calendars$", m -> new ListCalendarsCommand(managerController),
        "list calendars");

    register("^delete\\s+calendar\\s+--name\\s+(\\S+)$",
        m -> new DeleteCalendarCommand(managerController, m.group(1)),
        "delete calendar --name <name>");

    register("^get\\s+active\\s+calendar$", m -> new GetActiveCalendarCommand(managerController),
        "get active calendar");

    register(
        "^copy\\s+event\\s+\"([^\"]+)\"\\s+on\\s+(\\S+)\\s+--target\\s+(\\S+)\\s+to\\s+(\\S+)$",
        m -> new CopyEventCommand(managerController, m.group(1), parseDateOrDateTime(m.group(2)),
            m.group(3), parseDateOrDateTime(m.group(4))),
        "copy event \"<name>\" on <datetime> --target <calendar> to <datetime>");

    register("^copy\\s+events\\s+on\\s+(\\S+)\\s+--target\\s+(\\S+)\\s+to\\s+(\\S+)$",
        m -> new CopyEventsOnCommand(managerController, parseDate(m.group(1)),
            m.group(2), parseDate(m.group(3))),
        "copy events on <date> --target <calendar> to <date>");

    register(
        "^copy\\s+events\\s+between\\s+(\\S+)\\s+and\\s+(\\S+)\\s+--target\\s+(\\S+)\\s"
            + "+to\\s+(\\S+)$",
        m -> new CopyEventsBetweenCommand(managerController, parseDate(m.group(1)),
            parseDate(m.group(2)), m.group(3), parseDate(m.group(4))),
        "copy events between <date> and <date> --target <calendar> to <date>");

    register("^export\\s+cal\\s+(\\S+\\.(csv|ical|ics))$",
        m -> new ExportCalendarCommand(managerController, m.group(1)),
        "export cal <filename.csv|filename.ical|filename.ics>");

    register(
        "^create\\s+event\\s+\"([^\"]+)\"\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)(?:\\s"
            + "+status\\s+(\\S+))?$",
        m -> new CreateEventCommand(eventController, m.group(1), parseDateTime(m.group(2)),
            parseDateTime(m.group(3)),
            parseOptionalStatus(m.groupCount() >= 4 ? m.group(4) : null)),
        "create event \"<name>\" from <datetime> to <datetime> [status <status>]");

    register(
        "^create\\s+event\\s+\"([^\"]+)\"\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+repeats\\s"
            + "+([MTWRFSU]+)\\s+for\\s+(\\d+)\\s+times$",
        m -> new CreateRecurringEventCommand(eventController, m.group(1), parseDateTime(m.group(2)),
            parseDateTime(m.group(3)), parseWeekdays(m.group(4)), Integer.parseInt(m.group(5)),
            null, EventStatus.PUBLIC),
        "create event \"<name>\" from <datetime> to <datetime> repeats <days> for <n> times");

    register(
        "^create\\s+event\\s+\"([^\"]+)\"\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+repeats\\s"
            + "+([MTWRFSU]+)\\s+until\\s+(\\S+)$",
        m -> new CreateRecurringEventCommand(eventController, m.group(1), parseDateTime(m.group(2)),
            parseDateTime(m.group(3)), parseWeekdays(m.group(4)), null,
            parseDate(m.group(5)), EventStatus.PUBLIC),
        "create event \"<name>\" from <datetime> to <datetime> repeats <days> until <date>");

    register("^create\\s+event\\s+\"([^\"]+)\"\\s+on\\s+(\\S+)$",
        m -> new CreateAllDayEventCommand(eventController, m.group(1),
            parseDate(m.group(2)), EventStatus.PUBLIC),
        "create event \"<name>\" on <date>");

    register(
        "^create\\s+event\\s+\"([^\"]+)\"\\s+on\\s+(\\S+)\\s+repeats\\s+([MTWRFSU]+)\\s"
            + "+for\\s+(\\d+)\\s+times$",
        m -> new CreateRecurringAllDayEventCommand(eventController, m.group(1),
            parseDate(m.group(2)),
            parseWeekdays(m.group(3)), Integer.parseInt(m.group(4)), null, EventStatus.PUBLIC),
        "create event \"<name>\" on <date> repeats <days> for <n> times");

    register(
        "^create\\s+event\\s+\"([^\"]+)\"\\s+on\\s+(\\S+)\\s+repeats\\s+([MTWRFSU]+)\\s"
            + "+until\\s+(\\S+)$",
        m -> new CreateRecurringAllDayEventCommand(eventController, m.group(1),
            parseDate(m.group(2)),
            parseWeekdays(m.group(3)), null, parseDate(m.group(4)), EventStatus.PUBLIC),
        "create event \"<name>\" on <date> repeats <days> until <date>");

    register(
        "^edit\\s+event\\s+(\\w+)\\s+\"([^\"]+)\"\\s+from\\s+(\\S+)\\s+to\\s"
            + "+(\\S+)\\s+with\\s+(.+)$",
        m -> new EditSingleEventCommand(eventController, m.group(2), parseDateTime(m.group(3)),
            parseDateTime(m.group(4)), m.group(1),
            parsePropertyValue(m.group(1), stripQuotes(m.group(5)))),
        "edit event <property> \"<name>\" from <datetime> to <datetime> with <value>");

    register("^edit\\s+events\\s+(\\w+)\\s+\"([^\"]+)\"\\s+from\\s+(\\S+)\\s+with\\s+(.+)$",
        m -> new EditEventsFromCommand(eventController, m.group(2), parseDateTime(m.group(3)),
            m.group(1), parsePropertyValue(m.group(1), stripQuotes(m.group(4)))),
        "edit events <property> \"<name>\" from <datetime> with <value>");

    register("^edit\\s+series\\s+(\\w+)\\s+\"([^\"]+)\"\\s+from\\s+(\\S+)\\s+with\\s+(.+)$",
        m -> new EditSeriesCommand(eventController, m.group(2), parseDateOrDateTime(m.group(3)),
            m.group(1), parsePropertyValue(m.group(1), stripQuotes(m.group(4)))),
        "edit series <property> \"<name>\" from <datetime> with <value>");

    register(
        "^edit\\s+series\\s+(\\w+)\\s+\"([^\"]+)\"\\s+from\\s+(\\S+)\\s+with\\s"
            + "+(.+?)\\s+only\\s+(before|after)\\s+(\\S+)$",
        m -> new EditSeriesConditionalCommand(eventController, m.group(2),
            parseDateOrDateTime(m.group(3)),
            m.group(1), parsePropertyValue(m.group(1), stripQuotes(m.group(4))),
            parseDate(m.group(6)), m.group(5).equalsIgnoreCase("before")),
        "edit series <property> \"<name>\" from <datetime> with <value> only "
            + "<before|after> <date>");

    register("^print\\s+events\\s+on\\s+(\\S+)$",
        m -> new PrintEventsOnCommand(eventController, LocalDate.parse(m.group(1))),
        "print events on <date>");

    register(
        "^print\\s+events\\s+(?:from\\s+(\\S+)\\s+to\\s+(\\S+)|between\\s"
            + "+(\\S+)\\s+and\\s+(\\S+))$",
        m -> {
          String startStr = m.group(1) != null ? m.group(1) : m.group(3);
          String endStr = m.group(2) != null ? m.group(2) : m.group(4);
          return new PrintEventsBetweenCommand(eventController, parseDateOrDateTime(startStr),
              parseDateOrDateTime(endStr));
        },
        "print events from <datetime> to <datetime>");

    register("^show\\s+status\\s+on\\s+(\\S+)$",
        m -> new ShowStatusCommand(eventController, parseDateTime(m.group(1))),
        "show status on <datetime>");
  }

  private void register(String regex, Function<Matcher, CommandInterface> parser,
                        String description) {
    commandRegistry.add(new CommandEntry(Pattern.compile(regex, Pattern.CASE_INSENSITIVE),
        parser, description));
  }

  private static String stripQuotes(String s) {
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }

  /**
   * Get the timezone to use for parsing dates/times.
   * Uses the active calendar's timezone if available, otherwise falls back to default.
   */
  private ZoneId getParsingTimezone() {
    try {
      if (managerController != null && managerController.getActiveCalendar() != null) {
        return managerController.getActiveCalendar().getCalendarZone();
      }
    } catch (Exception e) {
      // Fall through to default
    }
    return DEFAULT_TIMEZONE;
  }

  private ZonedDateTime parseDateTime(String s) {
    String[] parts = s.split("T");
    return ZonedDateTime.of(LocalDate.parse(parts[0]), LocalTime.parse(parts[1]),
        getParsingTimezone());
  }

  private ZonedDateTime parseDateOrDateTime(String s) {
    return s.contains("T") ? parseDateTime(s) : parseDate(s);
  }

  private ZonedDateTime parseDate(String s) {
    return ZonedDateTime.of(LocalDate.parse(s), LocalTime.of(8, 0), getParsingTimezone());
  }

  private static List<Weekday> parseWeekdays(String s) {
    List<Weekday> list = new ArrayList<>();
    for (char c : s.toCharArray()) {
      switch (c) {
        case 'M':
          list.add(Weekday.MONDAY);
          break;
        case 'T':
          list.add(Weekday.TUESDAY);
          break;
        case 'W':
          list.add(Weekday.WEDNESDAY);
          break;
        case 'R':
          list.add(Weekday.THURSDAY);
          break;
        case 'F':
          list.add(Weekday.FRIDAY);
          break;
        case 'S':
          list.add(Weekday.SATURDAY);
          break;
        case 'U':
          list.add(Weekday.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }
    return list;
  }

  private static EventStatus parseOptionalStatus(String s) {
    if (s == null) {
      return EventStatus.PUBLIC;
    }
    return s.equalsIgnoreCase("private") ? EventStatus.PRIVATE : EventStatus.PUBLIC;
  }

  private Object parsePropertyValue(String property, String value) {
    switch (property.toLowerCase()) {
      case "start":
      case "end":
        return parseDateTime(value);
      case "status":
        return EventStatus.valueOf(value.toUpperCase());
      default:
        return value;
    }
  }
}