package controller;

import interpreter.CommandInterpreter;
import interpreter.CommandInterpreter.CommandMatch;
import interpreter.CommandInterpreter.CommandType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import messaging.Messages;
import model.Calendar;
import model.CalendarStore;
import utils.CalendarCopier;



/**
 * Top-level controller that manages multiple calendars and delegates event commands to a
 * per-calendar {@link CommandController}.
 */
public class CalendarController implements CommandProcessor {

  private final CalendarStore calendarStore;
  private final CommandInterpreter interpreter;
  private CommandController activeController;

  /**
   * Builds a controller with a fresh calendar store.
   */
  public CalendarController() {
    this(new CalendarStore());
  }

  /**
   * Builds a controller around an existing store.
   *
   * @param store calendar store
   */
  public CalendarController(CalendarStore store) {
    this.calendarStore = Objects.requireNonNull(store, "store");
    this.interpreter = new CommandInterpreter();
    initializeDefaultCalendar();
  }

  /**
   * Returns the currently active calendar, or {@code null} if none is selected.
   *
   * @return active calendar
   */
  public Calendar getActiveCalendar() {
    return calendarStore.getActiveCalendar();
  }

  /**
   * Returns the display name of the active calendar, or a placeholder when none is set.
   *
   * @return active calendar name or fallback text
   */
  public String getActiveCalendarName() {
    Calendar active = calendarStore.getActiveCalendar();
    return active != null ? active.getName() : "No calendar selected";
  }

  /**
   * Returns the active calendar's timezone, or {@code null} when none exists.
   *
   * @return active calendar timezone
   */
  public ZoneId getActiveCalendarZone() {
    Calendar active = calendarStore.getActiveCalendar();
    return active != null ? active.getTimezone() : null;
  }

  /**
   * Returns a user-friendly timezone description for the active calendar.
   *
   * @return formatted timezone string
   */
  public String getActiveCalendarTimezoneText() {
    ZoneId zone = getActiveCalendarZone();
    return zone != null ? "Timezone: " + zone : "Timezone: (none)";
  }

  /**
   * Returns all calendars in insertion order.
   *
   * @return ordered calendars
   */
  public List<Calendar> getCalendars() {
    return calendarStore.getCalendars();
  }

  /**
   * Selects a calendar to become active.
   *
   * @param name calendar name
   */
  public void selectCalendar(String name) {
    Calendar calendar = calendarStore.setActiveCalendar(name);
    this.activeController = new CommandController(calendar);
  }

  /**
   * Creates a calendar and makes it active.
   *
   * @param name     calendar name
   * @param timezone timezone id (Area/Location)
   * @return created calendar
   */
  public Calendar createCalendar(String name, String timezone) {
    Calendar created = calendarStore.createCalendar(name, timezone);
    this.activeController = new CommandController(created);
    return created;
  }

  /**
   * Ensure there is always an active calendar. If none exists, create a default
   * calendar in the system timezone and hook up an active controller for it.
   */
  private void initializeDefaultCalendar() {
    if (calendarStore.hasActiveCalendar()) {
      this.activeController = new CommandController(calendarStore.getActiveCalendar());
      return;
    }
    Calendar defaultCal =
        calendarStore.createCalendar("default", ZoneId.systemDefault().getId());
    this.activeController = new CommandController(defaultCal);
    Messages.info("Using default calendar in " + defaultCal.getTimezone() + ".");
  }

  @Override
  public void interpret(String command) {
    CommandMatch match = interpreter.interpret(command);
    CommandType type = match.type();

    switch (type) {
      case CREATE_CALENDAR:
        handleCreateCalendar(match);
        return;
      case EDIT_CALENDAR:
        handleEditCalendar(match);
        return;
      case LIST_CALENDARS:
        handleListCalendars();
        return;
      case USE_CALENDAR:
        handleUseCalendar(match);
        return;
      case COPY_EVENT_SINGLE:
        handleCopySingleEvent(match);
        return;
      case COPY_EVENTS_ON:
        handleCopyEventsOn(match);
        return;
      case COPY_EVENTS_BETWEEN:
        handleCopyEventsBetween(match);
        return;
      case HELP:
        printHelp();
        return;
      case EXIT:
        return;
      case UNKNOWN:
        emitUnknown(match);
        return;
      default:
        break;
    }

    if (requiresActiveCalendar(type)) {
      if (activeController == null) {
        Messages.error("No calendar in use. Create/use a calendar first.");
        return;
      }
      activeController.interpret(command);
      return;
    }

    emitUnknown(match);
  }

  private void handleCreateCalendar(CommandMatch match) {
    if (calendarStore == null) {
      Messages.error("No calendar store configured.");
      return;
    }
    try {
      String name = match.matcher().group("calendarName");
      String timezone = match.matcher().group("timezone");
      Calendar calendar = calendarStore.createCalendar(name, timezone);
      Messages.info(
          "Created calendar: " + calendar.getName() + " (" + calendar.getTimezone() + ")");
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  private void handleEditCalendar(CommandMatch match) {
    String name = match.matcher().group("calendarName");
    String property = match.matcher().group("calendarProperty").toLowerCase();
    String value = match.matcher().group("calendarValue").trim();
    try {
      switch (property) {
        case "name":
          Calendar renamed = calendarStore.renameCalendar(name, value);
          Messages.info("Renamed calendar to: " + renamed.getName());
          break;
        case "timezone":
          Calendar updated = calendarStore.updateTimezone(name, value);
          Messages.info("Updated timezone for " + updated.getName()
              + " to " + updated.getTimezone());
          break;
        default:
          Messages.error("Unsupported calendar property: " + property);
          break;
      }
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  private void handleListCalendars() {
    if (calendarStore == null) {
      Messages.error("No calendars available.");
      return;
    }
    List<Calendar> calendars = calendarStore.getCalendars();
    if (calendars.isEmpty()) {
      Messages.error("No calendars found.");
      return;
    }
    Messages.info("Calendars:");
    for (Calendar cal : calendars) {
      Messages.info(" - " + cal.getName() + " (" + cal.getTimezone() + ")");
    }
  }

  private void handleUseCalendar(CommandMatch match) {
    if (calendarStore == null) {
      Messages.error("No calendar store configured.");
      return;
    }
    String name = match.matcher().group("calendarName");
    try {
      Calendar calendar = calendarStore.setActiveCalendar(name);
      this.activeController = new CommandController(calendar);
      Messages.info("Using calendar: " + calendar.getName());
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Prints management help text and delegates to the active controller for event-level help.
   */
  private void printHelp() {
    Messages.info("----------------- CALENDAR MANAGEMENT --------------");
    Messages.info("  create calendar --name <name> --timezone <Area/Location>");
    Messages.info("  edit calendar --name <name> --property <name|timezone> <value>");
    Messages.info("  list calendars");
    Messages.info("  use calendar --name <name>");
    Messages.info("  copy event <subject> on <start> --target <calendar> to <start>");
    Messages.info("  copy events on <date> --target <calendar> to <date>");
    Messages.info("  copy events between <start> and <end> --target <calendar> to <date>");
    if (activeController == null) {
      Messages.info("");
      Messages.info(
          "Select a calendar to view event commands (use 'use calendar --name ...').");
    } else {
      Messages.info("");
      activeController.help();
    }
  }

  /**
   * Returns whether a given command type requires an active calendar context.
   *
   * @param type command type
   * @return true if the command must be handled by the active calendar
   */
  private boolean requiresActiveCalendar(CommandType type) {
    switch (type) {
      case CREATE_CALENDAR:
      case EDIT_CALENDAR:
      case LIST_CALENDARS:
      case USE_CALENDAR:
      case COPY_EVENT_SINGLE:
      case COPY_EVENTS_ON:
      case COPY_EVENTS_BETWEEN:
      case HELP:
      case EXIT:
      case UNKNOWN:
        return false;
      default:
        return true;
    }
  }

  /**
   * Emits a friendly error when a command cannot be parsed.
   *
   * @param match parsed match (possibly UNKNOWN)
   */
  private void emitUnknown(CommandMatch match) {
    String normalized = match.normalized();
    String message = (normalized == null || normalized.isBlank())
        ? "ERROR: No command provided."
        : "ERROR: Could not understand command: " + normalized;
    Messages.error(message);
    // Do not throw here; keep the session running.
  }

  /**
   * Retrieves the currently active calendar, printing an error if none is available.
   *
   * @return active calendar or {@code null} when missing
   */
  private Calendar requireActiveCalendar() {
    if (calendarStore == null) {
      Messages.error("No calendar store configured.");
      return null;
    }
    Calendar active = calendarStore.getActiveCalendar();
    if (active == null) {
      Messages.error("No calendar in use. Create/use a calendar first.");
    }
    return active;
  }

  /**
   * Finds a calendar by name in the store and reports any lookup failures to the user.
   *
   * @param name target calendar name
   * @return calendar instance or {@code null} if not found
   */
  private Calendar resolveTargetCalendar(String name) {
    try {
      return calendarStore.getCalendar(name);
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
      return null;
    }
  }

  /**
   * Extracts the subject token from a command match, handling quoted forms.
   *
   * @param match parsed command
   * @return normalized subject text
   */
  private String extractSubject(CommandMatch match) {
    var matcher = match.matcher();
    String quoted = matcher.group("subjectQuoted");
    if (quoted != null) {
      return quoted.substring(1, quoted.length() - 1);
    }
    return matcher.group("subjectPlain");
  }

  /**
   * Handles {@code copy event ...} commands that copy a single occurrence into another calendar.
   *
   * @param match parsed command match
   */
  private void handleCopySingleEvent(CommandMatch match) {
    Calendar source = requireActiveCalendar();
    if (source == null) {
      return;
    }
    var matcher = match.matcher();
    LocalDateTime sourceStart;
    LocalDateTime targetStart;
    try {
      sourceStart = LocalDateTime.parse(matcher.group("sourceStart"));
      targetStart = LocalDateTime.parse(matcher.group("targetStart"));
    } catch (DateTimeParseException e) {
      Messages.error(QueryController.DATETIME_ERROR);
      return;
    }
    String subject = extractSubject(match);
    if (subject == null || subject.isBlank()) {
      Messages.error("Subject is required.");
      return;
    }
    String targetName = matcher.group("targetCalendar");
    Calendar target = resolveTargetCalendar(targetName);
    if (target == null) {
      return;
    }
    try {
      int copied = CalendarCopier.copySingleEvent(
          source, target, subject, sourceStart, targetStart);
      if (copied == 0) {
        Messages.error("No matching event found to copy.");
      } else {
        Messages.info("Copied " + copied + " event to " + target.getName() + ".");
      }
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles {@code copy events on ...} commands which copy a day's events to a new day.
   *
   * @param match parsed command match
   */
  private void handleCopyEventsOn(CommandMatch match) {
    Calendar source = requireActiveCalendar();
    if (source == null) {
      return;
    }
    var matcher = match.matcher();
    LocalDate sourceDate;
    LocalDate targetDate;
    try {
      sourceDate = LocalDate.parse(matcher.group("sourceDate"));
      targetDate = LocalDate.parse(matcher.group("targetDate"));
    } catch (DateTimeParseException e) {
      Messages.error(QueryController.DATE_ERROR);
      return;
    }
    Calendar target = resolveTargetCalendar(matcher.group("targetCalendar"));
    if (target == null) {
      return;
    }
    try {
      int copied = CalendarCopier.copyEventsOn(
          source, target, sourceDate, targetDate);
      Messages.info("Copied " + copied + " event(s) to " + target.getName() + ".");
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles {@code copy events between ... and ...} commands that copy a date window.
   *
   * @param match parsed command match
   */
  private void handleCopyEventsBetween(CommandMatch match) {
    Calendar source = requireActiveCalendar();
    if (source == null) {
      return;
    }
    var matcher = match.matcher();
    LocalDate rangeStart;
    LocalDate rangeEnd;
    LocalDate targetStart;
    try {
      rangeStart = LocalDate.parse(matcher.group("rangeStart"));
      rangeEnd = LocalDate.parse(matcher.group("rangeEnd"));
      targetStart = LocalDate.parse(matcher.group("targetStartDate"));
    } catch (DateTimeParseException e) {
      Messages.error(QueryController.DATE_ERROR);
      return;
    }
    if (rangeEnd.isBefore(rangeStart)) {
      Messages.error("Range end date must be on or after the start date.");
      return;
    }
    Calendar target = resolveTargetCalendar(matcher.group("targetCalendar"));
    if (target == null) {
      return;
    }
    try {
      int copied = CalendarCopier.copyEventsInRange(
          source, target, rangeStart, rangeEnd, targetStart);
      Messages.info("Copied " + copied + " event(s) to " + target.getName() + ".");
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  // Removed legacy subject accessor in favor of getActiveCalendar/getActiveCalendarName.
}
