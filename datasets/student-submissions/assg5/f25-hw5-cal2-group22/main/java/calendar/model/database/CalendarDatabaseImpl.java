package calendar.model.database;

import calendar.model.Imodel;
import calendar.model.ModelImpl;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of Multiple Calendars.
 */

public class CalendarDatabaseImpl implements IcalendarDatabase {

  private final Map<String, Imodel> calendars = new HashMap<>();
  private final Map<String, ZoneId> calendarTimeZones = new HashMap<>();
  private String currentCalendarName;
  private Imodel currentCalendar;

  /**
   * Constructor - no default calendar created.
   */
  public CalendarDatabaseImpl() {
    this.currentCalendarName = null;
    this.currentCalendar = null;
  }

  /**
   * The method to create the calendar with given name and time zone.
   *
   * @param name     - name of the calendar
   * @param timezone - the time zone of the calendar
   * @throws IllegalArgumentException - if the name already exists or no such time zone
   */
  @Override
  public void createCalendar(String name, ZoneId timezone) throws IllegalArgumentException {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name is null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("ZoneId is null");
    }

    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with given name already exists");
    }
    calendarTimeZones.put(name, timezone);
    calendars.put(name, new ModelImpl(timezone));
  }

  /**
   * It renames the name of the calendar if it already exists.
   *
   * @param oldName - the original name of the calendar
   * @param newName - the name it is being changed to
   * @throws IllegalArgumentException - if the new name already exists or is empty (null too)
   */
  @Override
  public void renameCalendar(String oldName, String newName) throws IllegalArgumentException {
    if (oldName == null || oldName.isEmpty() || newName == null || newName.isEmpty()) {
      throw new IllegalArgumentException("Names cannot be null or empty");
    }
    if (!(calendars.containsKey(oldName))) {
      throw new IllegalArgumentException("Calendar with '" + oldName + "' does not exist");
    }

    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("Calendar with '" + newName + "' already exists");
    }
    Imodel cal = calendars.remove(oldName);
    calendars.put(newName, cal);

    ZoneId zoneId = calendarTimeZones.remove(oldName);
    calendarTimeZones.put(newName, zoneId);

    if (oldName.equals(currentCalendarName)) {
      currentCalendarName = newName;
    }
  }

  /**
   * It changes the time zone of the existing calendar.
   *
   * @param name     - name of the calendar whose time zone is being changed
   * @param timezone - the time zone to which it is being changed to
   * @throws IllegalArgumentException - if there are any issues in changing the time zone
   */
  @Override
  public void changeTimeZone(String name, ZoneId timezone) throws IllegalArgumentException {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name is null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Time Zone is null");
    }

    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }

    Imodel calendar = calendars.get(name);
    calendar.changeTimeZone(timezone);

    calendarTimeZones.put(name, timezone);
  }

  /**
   * This is for using the calendar that is requested.
   *
   * @param name - name of the calendar
   * @throws IllegalArgumentException - if there is no such calendar
   */
  @Override
  public void useCalendar(String name) throws IllegalArgumentException {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name is null or empty");
    }
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }

    this.currentCalendarName = name;
    this.currentCalendar = calendars.get(name);
  }

  /**
   * lets us know what is the current calendar.
   *
   * @return the Icalendar
   */
  @Override
  public Optional<Imodel> getCurrent() {
    return Optional.ofNullable(currentCalendar);
  }

  /**
   * gives calendar that is being requested.
   *
   * @param name - name of the requested calendar
   * @return the requested calendar
   */
  @Override
  public Optional<Imodel> get(String name) {
    return Optional.ofNullable(calendars.get(name));
  }

  /**
   * Returns the name of the current calendar.
   *
   * @return the name of the current calendar
   */
  @Override
  public String getCurrentCalendarName() {
    if (currentCalendarName == null) {
      throw new IllegalArgumentException("Current calendar name is null");
    }
    return this.currentCalendarName;
  }

  /**
   * Check if any calendar exists in the system.
   *
   * @return true if at least one calendar exists
   */
  @Override
  public boolean hasCalendars() {
    return !calendars.isEmpty();
  }

  /**
   * Get all available calendar names.
   *
   * @return set of calendar names
   */
  @Override
  public Set<String> getCalendarNames() {
    return new HashSet<>(calendars.keySet());
  }

  /**
   * Get the time zone of the calendar.
   *
   * @param calendarName name of the calendar
   * @return time zone
   */
  @Override
  public ZoneId getTimeZone(String calendarName) {
    if (calendarName == null || calendarName.isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }

    Imodel calendar = calendars.get(calendarName);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' does not exist");
    }

    return calendar.getTimeZone();
  }


}
