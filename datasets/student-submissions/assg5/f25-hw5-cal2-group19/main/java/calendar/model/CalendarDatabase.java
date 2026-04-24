package calendar.model;

import calendar.exception.CalendarNotFoundException;
import calendar.exception.DuplicateCalendarException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Manages multiple calendars with their associated timezones.
 * Maintains a database of calendars mapped by name and tracks the active calendar.
 * Design decisions:
 * - Uses separate maps for calendar instances and timezones as per requirement
 * - Tracks active calendar via a reference (not boolean flags in each calendar)
 * - Ensures thread-safety through defensive copying in getters
 */
public class CalendarDatabase {

  private final Map<String, InCalendar> calendars;
  private final Map<String, ZoneId> timezones;
  private InCalendar activeCalendar;

  /**
   * Constructs an empty CalendarDatabase.
   * No calendars exist initially and no active calendar is set.
   */
  public CalendarDatabase() {
    this.calendars = new HashMap<>();
    this.timezones = new HashMap<>();
    this.activeCalendar = null;
  }

  /**
   * Adds a new calendar to the database.
   *
   * @param name the unique name for the calendar
   * @param calendar the calendar instance
   * @param timezone the timezone for this calendar
   * @throws DuplicateCalendarException if a calendar with this name already exists
   */
  public void addCalendar(String name, InCalendar calendar, ZoneId timezone)
      throws DuplicateCalendarException {
    Objects.requireNonNull(name, "Calendar name cannot be null");
    Objects.requireNonNull(calendar, "Calendar cannot be null");
    Objects.requireNonNull(timezone, "Timezone cannot be null");

    if (calendars.containsKey(name)) {
      throw new DuplicateCalendarException(
          "Calendar with name '" + name + "' already exists");
    }

    calendars.put(name, calendar);
    timezones.put(name, timezone);

    if (activeCalendar == null) {
      activeCalendar = calendar;
    }
  }

  /**
   * Retrieves a calendar by name.
   *
   * @param name the calendar name
   * @return the calendar instance
   * @throws CalendarNotFoundException if calendar doesn't exist
   */
  public InCalendar getCalendar(String name) throws CalendarNotFoundException {
    Objects.requireNonNull(name, "Calendar name cannot be null");

    InCalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    return calendar;
  }

  /**
   * Gets the timezone for a specific calendar.
   *
   * @param name the calendar name
   * @return the timezone
   * @throws CalendarNotFoundException if calendar doesn't exist
   */
  public ZoneId getTimezone(String name) throws CalendarNotFoundException {
    Objects.requireNonNull(name, "Calendar name cannot be null");

    if (!timezones.containsKey(name)) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    return timezones.get(name);
  }

  /**
   * Updates the timezone for a calendar.
   *
   * @param name the calendar name
   * @param newTimezone the new timezone
   * @throws CalendarNotFoundException if calendar doesn't exist
   */
  public void setTimezone(String name, ZoneId newTimezone)
      throws CalendarNotFoundException {
    Objects.requireNonNull(name, "Calendar name cannot be null");
    Objects.requireNonNull(newTimezone, "Timezone cannot be null");

    if (!calendars.containsKey(name)) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    timezones.put(name, newTimezone);
  }

  /**
   * Renames a calendar.
   * Updates both the calendar map and timezone map with the new name.
   *
   * @param oldName the current calendar name
   * @param newName the new calendar name
   * @throws CalendarNotFoundException if old calendar doesn't exist
   * @throws DuplicateCalendarException if new name already exists
   */
  public void renameCalendar(String oldName, String newName)
      throws CalendarNotFoundException, DuplicateCalendarException {
    Objects.requireNonNull(oldName, "Old calendar name cannot be null");
    Objects.requireNonNull(newName, "New calendar name cannot be null");

    if (!calendars.containsKey(oldName)) {
      throw new CalendarNotFoundException("Calendar not found: " + oldName);
    }

    if (calendars.containsKey(newName)) {
      throw new DuplicateCalendarException(
          "Calendar with name '" + newName + "' already exists");
    }

    InCalendar calendar = calendars.remove(oldName);
    ZoneId timezone = timezones.remove(oldName);

    calendars.put(newName, calendar);
    timezones.put(newName, timezone);
    calendar.setCalendarName(newName);
  }

  /**
   * Sets the active calendar.
   *
   * @param name the name of the calendar to make active
   * @throws CalendarNotFoundException if calendar doesn't exist
   */
  public void setActiveCalendar(String name) throws CalendarNotFoundException {
    Objects.requireNonNull(name, "Calendar name cannot be null");

    InCalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new CalendarNotFoundException("Calendar not found: " + name);
    }
    this.activeCalendar = calendar;
  }

  /**
   * Gets the currently active calendar.
   *
   * @return the active calendar, or null if none is set
   */
  public InCalendar getActiveCalendar() {
    return activeCalendar;
  }

  /**
   * Checks if any calendars exist in the database.
   *
   * @return true if at least one calendar exists, false otherwise
   */
  public boolean isEmpty() {
    return calendars.isEmpty();
  }

  @Override
  public String toString() {
    return "CalendarDatabase{"
        + "calendarCount=" + calendars.size()
        + ", activeCalendar=" + (activeCalendar != null
        ? activeCalendar.getCalendarName() : "none")
        + '}';
  }
}