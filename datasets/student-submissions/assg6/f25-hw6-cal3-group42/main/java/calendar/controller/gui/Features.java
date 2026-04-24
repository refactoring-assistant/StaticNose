package calendar.controller.gui;

import calendar.controller.CalendarControllerInterface;
import calendar.model.event.CalendarEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Features interface that defines all callbacks the GUI view can invoke.
 * The controller implements this interface to handle GUI events.
 */
public interface Features extends CalendarControllerInterface {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param calendarName the name for the new calendar
   * @param timezone     the timezone ID
   */
  void createCalendar(String calendarName, String timezone);

  /**
   * Edits an existing calendar with the specified name or timezone.
   *
   * @param calendarName the name for the new calendar
   */
  void editCalendar(String calendarName, String newName, String newTimezone);

  /**
   * Switches to a different calendar.
   *
   * @param calendarName the name of the calendar to switch to
   */
  void switchCalendar(String calendarName);

  /**
   * Gets the list of all available calendar names.
   *
   * @return list of calendar names
   */
  List<String> getCalendarNames();

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the current calendar name
   */
  String getCurrentCalendarName();

  /**
   * Creates a single event.
   *
   * @param parameters map containing event details
   */
  void createSingleEvent(Map<String, Object> parameters);

  /**
   * Creates a recurring event series.
   *
   * @param parameters map containing series details
   */
  void createEventSeries(Map<String, Object> parameters);

  /**
   * Creates an all-day event.
   *
   * @param parameters map containing all-day event details
   */
  void createAllDayEvent(Map<String, Object> parameters);

  /**
   * Creates an all-day event series.
   *
   * @param parameters map containing all-day series details
   */
  void createAllDayEventSeries(Map<String, Object> parameters);

  /**
   * Edits a single event instance.
   *
   * @param parameters map containing event identification and new values
   */
  void editSingleEvent(Map<String, Object> parameters);

  /**
   * Edits all events in a series.
   *
   * @param parameters map containing series identification and new values
   */
  void editSeries(Map<String, Object> parameters);

  /**
   * Edits events from a specific date onwards.
   *
   * @param parameters map containing event identification and new values
   */
  void editEvents(Map<String, Object> parameters);

  /**
   * Gets all events for a specific date in the current calendar.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<CalendarEvent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events in a date range for the current calendar.
   *
   * @param start the start date
   * @param end   the end date
   * @return list of events in the range
   */
  List<CalendarEvent> getEventsInRange(LocalDate start, LocalDate end);

  /**
   * Gets all events for the current calendar.
   *
   * @return list of all events
   */
  List<CalendarEvent> getAllEvents();

  /**
   * Copies an event to another calendar.
   *
   * @param parameters map containing event data to copy from
   */
  void copyEvent(Map<String, Object> parameters);

  /**
   * Copies all events on a specific date to another calendar.
   *
   * @param parameters map containing events details to copy from
   */
  void copyEventsOnDate(Map<String, Object> parameters);

  /**
   * Copies all events in a date range to another calendar.
   *
   * @param parameters map containing events details to copy from
   */
  void copyEventsInRange(Map<String, Object> parameters);

  /**
   * Exports the current calendar to a file.
   *
   * @param filename the output filename (should end in .csv or .ics)
   */
  void exportCalendar(String filename);

  /**
   * Gets the timezone of the current calendar.
   *
   * @return the timezone ID
   */
  String getCurrentCalendarTimezone();

  /**
   * Edit events with given name.
   *
   * @param parameters parameter map
   */
  void editEventsWithName(Map<String, Object> parameters);
}