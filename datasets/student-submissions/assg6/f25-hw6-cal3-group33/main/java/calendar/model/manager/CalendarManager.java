package calendar.model.manager;

import calendar.exceptions.CalendarNotFoundException;
import calendar.exceptions.DuplicateCalendarException;
import calendar.exceptions.InvalidDateTimeException;
import calendar.exceptions.InvalidTimezoneException;
import calendar.exceptions.NoCalendarInUseException;
import calendar.model.calendar.CalendarInterface;
import java.time.ZoneId;
import java.util.Set;

/**
 * Manages multiple calendars and tracks which calendar is currently in use.
 * Provides operations to create, edit, select, and retrieve calendars.
 */
public interface CalendarManager {

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name unique name for the calendar
   * @param timezone IANA timezone identifier (e.g., "America/New_York")
   * @throws DuplicateCalendarException if a calendar with this name already exists
   * @throws InvalidTimezoneException if the timezone string is not a valid IANA timezone
   */
  void createCalendar(String name, String timezone)
      throws DuplicateCalendarException, InvalidTimezoneException;

  /**
   * Changes the name of an existing calendar.
   * If the renamed calendar is currently in use, the current calendar reference is updated.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws CalendarNotFoundException if the calendar with oldName does not exist
   * @throws DuplicateCalendarException if a calendar with newName already exists
   */
  void editCalendarName(String oldName, String newName)
      throws CalendarNotFoundException, DuplicateCalendarException;

  /**
   * Changes the timezone of an existing calendar.
   *
   * @param name the name of the calendar
   * @param newTimezone IANA timezone identifier (e.g., "Europe/London")
   * @throws CalendarNotFoundException if the calendar does not exist
   * @throws InvalidTimezoneException if the timezone string is not valid
   */
  void editCalendarTimezone(String name, String newTimezone)
      throws CalendarNotFoundException, InvalidTimezoneException, InvalidDateTimeException;

  /**
   * Selects a calendar to be the current calendar for operations.
   *
   * @param name the name of the calendar to use
   * @throws CalendarNotFoundException if the calendar does not exist
   */
  void useCalendar(String name) throws CalendarNotFoundException;

  /**
   * Returns the currently selected calendar.
   *
   * @return the current calendar
   * @throws NoCalendarInUseException if no calendar is currently selected
   */
  CalendarInterface getCurrentCalendar() throws NoCalendarInUseException;

  /**
   * Returns the name of the currently selected calendar.
   *
   * @return the current calendar name, or null if no calendar is selected
   */
  String getCurrentCalendarName();

  /**
   * Retrieves a specific calendar by name.
   *
   * @param name the name of the calendar
   * @return the calendar with the given name
   * @throws CalendarNotFoundException if the calendar does not exist
   */
  CalendarInterface getCalendar(String name) throws CalendarNotFoundException;

  /**
   * Checks if a calendar with the given name exists.
   *
   * @param name the calendar name to check
   * @return true if the calendar exists, false otherwise
   */
  boolean hasCalendar(String name);

  /**
   * Returns the names of all calendars.
   *
   * @return a set of all calendar names (empty set if no calendars exist)
   */
  Set<String> getAllCalendarNames();

  /**
   * Deletes a calendar by name.
   * If the deleted calendar is currently in use, the current calendar selection is cleared.
   * Note: This operation may not be supported in all implementations.
   *
   * @param name the name of the calendar to delete
   * @throws CalendarNotFoundException if the calendar does not exist
   * @throws UnsupportedOperationException if deletion is not supported
   */
  void deleteCalendar(String name) throws CalendarNotFoundException;
}