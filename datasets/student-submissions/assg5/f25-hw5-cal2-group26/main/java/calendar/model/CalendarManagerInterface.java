package calendar.model;

import java.time.ZoneId;
import java.util.Collection;

/**
 * Defines operations for managing multiple calendars
 * and maintaining an active calendar context.
 */
public interface CalendarManagerInterface {

  /**
   * Creates a new calendar with the given name and timezone.
   * Automatically sets it as active if it's the first one.
   *
   * @param name unique name of the calendar
   * @param zone timezone of the calendar
   * @throws IllegalArgumentException if name already exists or zone is null
   */
  void createCalendar(String name, ZoneId zone);

  /**
   * Deletes a calendar by name. If it was active, another calendar becomes active automatically.
   *
   * @param name name of the calendar to delete
   * @throws IllegalArgumentException if calendar does not exist
   */
  void deleteCalendar(String name);

  /**
   * Switches the active calendar to the one specified by name.
   *
   * @param name name of the calendar to switch to
   * @throws IllegalArgumentException if calendar does not exist
   */
  void useCalendar(String name);

  /**
   * Returns the currently active calendar.
   *
   * @return the active calendar
   * @throws IllegalStateException if no active calendar is set
   */
  CalendarInterface getActiveCalendar();

  /**
   * Returns the calendar by name.
   *
   * @param name the name of the calendar
   * @return the matching calendar
   * @throws IllegalArgumentException if not found
   */
  CalendarInterface getCalendar(String name);

  /**
   * Returns all calendars currently managed.
   *
   * @return unmodifiable collection of all calendars
   */
  Collection<CalendarInterface> getAllCalendars();

  /**
   * Updates the name of an existing calendar.
   *
   * @param currentName the current name of the calendar; must not be null
   * @param newName     the new name to assign; must not be null and must be unique
   * @throws IllegalArgumentException if no calendar exists with currentName
   *                                  or if a calendar with newName already exists
   */
  void editCalendarName(String currentName, String newName);

  /**
   * Changes the timezone of an existing calendar.
   * All events in the calendar are updated to reflect the new timezone.
   *
   * @param name    the name of the calendar to update; must not be null
   * @param newZone the new timezone to assign; must not be null
   * @throws IllegalArgumentException if no calendar exists with the given name
   */
  void changeCalendarTimezone(String name, ZoneId newZone);

}
