package calendar.model;

import java.time.ZoneId;
import java.util.List;

/**
 * Manages multiple calendars in the system.
 * Each calendar has a unique name and timezone.
 */
public interface CalendarSystem {

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name the unique calendar name
   * @param timezone the IANA timezone (e.g., "America/New_York")
   * @throws IllegalArgumentException if name already exists or timezone invalid
   */
  void createCalendar(String name, String timezone);

  /**
   * Gets a calendar by name.
   *
   * @param name the calendar name
   * @return the calendar
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  CalendarModel getCalendar(String name);

  /**
   * Edits a calendar property (name or timezone).
   *
   * @param calendarName the calendar to edit
   * @param property "name" or "timezone"
   * @param newValue the new value
   * @throws IllegalArgumentException if invalid
   */
  void editCalendar(String calendarName, String property, String newValue);

  /**
   * Gets all calendar names.
   *
   * @return list of calendar names
   */
  List<String> getAllCalendarNames();

  /**
   * Checks if a calendar exists.
   *
   * @param name the calendar name
   * @return true if exists
   */
  boolean calendarExists(String name);

  /**
   * Gets the timezone of a calendar.
   *
   * @param calendarName the calendar name
   * @return the ZoneId
   */
  ZoneId getCalendarTimezone(String calendarName);
}