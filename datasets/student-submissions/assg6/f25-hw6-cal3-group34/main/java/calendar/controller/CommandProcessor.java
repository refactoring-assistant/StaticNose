package calendar.controller;

import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.EventProperty;
import calendar.model.EventStatus;
import calendar.util.CommandTokenizer;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

/** Parses and executes user commands against the {@link CalendarModel}. */
public class CommandProcessor {
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.US);
  private static final DateTimeFormatter DISPLAY_TIME =
      DateTimeFormatter.ofPattern("HH:mm", Locale.US);

  private final CalendarModel model;

  /**
   * Creates a command processor that operates on the supplied model.
   *
   * @param model the calendar model
   */
  public CommandProcessor(CalendarModel model) {
    this.model = model;
  }

  /**
   * Processes the supplied command string.
   *
   * @param rawCommand user command
   * @return the command result
   * @throws CommandException if the command fails to parse or execute
   */
  public CommandResult process(String rawCommand) throws CommandException {
    if (rawCommand == null || rawCommand.trim().isEmpty()) {
      return CommandResult.message("");
    }
    List<String> tokens = tokenize(rawCommand);
    if (tokens.isEmpty()) {
      return CommandResult.message("");
    }
    String keyword = tokens.get(0).toLowerCase(Locale.US);
    switch (keyword) {
      case "create":
        return handleCreate(tokens);
      case "edit":
        return handleEdit(tokens);
      case "print":
        return handlePrint(tokens);
      case "export":
        return handleExport(tokens);
      case "show":
        return handleShow(tokens);
      case "use":
        return handleUse(tokens);
      case "copy":
        return handleCopy(tokens);
      case "exit":
        ensureTokenCount(tokens, 1, "exit command takes no additional arguments.");
        return CommandResult.exit("Exiting calendar.");
      default:
        throw new CommandException("Unknown command: " + tokens.get(0));
    }
  }

  private CommandResult handleCreate(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 3, "create command is incomplete.");
    String target = tokens.get(1).toLowerCase(Locale.US);
    if ("calendar".equals(target)) {
      return handleCreateCalendar(tokens);
    }
    if (!"event".equals(target)) {
      throw new CommandException("Only event creation is supported.");
    }
    ensureCalendarInUse();
    int indexOfFrom = indexOf(tokens, "from", 2);
    int indexOfOn = indexOf(tokens, "on", 2);
    if (indexOfFrom >= 0 && indexOfOn >= 0) {
      throw new CommandException("Specify either 'from ... to ...' or 'on <date>', not both.");
    }
    if (indexOfFrom < 0 && indexOfOn < 0) {
      throw new CommandException("Missing 'from' or 'on' clause.");
    }

    if (indexOfFrom >= 0) {
      return createTimedEvent(tokens, indexOfFrom);
    } else {
      return createAllDayEvent(tokens, indexOfOn);
    }
  }

  private CommandResult handleCreateCalendar(List<String> tokens) throws CommandException {
    String name =
        requireOptionValue(
            tokens, "--name", 2, "create calendar requires '--name <calendarName>'.");
    String zoneText =
        requireOptionValue(
            tokens, "--timezone", 2, "create calendar requires '--timezone area/location'.");
    ZoneId zone = parseZone(zoneText);
    try {
      model.createCalendar(name, zone);
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
    return CommandResult.message("Created calendar \"" + name + "\" (" + zone + ").");
  }

  private CommandResult createTimedEvent(List<String> tokens, int indexOfFrom)
      throws CommandException {
    int indexOfTo = indexOf(tokens, "to", indexOfFrom + 1);
    if (indexOfTo < 0) {
      throw new CommandException("Timed events must include a 'to' clause.");
    }
    String subject = join(tokens, 2, indexOfFrom);
    if (subject.isEmpty()) {
      throw new CommandException("Event subject cannot be empty.");
    }
    LocalDateTime start = parseDateTime(tokens.get(indexOfFrom + 1), "start date/time");
    LocalDateTime end = parseDateTime(tokens.get(indexOfTo + 1), "end date/time");
    boolean allDay = false;

    RecurrenceInput recurrence = parseRecurrence(tokens, indexOfTo + 2);
    if (!recurrence.hasRecurrence()) {
      try {
        CalendarEvent event = model.createEvent(subject, start, end, allDay);
        return CommandResult.message(formatSingleEventCreated(event));
      } catch (IllegalArgumentException e) {
        throw new CommandException(e.getMessage(), e);
      }
    }

    try {
      List<CalendarEvent> events = recurrence.createTimed(model, subject, start, end, allDay);
      return CommandResult.message(formatSeriesCreated(subject, events.size(), recurrence));
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
  }

  private CommandResult createAllDayEvent(List<String> tokens, int indexOfOn)
      throws CommandException {
    String subject = join(tokens, 2, indexOfOn);
    if (subject.isEmpty()) {
      throw new CommandException("Event subject cannot be empty.");
    }
    LocalDate date = parseDate(tokens.get(indexOfOn + 1), "date");
    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(8, 0));
    LocalDateTime end = LocalDateTime.of(date, LocalTime.of(17, 0));
    boolean allDay = true;

    RecurrenceInput recurrence = parseRecurrence(tokens, indexOfOn + 2);
    if (!recurrence.hasRecurrence()) {
      try {
        CalendarEvent event = model.createEvent(subject, start, end, allDay);
        return CommandResult.message(formatSingleEventCreated(event));
      } catch (IllegalArgumentException e) {
        throw new CommandException(e.getMessage(), e);
      }
    }

    try {
      List<CalendarEvent> events = recurrence.createAllDay(model, subject, start, end, allDay);
      return CommandResult.message(formatSeriesCreated(subject, events.size(), recurrence));
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
  }

  private RecurrenceInput parseRecurrence(List<String> tokens, int startIndex)
      throws CommandException {
    if (startIndex >= tokens.size()) {
      return RecurrenceInput.none();
    }
    if (!tokens.get(startIndex).equalsIgnoreCase("repeats")) {
      throw new CommandException("Unexpected trailing tokens after create command.");
    }
    ensureTokenAtLeast(tokens, startIndex + 2, "Missing weekday information for repeats.");
    String weekdaysToken = tokens.get(startIndex + 1);
    Set<DayOfWeek> weekdays = parseWeekdays(weekdaysToken);
    if (startIndex + 2 >= tokens.size()) {
      throw new CommandException("Missing recurrence termination (for/until).");
    }
    String clause = tokens.get(startIndex + 2).toLowerCase(Locale.US);
    if ("for".equals(clause)) {
      ensureTokenAtLeast(tokens, startIndex + 5, "Incomplete 'for N times' clause.");
      int occurrences = parsePositiveInt(tokens.get(startIndex + 3), "occurrence count");
      if (!tokens.get(startIndex + 4).equalsIgnoreCase("times")) {
        throw new CommandException("Recurrence must be specified as 'for <N> times'.");
      }
      ensureNoTrailing(tokens, startIndex + 5);
      return RecurrenceInput.forOccurrences(weekdays, occurrences);
    } else if ("until".equals(clause)) {
      ensureTokenAtLeast(tokens, startIndex + 4, "Missing 'until' date.");
      LocalDate until = parseDate(tokens.get(startIndex + 3), "until date");
      ensureNoTrailing(tokens, startIndex + 4);
      return RecurrenceInput.untilDate(weekdays, until);
    } else {
      throw new CommandException("Recurrence must end with 'for ... times' or 'until <date>'.");
    }
  }

  private CommandResult handleEdit(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 2, "edit command is incomplete.");
    String scope = tokens.get(1).toLowerCase(Locale.US);
    switch (scope) {
      case "calendar":
        return handleEditCalendar(tokens);
      case "event":
        return handleEditSingle(tokens);
      case "events":
        return handleEditSeriesFrom(tokens);
      case "series":
        return handleEditSeries(tokens);
      default:
        throw new CommandException("Unknown edit scope: " + tokens.get(1));
    }
  }

  private CommandResult handleEditCalendar(List<String> tokens) throws CommandException {
    String name =
        requireOptionValue(tokens, "--name", 2, "edit calendar requires '--name <calendarName>'.");
    int propertyIdx = indexOf(tokens, "--property", 2);
    if (propertyIdx < 0 || propertyIdx + 2 >= tokens.size()) {
      throw new CommandException("edit calendar requires '--property <name|timezone> <value>'.");
    }
    String property = tokens.get(propertyIdx + 1).toLowerCase(Locale.US);
    String value = tokens.get(propertyIdx + 2);
    try {
      switch (property) {
        case "name":
          model.renameCalendar(name, value);
          return CommandResult.message("Renamed calendar to \"" + value + "\".");
        case "timezone":
          ZoneId zone = parseZone(value);
          model.changeCalendarTimezone(name, zone);
          return CommandResult.message("Updated timezone for \"" + name + "\" to " + zone + ".");
        default:
          throw new CommandException("Unknown calendar property: " + property);
      }
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
  }

  private CommandResult handleEditSingle(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 7, "Incomplete edit event command.");
    EventProperty property = parseProperty(tokens.get(2));
    int indexOfFrom = indexOf(tokens, "from", 3);
    int indexOfTo = indexOf(tokens, "to", indexOfFrom + 1);
    int indexOfWith = indexOf(tokens, "with", indexOfTo + 1);
    if (indexOfFrom < 0 || indexOfTo < 0 || indexOfWith < 0) {
      throw new CommandException("Edit event command must include from/to/with clauses.");
    }
    String subject = join(tokens, 3, indexOfFrom);
    if (subject.isEmpty()) {
      throw new CommandException("Event subject cannot be empty.");
    }
    LocalDateTime start = parseDateTime(tokens.get(indexOfFrom + 1), "start date/time");
    LocalDateTime end = parseDateTime(tokens.get(indexOfTo + 1), "end date/time");
    Object newValue = parseNewValue(property, join(tokens, indexOfWith + 1, tokens.size()));
    CalendarEvent updated = model.editSingleEvent(subject, start, end, property, newValue);
    return CommandResult.message(formatEditResult(property, updated, "instance"));
  }

  private CommandResult handleEditSeriesFrom(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 6, "Incomplete edit events command.");
    EventProperty property = parseProperty(tokens.get(2));
    int indexOfFrom = indexOf(tokens, "from", 3);
    int indexOfWith = indexOf(tokens, "with", indexOfFrom + 1);
    if (indexOfFrom < 0 || indexOfWith < 0) {
      throw new CommandException("Edit events command must include from/with clauses.");
    }
    String subject = join(tokens, 3, indexOfFrom);
    if (subject.isEmpty()) {
      throw new CommandException("Event subject cannot be empty.");
    }
    LocalDateTime start = parseDateTime(tokens.get(indexOfFrom + 1), "start date/time");
    Object newValue = parseNewValue(property, join(tokens, indexOfWith + 1, tokens.size()));
    List<CalendarEvent> updated = model.editEventsFrom(subject, start, property, newValue);
    return CommandResult.message(formatEditSeriesResult(property, updated, "from event onward"));
  }

  private CommandResult handleEditSeries(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 6, "Incomplete edit series command.");
    EventProperty property = parseProperty(tokens.get(2));
    int indexOfFrom = indexOf(tokens, "from", 3);
    int indexOfWith = indexOf(tokens, "with", indexOfFrom + 1);
    if (indexOfFrom < 0 || indexOfWith < 0) {
      throw new CommandException("Edit series command must include from/with clauses.");
    }
    String subject = join(tokens, 3, indexOfFrom);
    if (subject.isEmpty()) {
      throw new CommandException("Event subject cannot be empty.");
    }
    LocalDateTime start = parseDateTime(tokens.get(indexOfFrom + 1), "start date/time");
    Object newValue = parseNewValue(property, join(tokens, indexOfWith + 1, tokens.size()));
    List<CalendarEvent> updated = model.editEntireSeries(subject, start, property, newValue);
    return CommandResult.message(formatEditSeriesResult(property, updated, "entire series"));
  }

  private CommandResult handlePrint(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 3, "Incomplete print command.");
    if (!tokens.get(1).equalsIgnoreCase("events")) {
      throw new CommandException("Only printing events is supported.");
    }
    ensureCalendarInUse();
    ZoneId zone = model.getActiveCalendarZone();
    String mode = tokens.get(2).toLowerCase(Locale.US);
    if ("on".equals(mode)) {
      ensureTokenCount(tokens, 4, "print events on requires a date argument.");
      LocalDate date = parseDate(tokens.get(3), "date");
      List<CalendarEvent> events = model.eventsOn(date);
      return CommandResult.message(formatEventsOn(date, events, zone));
    } else if ("from".equals(mode)) {
      int indexOfTo = indexOf(tokens, "to", 3);
      if (indexOfTo < 0) {
        throw new CommandException("print events from command must include a 'to' clause.");
      }
      LocalDateTime start = parseDateTime(tokens.get(3), "start date/time");
      LocalDateTime end = parseDateTime(tokens.get(indexOfTo + 1), "end date/time");
      ensureNoTrailing(tokens, indexOfTo + 2);
      List<CalendarEvent> events = model.eventsBetween(start, end);
      return CommandResult.message(formatEventsBetween(start, end, events, zone));
    } else {
      throw new CommandException("Unsupported print command.");
    }
  }

  private CommandResult handleExport(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 2, "Incomplete export command.");
    if (!tokens.get(1).equalsIgnoreCase("cal")) {
      throw new CommandException("Unknown export target: " + tokens.get(1));
    }
    String fileName = join(tokens, 2, tokens.size());
    if (fileName.isEmpty()) {
      throw new CommandException("Export command requires a file name.");
    }
    try {
      Path requestedPath = Path.of(fileName);
      model.exportCalendar(requestedPath);
      return CommandResult.message("Exported calendar to " + requestedPath.toAbsolutePath());
    } catch (IOException e) {
      throw new CommandException("Failed to export calendar: " + e.getMessage(), e);
    }
  }

  private CommandResult handleUse(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 2, "use command requires a target.");
    if (!tokens.get(1).equalsIgnoreCase("calendar")) {
      throw new CommandException("Only calendar usage can be set.");
    }
    String name =
        requireOptionValue(tokens, "--name", 2, "use calendar requires '--name <calendarName>'.");
    try {
      model.useCalendar(name);
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
    ZoneId zone = model.getActiveCalendarZone();
    return CommandResult.message("Using calendar \"" + name + "\" (" + zone + ").");
  }

  private CommandResult handleCopy(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 2, "copy command requires a target (event/events).");
    ensureCalendarInUse();
    String target = tokens.get(1).toLowerCase(Locale.US);
    if ("event".equals(target)) {
      return handleCopySingleEvent(tokens);
    }
    if ("events".equals(target)) {
      return handleCopyEvents(tokens);
    }
    throw new CommandException("Unknown copy target: " + tokens.get(1));
  }

  private CommandResult handleCopySingleEvent(List<String> tokens) throws CommandException {
    int onIndex = indexOf(tokens, "on", 2);
    if (onIndex < 0 || onIndex + 1 >= tokens.size()) {
      throw new CommandException("copy event requires 'on <dateTime>'.");
    }
    String subject = join(tokens, 2, onIndex);
    if (subject.isEmpty()) {
      throw new CommandException("Event name cannot be empty.");
    }
    LocalDateTime start = parseDateTime(tokens.get(onIndex + 1), "source date/time");
    int targetOptionIndex = indexOf(tokens, "--target", onIndex + 2);
    if (targetOptionIndex < 0 || targetOptionIndex + 1 >= tokens.size()) {
      throw new CommandException("copy event requires '--target <calendarName>'.");
    }
    String targetName = tokens.get(targetOptionIndex + 1);
    int toIndex = indexOf(tokens, "to", targetOptionIndex + 2);
    if (toIndex < 0 || toIndex + 1 >= tokens.size()) {
      throw new CommandException("copy event requires 'to <dateTime>'.");
    }
    LocalDateTime targetStart = parseDateTime(tokens.get(toIndex + 1), "target date/time");
    try {
      CalendarEvent created = model.copyEvent(subject, start, targetName, targetStart);
      return CommandResult.message(
          "Copied event \""
              + created.getSubject()
              + "\" to "
              + targetName
              + " starting "
              + created.getStart()
              + ".");
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
  }

  private CommandResult handleCopyEvents(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 3, "copy events command requires 'on' or 'between'.");
    String mode = tokens.get(2).toLowerCase(Locale.US);
    switch (mode) {
      case "on":
        return handleCopyEventsOn(tokens);
      case "between":
        return handleCopyEventsBetween(tokens);
      default:
        throw new CommandException("Unknown copy events mode: " + tokens.get(2));
    }
  }

  private CommandResult handleCopyEventsOn(List<String> tokens) throws CommandException {
    if (tokens.size() < 7) {
      throw new CommandException("copy events on requires '<date> --target <calendar> to <date>'.");
    }
    LocalDate sourceDate = parseDate(tokens.get(3), "source date");
    int targetOptionIndex = indexOf(tokens, "--target", 4);
    if (targetOptionIndex < 0 || targetOptionIndex + 1 >= tokens.size()) {
      throw new CommandException("copy events on requires '--target <calendarName>'.");
    }
    String targetName = tokens.get(targetOptionIndex + 1);
    int toIndex = indexOf(tokens, "to", targetOptionIndex + 2);
    if (toIndex < 0 || toIndex + 1 >= tokens.size()) {
      throw new CommandException("copy events on requires 'to <date>'.");
    }
    LocalDate targetDate = parseDate(tokens.get(toIndex + 1), "target date");
    try {
      List<CalendarEvent> created = model.copyEventsOn(sourceDate, targetName, targetDate);
      return CommandResult.message(
          "Copied "
              + created.size()
              + " event(s) from "
              + sourceDate
              + " to "
              + targetDate
              + " in "
              + targetName
              + ".");
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
  }

  private CommandResult handleCopyEventsBetween(List<String> tokens) throws CommandException {
    int betweenIdx = 3;
    if (tokens.size() <= betweenIdx + 4 || !tokens.get(betweenIdx + 1).equalsIgnoreCase("and")) {
      throw new CommandException(
          "copy events between requires 'between <date> and <date> --target <calendar> to"
              + " <date>'.");
    }
    LocalDate startDate = parseDate(tokens.get(betweenIdx), "start date");
    LocalDate endDate = parseDate(tokens.get(betweenIdx + 2), "end date");
    int targetOptionIndex = indexOf(tokens, "--target", betweenIdx + 3);
    if (targetOptionIndex < 0 || targetOptionIndex + 1 >= tokens.size()) {
      throw new CommandException("copy events between requires '--target <calendarName>'.");
    }
    String targetName = tokens.get(targetOptionIndex + 1);
    int toIndex = indexOf(tokens, "to", targetOptionIndex + 2);
    if (toIndex < 0 || toIndex + 1 >= tokens.size()) {
      throw new CommandException("copy events between requires 'to <date>'.");
    }
    LocalDate targetStart = parseDate(tokens.get(toIndex + 1), "target date");
    try {
      List<CalendarEvent> created =
          model.copyEventsBetween(startDate, endDate, targetName, targetStart);
      return CommandResult.message(
          "Copied "
              + created.size()
              + " event(s) from interval "
              + startDate
              + " to "
              + endDate
              + " into "
              + targetName
              + " starting "
              + targetStart
              + ".");
    } catch (IllegalArgumentException e) {
      throw new CommandException(e.getMessage(), e);
    }
  }

  private CommandResult handleShow(List<String> tokens) throws CommandException {
    ensureTokenAtLeast(tokens, 2, "Incomplete show command.");
    String scope = tokens.get(1).toLowerCase(Locale.US);
    switch (scope) {
      case "status":
        ensureTokenAtLeast(tokens, 4, "show status requires 'on <dateTime>'.");
        if (!tokens.get(2).equalsIgnoreCase("on")) {
          throw new CommandException("Show status command must use 'on <dateTime>'.");
        }
        ensureCalendarInUse();
        LocalDateTime moment = parseDateTime(tokens.get(3), "date/time");
        boolean busy = model.isBusy(moment);
        String status = busy ? "busy" : "available";
        return CommandResult.message("Status on " + moment + ": " + status);
      case "calendars":
        ensureTokenCount(tokens, 2, "show calendars takes no additional arguments.");
        return handleShowCalendars();
      default:
        throw new CommandException("Unknown show target: " + tokens.get(1));
    }
  }

  private CommandResult handleShowCalendars() {
    List<String> calendars = model.listCalendars();
    if (calendars.isEmpty()) {
      return CommandResult.message("No calendars have been created.");
    }
    String active = model.hasActiveCalendar() ? model.getActiveCalendarName() : null;
    StringBuilder builder = new StringBuilder("Calendars:\n");
    for (String name : calendars) {
      builder.append("- ").append(name);
      if (active != null && active.equals(name)) {
        builder.append(" (active)");
      }
      builder.append('\n');
    }
    return CommandResult.message(builder.toString().trim());
  }

  private EventProperty parseProperty(String token) throws CommandException {
    try {
      return EventProperty.fromToken(token);
    } catch (IllegalArgumentException e) {
      throw new CommandException("Unknown property: " + token, e);
    }
  }

  private Object parseNewValue(EventProperty property, String rawValue) throws CommandException {
    if (property == null) {
      throw new CommandException("Unsupported property: null");
    }
    switch (property) {
      case SUBJECT:
      case DESCRIPTION:
      case LOCATION:
        if (rawValue == null || rawValue.trim().isEmpty()) {
          throw new CommandException("New value cannot be empty.");
        }
        return rawValue.trim();
      case STATUS:
        try {
          return EventStatus.fromString(rawValue);
        } catch (IllegalArgumentException e) {
          throw new CommandException("Unrecognized status: " + rawValue, e);
        }
      case START:
      case END:
        return parseDateTime(rawValue, property.name().toLowerCase(Locale.US) + " date/time");
      default:
        throw new CommandException("Unsupported property: " + property);
    }
  }

  private String formatSingleEventCreated(CalendarEvent event) {
    return "Created event \""
        + event.getSubject()
        + "\" starting "
        + event.getStart()
        + " and ending "
        + event.getEnd()
        + ".";
  }

  private String formatSeriesCreated(String subject, int count, RecurrenceInput recurrence) {
    String scope = recurrence.describe();
    return "Created series \"" + subject + "\" with " + count + " event(s) " + scope + ".";
  }

  private String formatEditResult(EventProperty property, CalendarEvent event, String scope) {
    return "Updated "
        + scope
        + " for \""
        + event.getSubject()
        + "\" ("
        + property.name().toLowerCase(Locale.US)
        + ").";
  }

  private String formatEditSeriesResult(
      EventProperty property, List<CalendarEvent> events, String scope) {
    if (events.isEmpty()) {
      return "No events were updated.";
    }
    String subject = events.get(0).getSubject();
    return "Updated "
        + events.size()
        + " event(s) for \""
        + subject
        + "\" ("
        + scope
        + ", "
        + property.name().toLowerCase(Locale.US)
        + ").";
  }

  private String formatEventsOn(LocalDate date, List<CalendarEvent> events, ZoneId zone) {
    if (events.isEmpty()) {
      return "No events scheduled on " + date + ".";
    }
    StringBuilder builder = new StringBuilder();
    builder.append("Events on ").append(date).append(":\n");
    for (CalendarEvent event : events) {
      builder
          .append("- ")
          .append(event.getSubject())
          .append(" from ")
          .append(event.getStart())
          .append(" to ")
          .append(event.getEnd());
      event.getLocation().ifPresent(loc -> builder.append(" @ ").append(loc));
      builder.append('\n');
    }
    return builder.toString().trim();
  }

  private String formatEventsBetween(
      LocalDateTime start, LocalDateTime end, List<CalendarEvent> events, ZoneId zone) {
    if (events.isEmpty()) {
      return "No events between " + start + " and " + end + ".";
    }
    StringBuilder builder = new StringBuilder();
    builder.append("Events between ").append(start).append(" and ").append(end).append(":\n");
    for (CalendarEvent event : events) {
      builder
          .append("- ")
          .append(event.getSubject())
          .append(" starting on ")
          .append(event.getStart().toLocalDate())
          .append(" at ")
          .append(DISPLAY_TIME.format(event.getStart()))
          .append(", ending on ")
          .append(event.getEnd().toLocalDate())
          .append(" at ")
          .append(DISPLAY_TIME.format(event.getEnd()));
      event.getLocation().ifPresent(loc -> builder.append(" @ ").append(loc));
      builder.append('\n');
    }
    return builder.toString().trim();
  }

  private Set<DayOfWeek> parseWeekdays(String token) throws CommandException {
    if (token == null || token.trim().isEmpty()) {
      throw new CommandException("Weekday specification cannot be empty.");
    }
    Set<DayOfWeek> days = new LinkedHashSet<>();
    String upper = token.trim().toUpperCase(Locale.US);
    for (char ch : upper.toCharArray()) {
      days.add(dayFromChar(ch));
    }
    return days;
  }

  private DayOfWeek dayFromChar(char ch) throws CommandException {
    switch (ch) {
      case 'M':
        return DayOfWeek.MONDAY;
      case 'T':
        return DayOfWeek.TUESDAY;
      case 'W':
        return DayOfWeek.WEDNESDAY;
      case 'R':
        return DayOfWeek.THURSDAY;
      case 'F':
        return DayOfWeek.FRIDAY;
      case 'S':
        return DayOfWeek.SATURDAY;
      case 'U':
        return DayOfWeek.SUNDAY;
      default:
        throw new CommandException("Unknown weekday code: " + ch);
    }
  }

  private LocalDate parseDate(String token, String label) throws CommandException {
    try {
      return LocalDate.parse(token, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new CommandException("Invalid " + label + ": " + token, e);
    }
  }

  private LocalDateTime parseDateTime(String token, String label) throws CommandException {
    try {
      return LocalDateTime.parse(token, DATE_TIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new CommandException("Invalid " + label + ": " + token, e);
    }
  }

  private void ensureCalendarInUse() throws CommandException {
    if (!model.hasActiveCalendar()) {
      throw new CommandException(
          "No calendar is currently in use. Run 'use calendar --name <name>' first.");
    }
  }

  private ZoneId parseZone(String zoneText) throws CommandException {
    try {
      return ZoneId.of(zoneText);
    } catch (DateTimeException e) {
      throw new CommandException("Invalid timezone: " + zoneText, e);
    }
  }

  private String requireOptionValue(
      List<String> tokens, String option, int startIndex, String errorMessage)
      throws CommandException {
    int idx = indexOf(tokens, option, startIndex);
    if (idx < 0 || idx + 1 >= tokens.size()) {
      throw new CommandException(errorMessage);
    }
    return tokens.get(idx + 1);
  }

  private List<String> tokenize(String rawCommand) throws CommandException {
    try {
      return CommandTokenizer.tokenize(rawCommand);
    } catch (IllegalArgumentException e) {
      throw new CommandException("Invalid command syntax: " + e.getMessage(), e);
    }
  }

  private int indexOf(List<String> tokens, String target, int startIndex) {
    for (int i = startIndex; i < tokens.size(); i++) {
      if (tokens.get(i).equalsIgnoreCase(target)) {
        return i;
      }
    }
    return -1;
  }

  private void ensureTokenAtLeast(List<String> tokens, int required, String message)
      throws CommandException {
    if (tokens.size() < required) {
      throw new CommandException(message);
    }
  }

  private void ensureTokenCount(List<String> tokens, int expected, String message)
      throws CommandException {
    if (tokens.size() != expected) {
      throw new CommandException(message);
    }
  }

  private void ensureNoTrailing(List<String> tokens, int startIndex) throws CommandException {
    if (startIndex < tokens.size()) {
      throw new CommandException("Unexpected trailing arguments.");
    }
  }

  private String join(List<String> tokens, int startInclusive, int endExclusive) {
    if (startInclusive >= endExclusive) {
      return "";
    }
    StringJoiner joiner = new StringJoiner(" ");
    for (int i = startInclusive; i < endExclusive; i++) {
      joiner.add(tokens.get(i));
    }
    return joiner.toString();
  }

  private int parsePositiveInt(String token, String label) throws CommandException {
    try {
      int value = Integer.parseInt(token);
      if (value <= 0) {
        throw new NumberFormatException("Value must be positive.");
      }
      return value;
    } catch (NumberFormatException e) {
      throw new CommandException("Invalid " + label + ": " + token, e);
    }
  }

  private static class RecurrenceInput {
    private final Set<DayOfWeek> weekdays;
    private final Integer occurrences;
    private final LocalDate until;

    private RecurrenceInput(Set<DayOfWeek> weekdays, Integer occurrences, LocalDate until) {
      this.weekdays = weekdays;
      this.occurrences = occurrences;
      this.until = until;
    }

    static RecurrenceInput none() {
      return new RecurrenceInput(null, null, null);
    }

    static RecurrenceInput forOccurrences(Set<DayOfWeek> weekdays, int occurrences) {
      return new RecurrenceInput(weekdays, occurrences, null);
    }

    static RecurrenceInput untilDate(Set<DayOfWeek> weekdays, LocalDate until) {
      return new RecurrenceInput(weekdays, null, until);
    }

    boolean hasRecurrence() {
      return weekdays != null;
    }

    List<CalendarEvent> createTimed(
        CalendarModel model,
        String subject,
        LocalDateTime start,
        LocalDateTime end,
        boolean allDay) {
      if (occurrences != null) {
        return model.createRecurringEventsByCount(
            subject, start, end, allDay, weekdays, occurrences);
      }
      return model.createRecurringEventsUntil(subject, start, end, allDay, weekdays, until);
    }

    List<CalendarEvent> createAllDay(
        CalendarModel model,
        String subject,
        LocalDateTime start,
        LocalDateTime end,
        boolean allDay) {
      return createTimed(model, subject, start, end, allDay);
    }

    String describe() {
      if (occurrences != null) {
        return "repeating on " + weekdays + " for " + occurrences + " time(s)";
      }
      return "repeating on " + weekdays + " until " + until;
    }
  }
}
