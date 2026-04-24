package calendar.model;

import java.time.ZoneId;
import java.util.List;

/**
 * Interface for managing multiple calendars in the application.
 * Provides operations for creating, retrieving, and managing calendars,
 * as well as maintaining an active calendar context.
 */
public interface IntCalendarManager {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the name of the calendar (must be unique)
   * @param timezone the timezone for the calendar
   * @throws IllegalArgumentException if a calendar with the same name already exists
   * @throws IllegalArgumentException if name is null or empty
   * @throws IllegalArgumentException if timezone is null
   */
  void createCalendar(String name, ZoneId timezone);

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the name of the calendar
   * @return the calendar with the specified name
   * @throws IllegalArgumentException if no calendar with the given name exists
   */
  IntCalendar getCalendar(String name);

  /**
   * Sets the active calendar by name.
   * The active calendar is used as the default context for operations.
   *
   * @param name the name of the calendar to set as active
   * @throws IllegalArgumentException if no calendar with the given name exists
   */
  void setActiveCalendar(String name);

  /**
   * Gets the currently active calendar.
   *
   * @return the active calendar
   * @throws IllegalStateException if no calendar is currently active
   */
  IntCalendar getActiveCalendar();

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the name of the active calendar
   * @throws IllegalStateException if no calendar is currently active
   */
  String getActiveCalendarName();

  /**
   * Gets a list of all calendar names in the system.
   *
   * @return a list of all calendar names (may be empty)
   */
  List<String> getAllCalendarNames();

  /**
   * Checks if a calendar with the given name exists.
   *
   * @param name the name to check
   * @return true if a calendar with the given name exists, false otherwise
   */
  boolean calendarExists(String name);

  /**
   * Deletes a calendar by name.
   *
   * @param name the name of the calendar to delete
   * @throws IllegalArgumentException if no calendar with the given name exists
   * @throws IllegalStateException    if attempting to delete the active calendar
   */
  void deleteCalendar(String name);

  /**
   * Edits the name of an existing calendar.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws IllegalArgumentException if no calendar with oldName exists
   * @throws IllegalArgumentException if a calendar with newName already exists
   * @throws IllegalArgumentException if newName is null or empty
   */
  void editCalendarName(String oldName, String newName);

  /**
   * Edits the timezone of an existing calendar.
   *
   * @param name        the name of the calendar
   * @param newTimezone the new timezone for the calendar
   * @throws IllegalArgumentException if no calendar with the given name exists
   * @throws IllegalArgumentException if newTimezone is null
   */
  void editCalendarTimezone(String name, ZoneId newTimezone);
}

