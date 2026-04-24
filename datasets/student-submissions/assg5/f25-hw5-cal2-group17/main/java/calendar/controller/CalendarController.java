package calendar.controller;

import static calendar.util.CommandParser.parse;

import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.EventCopier;
import calendar.model.EventEditor;
import calendar.model.EventImpl;
import calendar.util.CommandParser;
import calendar.util.Exporter;
import calendar.util.ExporterRegistry;
import calendar.view.CalendarView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * Controller that coordinates between the model and view.
 * Executes parsed commands on the calendar model and displays results via the view.
 */
public class CalendarController {
  private final CalendarManager calendarManager;
  private Calendar currentCalendar;
  private EventEditor eventEditor;
  private final EventCopier eventCopier;
  private final CalendarView view;

  /**
   * Constructor for Calendar Controller with view only.
   * For multi-calendar support.
   *
   * @param view View object
   */
  public CalendarController(CalendarView view) {
    this.calendarManager = CalendarManager.getInstance();
    this.currentCalendar = null;
    this.eventEditor = null;
    this.eventCopier = new EventCopier();
    this.view = view;
  }

  /**
   * Backward compatibility constructor.
   * Uses the provided calendar directly without CalendarManager.
   *
   * @param calendar Calendar object
   * @param view     View object
   */
  public CalendarController(Calendar calendar, CalendarView view) {
    this.calendarManager = CalendarManager.getInstance();
    this.eventCopier = new EventCopier();
    this.view = view;
    this.currentCalendar = calendar;
    this.eventEditor = new EventEditor(currentCalendar);
  }

  /**
   * Executes a parsed command.
   *
   * @param command Command as String
   * @return true for all cases since it handles the errors gracefully
   */
  public boolean executeCommand(String command) {
    try {
      CommandParser.Command cmd = parse(command);
      return executeCommand(cmd);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
      return true;
    }
  }

  /**
   * Executes a parsed command.
   *
   * @param cmd Command Parser Command
   * @return true if should continue, false if exit command
   */
  public boolean executeCommand(CommandParser.Command cmd) {
    try {
      if (cmd.type == CommandParser.Command.Type.CREATE_CALENDAR) {
        handleCreateCalendar(cmd);
      } else if (cmd.type == CommandParser.Command.Type.EDIT_CALENDAR) {
        handleEditCalendar(cmd);
      } else if (cmd.type == CommandParser.Command.Type.USE_CALENDAR) {
        handleUseCalendar(cmd);
      } else if (cmd.type == CommandParser.Command.Type.COPY_EVENT) {
        handleCopyEvent(cmd);
      } else if (cmd.type == CommandParser.Command.Type.COPY_EVENTS_ON_DATE) {
        handleCopyEventsOnDate(cmd);
      } else if (cmd.type == CommandParser.Command.Type.COPY_EVENTS_IN_RANGE) {
        handleCopyEventsInRange(cmd);
      } else if (cmd.type == CommandParser.Command.Type.CREATE_EVENT) {
        handleCreateEvent(cmd);
      } else if (cmd.type == CommandParser.Command.Type.CREATE_EVENT_SERIES) {
        handleCreateEventSeries(cmd);
      } else if (cmd.type == CommandParser.Command.Type.EDIT_SINGLE) {
        handleEditSingle(cmd);
      } else if (cmd.type == CommandParser.Command.Type.EDIT_FROM) {
        handleEditFrom(cmd);
      } else if (cmd.type == CommandParser.Command.Type.EDIT_SERIES) {
        handleEditSeries(cmd);
      } else if (cmd.type == CommandParser.Command.Type.PRINT_DATE) {
        handlePrintDate(cmd);
      } else if (cmd.type == CommandParser.Command.Type.PRINT_RANGE) {
        handlePrintRange(cmd);
      } else if (cmd.type == CommandParser.Command.Type.SHOW_STATUS) {
        handleShowStatus(cmd);
      } else if (cmd.type == CommandParser.Command.Type.EXPORT) {
        handleExport(cmd);
      } else {
        return false;
      }
      return true;

    } catch (Exception e) {
      view.displayError(e.getMessage());
      return true;  // Continue despite error
    }
  }

  // ========== CALENDAR MANAGEMENT HANDLERS ==========

  private void handleCreateCalendar(CommandParser.Command command) {
    String name = (String) command.params.get("name");
    String timezoneStr = (String) command.params.get("timezone");

    // Validate input parameters
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }

    if (timezoneStr == null || timezoneStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Timezone cannot be null or empty");
    }

    // Validate and parse timezone
    ZoneId timezone;
    try {
      timezone = ZoneId.of(timezoneStr);
    } catch (java.time.zone.ZoneRulesException e) {
      throw new IllegalArgumentException(
          "Invalid timezone: '" + timezoneStr
              + "'. Use IANA timezone format (e.g., America/New_York, Europe/Paris, Asia/Tokyo)");
    }

    // Check for duplicate calendar name (optional - CalendarManager also checks this)
    if (calendarManager.calendarExists(name)) {
      throw new IllegalArgumentException(
          "Calendar with name '" + name + "' already exists. Please choose a different name.");
    }

    // Create calendar
    calendarManager.createCalendar(name, timezone);

    // Display success message
    view.displaySuccess("Calendar '" + name + "' created with timezone " + timezoneStr);
  }

  private void handleEditCalendar(CommandParser.Command command) {
    String calendarName = (String) command.params.get("name");
    String property = (String) command.params.get("property");
    String newValue = (String) command.params.get("value");

    Calendar calendar = calendarManager.getCalendar(calendarName);

    if (property.equalsIgnoreCase("name")) {
      calendarManager.renameCalendar(calendarName, newValue);
      if (currentCalendar == calendar) {
        currentCalendar = calendarManager.getCalendar(newValue);
        eventEditor = new EventEditor(currentCalendar);
      }
      view.displaySuccess("Calendar renamed to '" + newValue + "'");
    } else if (property.equalsIgnoreCase("timezone")) {
      try {
        ZoneId newTimezone = ZoneId.of(newValue);
        calendar.setTimezone(newTimezone);
        view.displaySuccess("Calendar timezone changed to " + newValue);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid timezone: " + newValue);
      }
    } else {
      throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  private void handleUseCalendar(CommandParser.Command command) {
    String calendarName = (String) command.params.get("name");

    currentCalendar = calendarManager.getCalendar(calendarName);
    eventEditor = new EventEditor(currentCalendar);
    view.displaySuccess("Now using calendar: " + calendarName);
  }

  // ========== EVENT COPYING HANDLERS ==========

  private void handleCopyEvent(CommandParser.Command command) {
    ensureCalendarInUse();

    String eventSubject = (String) command.params.get("subject");
    LocalDateTime eventStart = (LocalDateTime) command.params.get("start");
    String targetCalendarName = (String) command.params.get("targetCalendar");
    LocalDateTime targetStart = (LocalDateTime) command.params.get("targetStart");

    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);

    eventCopier.copyEvent(currentCalendar, targetCalendar,
        eventSubject, eventStart, targetStart);
    view.displaySuccess("Event copied to calendar '" + targetCalendarName + "'");
  }

  private void handleCopyEventsOnDate(CommandParser.Command command) {
    ensureCalendarInUse();

    LocalDate sourceDate = (LocalDate) command.params.get("sourceDate");
    String targetCalendarName = (String) command.params.get("targetCalendar");
    LocalDate targetDate = (LocalDate) command.params.get("targetDate");

    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);

    eventCopier.copyEventsOnDate(currentCalendar, targetCalendar,
        sourceDate, targetDate);
    view.displaySuccess("Events from " + sourceDate + " copied to calendar '"
        + targetCalendarName + "'");
  }

  private void handleCopyEventsInRange(CommandParser.Command command) {
    ensureCalendarInUse();

    LocalDate startDate = (LocalDate) command.params.get("startDate");
    LocalDate endDate = (LocalDate) command.params.get("endDate");
    String targetCalendarName = (String) command.params.get("targetCalendar");
    LocalDate targetStartDate = (LocalDate) command.params.get("targetStartDate");

    Calendar targetCalendar = calendarManager.getCalendar(targetCalendarName);

    eventCopier.copyEventsInRange(currentCalendar, targetCalendar,
        startDate, endDate, targetStartDate);
    view.displaySuccess("Events from " + startDate + " to " + endDate
        + " copied to calendar '" + targetCalendarName + "'");
  }

  // ========== EXISTING EVENT HANDLERS (Updated) ==========

  private void handleCreateEvent(CommandParser.Command command) {
    ensureCalendarInUse();
    String subject = (String) command.params.get("subject");
    boolean allDay = (Boolean) command.params.get("allDay");

    Event event;
    if (allDay) {
      LocalDate date = (LocalDate) command.params.get("date");
      event = new EventImpl(subject, date);
    } else {
      LocalDateTime start = (LocalDateTime) command.params.get("startDateTime");
      LocalDateTime end = (LocalDateTime) command.params.get("endDateTime");
      event = new EventImpl(subject, start, end);
    }
    currentCalendar.addEvent(event);
    view.displaySuccess("Event created: " + subject);
  }

  private void handleCreateEventSeries(CommandParser.Command command) {
    ensureCalendarInUse();

    String subject = (String) command.params.get("subject");
    LocalDateTime startDateTime = (LocalDateTime) command.params.get("startDateTime");
    LocalDateTime endDateTime = (LocalDateTime) command.params.get("endDateTime");
    Set<DayOfWeek> weekdays = (Set<DayOfWeek>) (command.params.get("weekdays"));
    Integer occurrences = (Integer) command.params.get("occurrences");
    LocalDate untilDate = (LocalDate) command.params.get("untilDate");

    if (occurrences != null) {
      currentCalendar.addEventSeries(subject, startDateTime, endDateTime,
          weekdays, occurrences, null);
      view.displaySuccess("Event series created: " + subject
          + " (" + occurrences + " occurrences)");
    } else {
      currentCalendar.addEventSeries(subject, startDateTime, endDateTime,
          weekdays, -1, untilDate);
      view.displaySuccess("Event series created: " + subject
          + " (until " + untilDate + ")");
    }
  }

  private void handleEditSingle(CommandParser.Command command) {
    ensureCalendarInUse();

    String property = (String) command.params.get("property");
    String subject = (String) command.params.get("subject");
    LocalDateTime startDateTime = (LocalDateTime) command.params.get("startDateTime");
    String newValue = (String) command.params.get("newValue");

    eventEditor.editSingleEvent(subject, startDateTime, property, newValue);
    view.displaySuccess("Event edited");
  }

  private void handleEditFrom(CommandParser.Command command) {
    ensureCalendarInUse();

    String property = (String) command.params.get("property");
    String subject = (String) command.params.get("subject");
    LocalDateTime startDateTime = (LocalDateTime) command.params.get("startDateTime");
    String newValue = (String) command.params.get("newValue");

    eventEditor.editEventsFrom(subject, startDateTime, property, newValue);
    view.displaySuccess("Events edited");
  }

  private void handleEditSeries(CommandParser.Command command) {
    ensureCalendarInUse();

    String property = (String) command.params.get("property");
    String subject = (String) command.params.get("subject");
    LocalDateTime startDateTime = (LocalDateTime) command.params.get("startDateTime");
    String newValue = (String) command.params.get("newValue");

    eventEditor.editEntireSeries(subject, startDateTime, property, newValue);
    view.displaySuccess("Series edited");
  }

  private void handlePrintDate(CommandParser.Command command) {
    ensureCalendarInUse();

    LocalDate date = (LocalDate) command.params.get("date");
    List<Event> events = currentCalendar.getEventsOnDate(date);
    view.displayEventsOnDate(date, events);
  }

  private void handlePrintRange(CommandParser.Command command) {
    ensureCalendarInUse();

    LocalDateTime start = (LocalDateTime) command.params.get("startDateTime");
    LocalDateTime end = (LocalDateTime) command.params.get("endDateTime");
    List<Event> events = currentCalendar.getEventsInRange(start, end);
    view.displayEventsInRange(start, end, events);
  }

  private void handleShowStatus(CommandParser.Command command) {
    ensureCalendarInUse();

    LocalDateTime dateTime = (LocalDateTime) command.params.get("dateTime");
    boolean busy = currentCalendar.isBusyAt(dateTime);
    view.displayStatus(dateTime, busy);
  }

  private void handleExport(CommandParser.Command command) {
    ensureCalendarInUse();

    String filename = (String) command.params.get("filename");
    try {
      // Use factory to get appropriate exporter
      Exporter exporter = ExporterRegistry.getExporter(filename);

      // Use visitor pattern (double dispatch)
      String absolutePath = currentCalendar.accept(exporter, filename);

      view.displaySuccess("Calendar exported to " + exporter.getFormatName()
          + " format: " + absolutePath);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException("Failed to export calendar: " + e.getMessage());
    }
  }

  // ========== HELPER METHODS ==========

  /**
   * Ensures a calendar is currently in use before executing event commands.
   *
   * @throws IllegalArgumentException if no calendar is active
   */
  private void ensureCalendarInUse() {
    if (currentCalendar == null) {
      throw new IllegalArgumentException(
          "No calendar in use. Use 'use calendar --name <name>' command first.");
    }
  }
}