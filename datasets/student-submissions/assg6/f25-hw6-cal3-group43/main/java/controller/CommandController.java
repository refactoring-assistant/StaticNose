package controller;

import interpreter.CommandInterpreter.CommandMatch;
import interpreter.CommandInterpreter.CommandType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import messaging.Messages;
import model.Calendar;
import model.Event;

/**
 * CommandController is responsible for:
 * - Command parsing
 * - Validation and business rules (deciding WHAT should change)
 * It delegates all data mutations to Calendar (doing the actual changes).
 */
public class CommandController extends QueryController {

  // Help text moved out of methods to keep code readable. This stays in controller (presentation).
  public static final String CREATE_INSTRUCTION = String.join(System.lineSeparator(),
      "----------------- CREATE INSTRUCTION --------------",
      "You can create one day's event with:",
      " create event <eventSubject> on <date>",
      "where <date> must be: " + DATE_FMT + ".",
      "",
      "Or create an event within a time range with:",
      " create event <eventSubject> from <dateTime> to <dateTime>",
      "where <dateTime> must be: " + DATETIME_FMT + ".",
      "",
      "You can create event series that repeats N times with:",
      " create event <eventSubject> on <date> repeats <weekdays> for <N> times",
      "where <date> must be: " + DATE_FMT + ".",
      "",
      "Or create event series that repeats N times, each within a time range:",
      " create event <eventSubject> from <dateTime> to <dateTime> repeats <weekdays> for <N> times",
      "where <dateTime> must be: " + DATETIME_FMT + ".",
      "",
      "You can create event series that repeats until a specific date with:",
      " create event <eventSubject> on <date> repeats <weekdays> until <date>",
      "where <date> must be: " + DATE_FMT + "."
  );

  public static final String EDIT_INSTRUCTION = String.join(System.lineSeparator(),
      "----------------- EDIT INSTRUCTION --------------",
      "Check status at a specific time:",
      "  show status on <dateTime>",
      "where <dateTime> must be: " + DATETIME_FMT + ".",
      "",
      "Edit a single event by exact (subject, start, end):",
      "  edit event <property> <subject> from <start> to <end> with <newValue>",
      "",
      "Edit multiple events (subject + anchor start; series tail from anchor):",
      "  edit events <property> <subject> from <start> with <newValue>",
      "",
      "Edit a series identified by anchor (subject, start):",
      "  edit series <property> <subject> from <start> with <newValue>",
      "",
      "Notes:",
      " - When editing start: the controller will split the series at the anchor time,",
      "   then only update the tail (events starting at or after the anchor).",
      " - For other properties on 'edit series', the whole series will be updated."
  );

  /**
   * Constructs a CommandController with a fresh calendar.
   */
  public CommandController() {
    this(new Calendar());
  }

  /**
   * Constructs a CommandController bound to an existing calendar instance.
   *
   * @param calendar calendar backing this controller session
   */
  public CommandController(Calendar calendar) {
    super(calendar);
  }

  /**
   * Print edit instructions.
   */
  public static void editInstruction() {
    Messages.info(EDIT_INSTRUCTION);
  }

  /**
   * Dispatches regex-based command matches to the appropriate create/edit handler.
   *
   * @param match parsed command
   * @return true if handled in this controller
   */
  @Override
  protected boolean handleSubclass(CommandMatch match) {
    CommandType type = match.type();
    switch (type) {
      case CREATE_TIMED_EVENT:
        handleCreateTimedEvent(match);
        return true;
      case CREATE_TIMED_SERIES_FOR:
        handleCreateTimedSeriesFor(match);
        return true;
      case CREATE_TIMED_SERIES_UNTIL:
        handleCreateTimedSeriesUntil(match);
        return true;
      case CREATE_ALLDAY_EVENT:
        handleCreateAllDayEvent(match);
        return true;
      case CREATE_ALLDAY_SERIES_FOR:
        handleCreateAllDaySeriesFor(match);
        return true;
      case CREATE_ALLDAY_SERIES_UNTIL:
        handleCreateAllDaySeriesUntil(match);
        return true;
      case EDIT_EVENT_FROM_TO:
        handleEditEvent(match);
        return true;
      case EDIT_EVENTS_FROM:
        handleEditEvents(match);
        return true;
      case EDIT_SERIES:
        handleEditSeries(match);
        return true;
      default:
        return false;
    }
  }

  /**
   * Handles {@code create event <subject> from ... to ...}.
   *
   * @param match parsed command
   */
  private void handleCreateTimedEvent(CommandMatch match) {
    Matcher matcher = match.matcher();
    String subject = extractSubject(matcher);
    try {
      LocalDateTime start = LocalDateTime.parse(matcher.group("start"));
      LocalDateTime end = LocalDateTime.parse(matcher.group("end"));
      this.calendar.addEvent(subject, start, end);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles timed recurring events that repeat for N occurrences.
   *
   * @param match parsed command
   */
  private void handleCreateTimedSeriesFor(CommandMatch match) {
    Matcher matcher = match.matcher();
    String subject = extractSubject(matcher);
    String weekdays = matcher.group("weekdays");
    int repeat = parseIntSafe(matcher.group("count"));
    try {
      LocalDateTime start = LocalDateTime.parse(matcher.group("start"));
      LocalDateTime end = LocalDateTime.parse(matcher.group("end"));
      this.calendar.addEventSeries(subject, start, end, weekdays, repeat);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles timed recurring events that run until a specific date.
   *
   * @param match parsed command
   */
  private void handleCreateTimedSeriesUntil(CommandMatch match) {
    Matcher matcher = match.matcher();
    String subject = extractSubject(matcher);
    String weekdays = matcher.group("weekdays");
    try {
      LocalDateTime start = LocalDateTime.parse(matcher.group("start"));
      LocalDateTime end = LocalDateTime.parse(matcher.group("end"));
      LocalDate until = LocalDate.parse(matcher.group("until"));
      this.calendar.addEventsUntil(subject, start, end, weekdays, until);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
      Messages.error(DATE_ERROR);
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles {@code create event <subject> on <date>} (all-day events).
   *
   * @param match parsed command
   */
  private void handleCreateAllDayEvent(CommandMatch match) {
    Matcher matcher = match.matcher();
    String subject = extractSubject(matcher);
    try {
      LocalDate date = LocalDate.parse(matcher.group("date"));
      calendar.addEvent(new Event(date, subject));
    } catch (DateTimeParseException e) {
      Messages.error(DATE_ERROR);
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles all-day recurring events repeating for N occurrences.
   *
   * @param match parsed command
   */
  private void handleCreateAllDaySeriesFor(CommandMatch match) {
    Matcher matcher = match.matcher();
    String subject = extractSubject(matcher);
    String weekdays = matcher.group("weekdays");
    int repeat = parseIntSafe(matcher.group("count"));
    try {
      LocalDate date = LocalDate.parse(matcher.group("date"));
      this.calendar.addEventSeries(subject, date, weekdays, repeat);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles all-day recurring events running until a given date.
   *
   * @param match parsed command
   */
  private void handleCreateAllDaySeriesUntil(CommandMatch match) {
    Matcher matcher = match.matcher();
    String subject = extractSubject(matcher);
    String weekdays = matcher.group("weekdays");
    try {
      LocalDate date = LocalDate.parse(matcher.group("date"));
      LocalDate until = LocalDate.parse(matcher.group("until"));
      this.calendar.addEventsUntil(subject, date, weekdays, until);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
    } catch (IllegalArgumentException e) {
      Messages.error(e.getMessage());
    }
  }

  /**
   * Handles {@code edit event ...} (single-instance edits).
   *
   * @param match parsed command
   */
  private void handleEditEvent(CommandMatch match) {
    Matcher matcher = match.matcher();
    calendar.handleEditEventFromTo(
        matcher.group("property"),
        extractSubject(matcher),
        matcher.group("start"),
        matcher.group("end"),
        matcher.group("value").trim());
  }

  /**
   * Handles {@code edit events ...} (tail edits from an anchor).
   *
   * @param match parsed command
   */
  private void handleEditEvents(CommandMatch match) {
    Matcher matcher = match.matcher();
    calendar.handleEditEventsFromWith(
        matcher.group("property"),
        extractSubject(matcher),
        matcher.group("start"),
        matcher.group("value").trim());
  }

  /**
   * Handles {@code edit series ...} (entire series edits).
   *
   * @param match parsed command
   */
  private void handleEditSeries(CommandMatch match) {
    Matcher matcher = match.matcher();
    calendar.handleEditSeries(
        matcher.group("property"),
        extractSubject(matcher),
        matcher.group("start"),
        matcher.group("value").trim());
  }

  /**
   * Parses integers for repeat counts with a clearer error message.
   *
   * @param text numeric text
   * @return parsed integer
   */
  private int parseIntSafe(String text) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Repeat count must be numeric: " + text, e);
    }
  }

  /* ============================================================
   * HELP / UTIL
   * ============================================================ */

  /**
   * Extracts either the quoted or plain subject group from a matcher.
   *
   * @param matcher regex matcher
   * @return normalized subject without surrounding quotes
   */
  private String extractSubject(Matcher matcher) {
    String quoted = matcher.group("subjectQuoted");
    if (quoted != null) {
      return quoted.substring(1, quoted.length() - 1);
    }
    return matcher.group("subjectPlain");
  }

  @Override
  protected void help() {
    super.help();
    Messages.info("");
    Messages.info(CREATE_INSTRUCTION);
    Messages.info("");
    Messages.info(EDIT_INSTRUCTION);
  }
}
