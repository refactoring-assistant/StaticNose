package calendar.controller.gui;

import calendar.controller.exporter.AbstractExportCalendar;
import calendar.controller.text.commandfactory.ExportCalendar;
import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.model.event.CalendarEvent;
import calendar.view.CalendarViewInterface;
import calendar.view.gui.CalendarGuiView;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller implementation for GUI mode that implements the Features interface.
 * Manages multiple calendars and delegates operations to the appropriate calendar model.
 */
public class CalendarGuiController implements Features {

  private final CalendarManagerInterface calendar;
  private final Map<String, CalendarModelInterface> calendars;
  private final Map<String, ZoneId> calendarTimezones;
  private String currentCalendarName;
  private final CalendarGuiView view;
  private final Map<String, Object> parameters;

  /**
   * Creates a new GUI controller with a default calendar.
   *
   * @param view the GUI view to control
   */
  public CalendarGuiController(CalendarViewInterface view, CalendarManagerInterface calendar) {
    if (view == null || calendar == null) {
      throw new IllegalArgumentException(
          "View and Manager cannot be null when creating a calendar GUI controller.");
    }
    this.view = (CalendarGuiView) view;
    this.calendar = calendar;
    this.calendars = new HashMap<>();
    this.parameters = new HashMap<>();
    this.calendarTimezones = new HashMap<>();

    createDefaultCalendar();
    this.view.setFeatures(this);
  }

  /**
   * Creates a default calendar on instantiation.
   */
  private void createDefaultCalendar() {
    String defaultName = "Default Calendar";
    ZoneId defaultZone = ZoneId.systemDefault();

    parameters.put("calname", defaultName);
    parameters.put("timezone", defaultZone);
    calendarTimezones.put(defaultName, defaultZone);
    currentCalendarName = defaultName;
    CalendarModelInterface model = createModelInstance(parameters);
    calendars.put(defaultName, model);
    calendar.useCalendar(parameters);
  }

  /**
   * Creates a new calendar model instance.
   */
  private CalendarModelInterface createModelInstance(Map<String, Object> parameters) {
    try {
      return calendar.createCalendar(parameters);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create calendar: " + e.getMessage());
    }
  }

  @Override
  public void createCalendar(String calendarName, String timezone) {
    try {
      ZoneId zoneId = ZoneId.of(timezone);
      parameters.put("calname", calendarName);
      parameters.put("timezone", zoneId);
      CalendarModelInterface model = createModelInstance(parameters);

      calendars.put(calendarName, model);
      calendarTimezones.put(calendarName, zoneId);

      view.updateCalendarList();
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  @Override
  public void editCalendar(String calendarName, String newName, String newTimezone) {

    if (!calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException(
          "Calendar '" + calendarName + "' does not exist");
    }

    String finalName = calendarName;
    if (newName != null && !newName.trim().isEmpty()) {
      editCalendarName(calendarName, newName);
      finalName = newName;
    }

    if (newTimezone != null && !newTimezone.trim().isEmpty()) {
      editCalendarTimezone(newTimezone, finalName);
    }

    view.updateCalendarList();
  }

  /**
   * Edit the calendar name from the GUI list and from the calendar manager.
   *
   * @param calendarName the calendar whose name is to be changed
   * @param newName      the new name of the calendar
   */
  private void editCalendarName(String calendarName, String newName) {
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException(
          "Calendar with name '" + newName + "' already exists");
    }

    calendars.remove(calendarName);

    parameters.put("calname", calendarName);
    parameters.put("property", "name");
    parameters.put("value", newName);
    CalendarModelInterface model = calendar.editCalendar(parameters);

    calendars.put(newName, model);
    ZoneId timezone = calendarTimezones.remove(calendarName);
    calendarTimezones.put(newName, timezone);

    if (currentCalendarName.equals(calendarName)) {
      currentCalendarName = newName;
      parameters.put("calname", newName);
      calendar.useCalendar(parameters);
    }
  }

  /**
   * Edit the calendar timezone to the new timezone.
   *
   * @param newTimezone the new timezone to be set to
   * @param finalName   the name of the calendar where the timezone has to be changed
   */
  private void editCalendarTimezone(String newTimezone, String finalName) {
    try {
      ZoneId zoneId = ZoneId.of(newTimezone);

      parameters.put("calname", finalName);
      parameters.put("property", "timezone");
      parameters.put("value", zoneId);
      CalendarModelInterface model = calendar.editCalendar(parameters);

      calendars.remove(finalName);
      calendarTimezones.remove(finalName);

      calendars.put(finalName, model);
      calendarTimezones.put(finalName, zoneId);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + newTimezone);
    }
  }

  @Override
  public void switchCalendar(String calendarName) {
    if (!calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException(
          "Calendar '" + calendarName + "' does not exist");
    }

    currentCalendarName = calendarName;
    parameters.put("calname", currentCalendarName);
    calendar.useCalendar(parameters);
    view.refreshCalendarView();
  }

  @Override
  public List<String> getCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  @Override
  public void createSingleEvent(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();

    try {
      currentModel.createSingleEvent(parameters);
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create event: " + e.getMessage());
    }
  }

  @Override
  public void createEventSeries(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();

    try {
      currentModel.createEventSeries(parameters);
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create event series: " + e.getMessage());
    }
  }

  @Override
  public void createAllDayEvent(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();

    try {
      currentModel.createAllDayEvent(parameters);
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create all-day event: " + e.getMessage());
    }
  }

  @Override
  public void createAllDayEventSeries(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();

    try {
      currentModel.createAllDayEventSeries(parameters);
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Failed to create all-day event series: " + e.getMessage());
    }
  }

  @Override
  public void editSingleEvent(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();

    try {
      currentModel.editSingleEvent(parameters);
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit event: " + e.getMessage());
    }
  }

  @Override
  public void editSeries(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();

    try {
      currentModel.editSeries(parameters);
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit series: " + e.getMessage());
    }
  }

  @Override
  public void editEvents(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();

    try {
      currentModel.editEvents(parameters);
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit events: " + e.getMessage());
    }
  }

  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    CalendarModelInterface currentModel = getCurrentModel();
    return currentModel.getEventsOn(date);
  }

  @Override
  public List<CalendarEvent> getEventsInRange(LocalDate start, LocalDate end) {
    CalendarModelInterface currentModel = getCurrentModel();
    ZoneId timezone = ZoneId.of(getCurrentCalendarTimezone());

    ZonedDateTime startDateTime = start.atStartOfDay(timezone);
    ZonedDateTime endDateTime = end.plusDays(1).atStartOfDay(timezone);

    return currentModel.getEventsInRange(startDateTime, endDateTime);
  }

  @Override
  public List<CalendarEvent> getAllEvents() {
    CalendarModelInterface currentModel = getCurrentModel();
    return currentModel.getAllEvents();
  }

  @Override
  public void copyEvent(Map<String, Object> parameters) {
    try {
      calendar.copyEvent(parameters);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to copy event: " + e.getMessage());
    }
  }

  @Override
  public void copyEventsOnDate(Map<String, Object> parameters) {
    try {
      calendar.copyEventsOn(parameters);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to copy events on date: " + e.getMessage());
    }
  }

  @Override
  public void copyEventsInRange(Map<String, Object> parameters) {
    try {
      calendar.copyEventsBetween(parameters);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to copy events between dates: " + e.getMessage());
    }
  }

  @Override
  public void exportCalendar(String filename) {
    try {
      CalendarModelInterface currentModel = getCurrentModel();
      List<CalendarEvent> eventList = currentModel.getAllEvents();
      parameters.put("filename", filename);
      if (eventList.isEmpty()) {
        throw new IllegalArgumentException(
            "No events exist in calendar " + calendar.getCalendarName() + ".");
      }
      AbstractExportCalendar exporter =
          new ExportCalendar(this.parameters, this.calendar, this.view).getExporter();
      exporter.exportHelper(eventList);
    } catch (Exception e) {
      throw new IllegalArgumentException("Export calendar failed: " + e.getMessage());
    }
  }

  @Override
  public String getCurrentCalendarTimezone() {
    return calendarTimezones.get(currentCalendarName).toString();
  }

  /**
   * Gets the currently active calendar model.
   */
  private CalendarModelInterface getCurrentModel() {
    return calendars.get(currentCalendarName);
  }

  @Override
  public void interactiveMode() {
    throw new UnsupportedOperationException("Interactive mode not supported in GUI mode.");
  }

  @Override
  public void headlessMode(String fileName) {
    throw new UnsupportedOperationException("Headless mode not supported in GUI mode.");
  }

  @Override
  public void guiMode() {
    view.displayGui();
    view.refreshCalendarView();
  }

  @Override
  public void processCommand(String command) {
    throw new UnsupportedOperationException("Processing commands only allowed in text based mode");
  }

  @Override
  public void showUsageAndExit() {
    throw new UnsupportedOperationException("Usage: java -jar calendar.jar");
  }

  @Override
  public void editEventsWithName(Map<String, Object> parameters) {
    CalendarModelInterface currentModel = getCurrentModel();
    String name = (String) parameters.get("subject");
    try {
      List<CalendarEvent> allEvents = currentModel.getAllEvents();
      for (CalendarEvent event : allEvents) {
        if (event.getSubject().equals(name)) {
          Map<String, Object> eventParameters = event.getHashMap();
          eventParameters.put("property", parameters.get("property"));
          eventParameters.put("value", parameters.get("value"));
          currentModel.editSingleEvent(eventParameters);
        }
      }
      view.refreshCalendarView();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to edit events with name: " + e.getMessage());
    }
  }
}