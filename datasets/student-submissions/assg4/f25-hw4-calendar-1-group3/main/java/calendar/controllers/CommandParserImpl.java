package calendar.controllers;

import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.models.EventProperty;
import calendar.models.Location;
import calendar.models.RecurrenceRule;
import calendar.models.RecurrenceRuleImpl;
import calendar.models.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for parsing commands and returning an {@link Command} representing the parsed
 * command.
 */
public class CommandParserImpl implements CommandParser {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
  private final CommandFactory factory;

  /**
   * Constructor to initialize CommandParserImpl object with CommandFactory.
   *
   * @param factory instance of command factory
   */
  public CommandParserImpl(CommandFactory factory) {
    this.factory = Objects.requireNonNull(factory, "Factory must not be null");
  }

  @Override
  public Command parse(String commandLine) throws CommandParseException {
    if (commandLine == null || commandLine.trim().isEmpty()) {
      throw new CommandParseException("Command cannot be empty");
    }

    String trimmed = commandLine.trim();

    if (trimmed.startsWith("create event ")) {
      return parseCreateEvent(trimmed);
    } else if (trimmed.startsWith("edit event ")) {
      return parseEditEvent(trimmed);
    } else if (trimmed.startsWith("edit events ")) {
      return parseEditEvents(trimmed);
    } else if (trimmed.startsWith("edit series ")) {
      return parseEditSeries(trimmed);
    } else if (trimmed.startsWith("print events on ")) {
      return parsePrintEventsOn(trimmed);
    } else if (trimmed.startsWith("print events from ")) {
      return parsePrintEventsFrom(trimmed);
    } else if (trimmed.startsWith("export cal ")) {
      return parseExportCal(trimmed);
    } else if (trimmed.startsWith("show status on ")) {
      return parseShowStatus(trimmed);
    } else {
      throw new CommandParseException("Unknown command: " + trimmed);
    }
  }


  private Command parseCreateEvent(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("create event ".length());

    Pattern p1 = Pattern.compile("\"([^\"]+)\" from (\\S+) to (\\S+)$");
    Matcher m1 = p1.matcher(rest);
    if (m1.matches()) {
      String subject = m1.group(1);
      LocalDateTime start = parseDateTime(m1.group(2));
      LocalDateTime end = parseDateTime(m1.group(3));
      return factory.createCreateEventCommand(subject, start, end);
    }

    Pattern p2 = Pattern.compile("(\\S+) from (\\S+) to (\\S+)$");
    Matcher m2 = p2.matcher(rest);
    if (m2.matches()) {
      String subject = m2.group(1);
      LocalDateTime start = parseDateTime(m2.group(2));
      LocalDateTime end = parseDateTime(m2.group(3));
      return factory.createCreateEventCommand(subject, start, end);
    }

    Pattern p3 =
        Pattern.compile("\"([^\"]+)\" from (\\S+) to (\\S+) repeats (\\S+) for (\\d+) times$");
    Matcher m3 = p3.matcher(rest);
    if (m3.matches()) {
      String subject = m3.group(1);
      LocalDateTime start = parseDateTime(m3.group(2));
      LocalDateTime end = parseDateTime(m3.group(3));
      Set<DayOfWeek> days = parseWeekdays(m3.group(4));
      int count = Integer.parseInt(m3.group(5));
      Event event =
          EventImpl.getBuilder().subject(subject).from(start.toLocalDate(), start.toLocalTime())
              .to(end.toLocalDate(), end.toLocalTime()).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, count);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    Pattern p4 = Pattern.compile("(\\S+) from (\\S+) to (\\S+) repeats (\\S+) for (\\d+) times$");
    Matcher m4 = p4.matcher(rest);
    if (m4.matches()) {
      String subject = m4.group(1);
      LocalDateTime start = parseDateTime(m4.group(2));
      LocalDateTime end = parseDateTime(m4.group(3));
      Set<DayOfWeek> days = parseWeekdays(m4.group(4));
      int count = Integer.parseInt(m4.group(5));
      Event event =
          EventImpl.getBuilder().subject(subject).from(start.toLocalDate(), start.toLocalTime())
              .to(end.toLocalDate(), end.toLocalTime()).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, count);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    Pattern p5 = Pattern.compile("\"([^\"]+)\" from (\\S+) to (\\S+) repeats (\\S+) until (\\S+)$");
    Matcher m5 = p5.matcher(rest);
    if (m5.matches()) {
      String subject = m5.group(1);
      LocalDateTime start = parseDateTime(m5.group(2));
      LocalDateTime end = parseDateTime(m5.group(3));
      Set<DayOfWeek> days = parseWeekdays(m5.group(4));
      LocalDate until = parseDate(m5.group(5));
      Event event =
          EventImpl.getBuilder().subject(subject).from(start.toLocalDate(), start.toLocalTime())
              .to(end.toLocalDate(), end.toLocalTime()).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, until);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    Pattern p6 = Pattern.compile("(\\S+) from (\\S+) to (\\S+) repeats (\\S+) until (\\S+)$");
    Matcher m6 = p6.matcher(rest);
    if (m6.matches()) {
      String subject = m6.group(1);
      LocalDateTime start = parseDateTime(m6.group(2));
      LocalDateTime end = parseDateTime(m6.group(3));
      Set<DayOfWeek> days = parseWeekdays(m6.group(4));
      LocalDate until = parseDate(m6.group(5));
      Event event =
          EventImpl.getBuilder().subject(subject).from(start.toLocalDate(), start.toLocalTime())
              .to(end.toLocalDate(), end.toLocalTime()).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, until);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    Pattern p7 = Pattern.compile("\"([^\"]+)\" on (\\S+)$");
    Matcher m7 = p7.matcher(rest);
    if (m7.matches()) {
      String subject = m7.group(1);
      LocalDate date = parseDate(m7.group(2));
      return factory.createCreateAllDayEventCommand(subject, date);
    }

    Pattern p8 = Pattern.compile("(\\S+) on (\\S+)$");
    Matcher m8 = p8.matcher(rest);
    if (m8.matches()) {
      String subject = m8.group(1);
      LocalDate date = parseDate(m8.group(2));
      return factory.createCreateAllDayEventCommand(subject, date);
    }

    Pattern p9 = Pattern.compile("\"([^\"]+)\" on (\\S+) repeats (\\S+) for (\\d+) times$");
    Matcher m9 = p9.matcher(rest);
    if (m9.matches()) {
      String subject = m9.group(1);
      LocalDate date = parseDate(m9.group(2));
      Set<DayOfWeek> days = parseWeekdays(m9.group(3));
      int count = Integer.parseInt(m9.group(4));
      Event event = EventImpl.getBuilder().subject(subject).on(date).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, count);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    Pattern p10 = Pattern.compile("(\\S+) on (\\S+) repeats (\\S+) for (\\d+) times$");
    Matcher m10 = p10.matcher(rest);
    if (m10.matches()) {
      String subject = m10.group(1);
      LocalDate date = parseDate(m10.group(2));
      Set<DayOfWeek> days = parseWeekdays(m10.group(3));
      int count = Integer.parseInt(m10.group(4));
      Event event = EventImpl.getBuilder().subject(subject).on(date).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, count);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    Pattern p11 = Pattern.compile("\"([^\"]+)\" on (\\S+) repeats (\\S+) until (\\S+)$");
    Matcher m11 = p11.matcher(rest);
    if (m11.matches()) {
      String subject = m11.group(1);
      LocalDate date = parseDate(m11.group(2));
      Set<DayOfWeek> days = parseWeekdays(m11.group(3));
      LocalDate until = parseDate(m11.group(4));
      Event event = EventImpl.getBuilder().subject(subject).on(date).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, until);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    Pattern p12 = Pattern.compile("(\\S+) on (\\S+) repeats (\\S+) until (\\S+)$");
    Matcher m12 = p12.matcher(rest);
    if (m12.matches()) {
      String subject = m12.group(1);
      LocalDate date = parseDate(m12.group(2));
      Set<DayOfWeek> days = parseWeekdays(m12.group(3));
      LocalDate until = parseDate(m12.group(4));
      Event event = EventImpl.getBuilder().subject(subject).on(date).build();
      RecurrenceRule rule = new RecurrenceRuleImpl(days, until);
      return factory.createCreateEventSeriesCommand(event, rule);
    }

    throw new CommandParseException("Invalid create event command: " + commandLine);
  }

  /**
   * Parses edit event command using regex pattern matching. Format: edit event {property} {subject}
   * from {dateTime} to {dateTime} with {value}
   *
   * @param commandLine the full command line
   * @return EditEventCommand
   * @throws CommandParseException if parsing fails
   */
  private Command parseEditEvent(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("edit event ".length()).trim();

    Pattern pattern = Pattern.compile(
        "(subject|start|end|description|location|status)\\s+(?:\"([^\"]+)\"|(\\S+))"
            + "\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+with\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(rest);

    if (!matcher.matches()) {
      throw new CommandParseException("Invalid edit event command format. Expected: "
          + "edit event <property> <subject> from <dateTime> to <dateTime> with <value>");
    }

    String propertyStr = matcher.group(1);
    String subject = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
    String startDateTimeStr = matcher.group(4);
    String endDateTimeStr = matcher.group(5);
    String newValueRaw = matcher.group(6).trim();

    String newValue = removeQuotes(newValueRaw);
    EventProperty property = getEventProperty(propertyStr);
    validateNewValue(property, newValue);

    LocalDateTime startDateTime = parseDateTime(startDateTimeStr);
    LocalDateTime endDateTime = parseDateTime(endDateTimeStr);

    return factory.createEditEventCommand(property, subject, startDateTime, endDateTime, newValue);
  }

  /**
   * Parses edit series command using regex pattern matching. Format: edit series {property}
   * {subject} from {dateTime} with {value}
   *
   * @param commandLine the full command line
   * @return EditSeriesCommand
   * @throws CommandParseException if parsing fails
   */
  private Command parseEditSeries(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("edit series ".length()).trim();

    Pattern pattern = Pattern.compile(
        "(subject|start|end|description|location|status)\\s+(?:\"([^\"]+)\"|(\\S+))"
            + "\\s+from\\s+(\\S+)\\s+with\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(rest);

    if (!matcher.matches()) {
      throw new CommandParseException("Invalid edit series command format. Expected: "
          + "edit series <property> <subject> from <dateTime> with <value>");
    }

    String propertyStr = matcher.group(1);
    String subject = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
    String startDateTimeStr = matcher.group(4);
    String newValueRaw = matcher.group(5).trim();

    String newValue = removeQuotes(newValueRaw);
    EventProperty property = getEventProperty(propertyStr);
    validateNewValue(property, newValue);

    LocalDateTime startDateTime = parseDateTime(startDateTimeStr);

    return factory.createEditSeriesCommand(property, subject, startDateTime, newValue);
  }

  /**
   * Parses edit events command using regex pattern matching. Format: edit events {property}
   * {subject} from {dateTime} with {value}
   *
   * @param commandLine the full command line
   * @return EditEventsCommand
   * @throws CommandParseException if parsing fails
   */
  private Command parseEditEvents(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("edit events ".length()).trim();

    Pattern pattern = Pattern.compile(
        "(subject|start|end|description|location|status)\\s+(?:\"([^\"]+)\"|(\\S+))"
            + "\\s+from\\s+(\\S+)\\s+with\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(rest);

    if (!matcher.matches()) {
      throw new CommandParseException("Invalid edit events command format. Expected: "
          + "edit events <property> <subject> from <dateTime> with <value>");
    }

    String propertyStr = matcher.group(1);
    String subject = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
    String startDateTimeStr = matcher.group(4);
    String newValueRaw = matcher.group(5).trim();

    String newValue = removeQuotes(newValueRaw);

    EventProperty property = getEventProperty(propertyStr);
    validateNewValue(property, newValue);

    LocalDateTime startDateTime = parseDateTime(startDateTimeStr);

    return factory.createEditEventsCommand(property, subject, startDateTime, newValue);
  }

  private Command parsePrintEventsOn(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("print events on ".length()).trim();
    LocalDate date = parseDate(rest);
    return factory.createQueryEventsByDateCommand(date);
  }

  private Command parsePrintEventsFrom(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("print events from ".length());

    Pattern p = Pattern.compile("(\\S+) to (\\S+)$");
    Matcher m = p.matcher(rest);
    if (m.matches()) {
      LocalDateTime start = parseDateTime(m.group(1));
      LocalDateTime end = parseDateTime(m.group(2));
      return factory.createQueryEventsByDateRangeCommand(start, end);
    }

    throw new CommandParseException("Invalid print events from command: " + commandLine);
  }

  private Command parseExportCal(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("export cal ".length()).trim();
    return factory.createExportCsvCommand(rest);
  }

  private Command parseShowStatus(String commandLine) throws CommandParseException {
    String rest = commandLine.substring("show status on ".length()).trim();
    LocalDateTime dateTime = parseDateTime(rest);
    return factory.createShowStatusCommand(dateTime);
  }

  private LocalDate parseDate(String dateStr) throws CommandParseException {
    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new CommandParseException(
          "Invalid date format: " + dateStr + ". Expected format: YYYY-MM-DD");
    }
  }

  private LocalDateTime parseDateTime(String dateTimeStr) throws CommandParseException {
    try {
      return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new CommandParseException(
          "Invalid datetime format: " + dateTimeStr + ". Expected format: YYYY-MM-DDTHH:mm");
    }
  }

  /**
   * Parse weekday string like "MWF" into Set of DayOfWeek. M=Monday, T=Tuesday, W=Wednesday,
   * R=Thursday, F=Friday, S=Saturday, U=Sunday
   */
  private Set<DayOfWeek> parseWeekdays(String weekdayStr) throws CommandParseException {
    Set<DayOfWeek> days = new LinkedHashSet<>();

    for (char c : weekdayStr.toCharArray()) {
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
          throw new CommandParseException(
              "Invalid weekday character: " + c + ". Valid: M, T, W, R, F, S, U");
      }
    }

    if (days.isEmpty()) {
      throw new CommandParseException("At least one weekday must be specified");
    }

    return days;
  }

  private EventProperty getEventProperty(String propertyStr) throws CommandParseException {
    switch (propertyStr) {
      case "subject":
        return EventProperty.SUBJECT;
      case "start":
        return EventProperty.START_DATE_TIME;
      case "end":
        return EventProperty.END_DATE_TIME;
      case "description":
        return EventProperty.DESCRIPTION;
      case "location":
        return EventProperty.LOCATION;
      case "status":
        return EventProperty.STATUS;
      default:
        throw new CommandParseException("Command contains invalid property");
    }
  }

  /**
   * Validates the new value based on property type.
   *
   * @param property the property being edited
   * @param value    the new value
   * @throws CommandParseException if validation fails
   */
  private void validateNewValue(EventProperty property, String value) throws CommandParseException {

    switch (property) {
      case SUBJECT:
      case DESCRIPTION:
        if (value.trim().isEmpty()) {
          throw new CommandParseException(property + " cannot be empty");
        }
        break;

      case START_DATE_TIME:
      case END_DATE_TIME:
        parseDateTime(value);
        break;

      case LOCATION:
        try {
          Location.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
          throw new CommandParseException("Invalid location: " + value);
        }
        break;

      case STATUS:
        try {
          Status.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
          throw new CommandParseException("Invalid status: " + value);
        }
        break;
      default:
        throw new CommandParseException("Invalid Command");
    }
  }

  private String removeQuotes(String str) {
    if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
      str = str.substring(1, str.length() - 1);
    }
    return str;
  }
}
