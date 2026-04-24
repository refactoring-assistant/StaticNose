package calendar.model;

import java.time.ZoneId;
import java.util.Map;

/**
 * Interface for managing multiple calendars and delegating operations to the active calendar.
 * Provides functionality for creating, editing, switching between calendars, and copying
 * events across calendars.
 */
public interface CalendarManagerInterface {

  /**
   * Gets the currently active calendar.
   *
   * @return the active calendar model
   * @throws IllegalArgumentException if no calendar is currently active
   */
  CalendarModelInterface getActiveCalendar();

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the name of the active calendar
   * @throws IllegalArgumentException if no calendar is currently active
   */
  String getCalendarName();

  /**
   * Gets the timezone of the currently active calendar.
   *
   * @return the timezone of the active calendar
   * @throws IllegalArgumentException if no calendar is currently active
   */
  ZoneId getTimeZone();

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param parameters map containing "calname" (String) and "timezone" (ZoneId)
   * @throws IllegalArgumentException if the calendar name is null, empty, or already exists,
   *                                  or if required parameters are missing
   */
  CalendarModelInterface createCalendar(Map<String, Object> parameters);

  /**
   * Edits properties of an existing calendar (name or timezone).
   *
   * @param parameters map containing "calname" (String), "property" (String),
   *                   and "value" (String for name, ZoneId for timezone)
   * @return true if the calendar was edited successfully
   * @throws IllegalArgumentException if the calendar doesn't exist, property is invalid,
   *                                  or the new name already exists
   */
  CalendarModelInterface editCalendar(Map<String, Object> parameters);

  /**
   * Sets the specified calendar as the active calendar.
   *
   * @param parameters map containing "calname" (String)
   * @return true if the calendar was set as active successfully
   * @throws IllegalArgumentException if the calendar doesn't exist
   */
  boolean useCalendar(Map<String, Object> parameters);

  /**
   * Copies a single event from the active calendar to a target calendar.
   *
   * @param parameters map containing "target" (String), "subject" (String),
   *                   "start" (ZonedDateTime), and "targetstart" (ZonedDateTime)
   * @throws IllegalArgumentException if no active calendar, target calendar doesn't exist,
   *                                  or event is not found
   */
  void copyEvent(Map<String, Object> parameters);

  /**
   * Copies all events from a specific date in the active calendar to a target calendar.
   *
   * @param parameters map containing "target" (String), "sourcedate" (LocalDate),
   *                   and "targetdate" (LocalDate)
   * @throws IllegalArgumentException if no active calendar, target calendar doesn't exist,
   *                                  or copying creates multi-day series events
   */
  void copyEventsOn(Map<String, Object> parameters);

  /**
   * Copies all events within a date range from the active calendar to a target calendar.
   *
   * @param parameters map containing "target" (String), "startdate" (LocalDate),
   *                   "enddate" (LocalDate), and "targetdate" (LocalDate)
   * @throws IllegalArgumentException if no active calendar, target calendar doesn't exist,
   *                                  or copying creates multi-day series events
   */
  void copyEventsBetween(Map<String, Object> parameters);
}