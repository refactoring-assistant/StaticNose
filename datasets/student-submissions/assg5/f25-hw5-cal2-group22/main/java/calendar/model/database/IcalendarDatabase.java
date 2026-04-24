package calendar.model.database;

import calendar.model.Imodel;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

/**
 * This interface will store multiple calendars.
 */

public interface IcalendarDatabase {


  /**
   * The method to create the calendar with given name and time zone.
   *
   * @param name   - name of the calendar
   * @param zoneId - the time zone of the calendar
   * @throws IllegalArgumentException - if the name already exists or no such time zone
   */
  void createCalendar(String name, ZoneId zoneId) throws IllegalArgumentException;

  /**
   * It renames the name of the calendar if it already exists.
   *
   * @param oldName - the original name of the calendar
   * @param newName - the name it is being changed to
   * @throws IllegalArgumentException - if the new name already exists or is empty (null too)
   */
  void renameCalendar(String oldName, String newName) throws IllegalArgumentException;

  /**
   * It changes the time zone of the existing calendar.
   *
   * @param name   - name of the calendar whose time zone is being changed
   * @param zoneId - the time zone to which it is being changed to
   * @throws IllegalArgumentException - if there are any issues in changing the time zone
   */
  void changeTimeZone(String name, ZoneId zoneId) throws IllegalArgumentException;

  /**
   * This is for using the calendar that is requested.
   *
   * @param name - name of the calendar
   * @throws IllegalArgumentException - if there is no such calendar
   */
  void useCalendar(String name) throws IllegalArgumentException;

  /**
   * lets us know what is the current calendar.
   *
   * @return the Icalendar
   */
  Optional<Imodel> getCurrent();

  /**
   * gives calendar that is being requested.
   *
   * @param name - name of the requested calendar
   * @return the requested calendar
   */
  Optional<Imodel> get(String name);

  /**
   * Returns the name of the current calendar.
   *
   * @return the name of the current calendar
   */
  String getCurrentCalendarName();

  /**
   * Check if any calendar exists in the system.
   *
   * @return true if at least one calendar exists
   */
  boolean hasCalendars();

  /**
   * Get all available calendar names.
   *
   * @return set of calendar names
   */
  Set<String> getCalendarNames();

  /**
   * Get the time zone of the calendar.
   *
   * @param name is the calendar name.
   * @return time zone
   */
  ZoneId getTimeZone(String name);

}