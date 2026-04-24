package calendar.model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages multiple calendars in the system.
 * Each calendar has a unique name.
 *
 * <p>Assignment 5 Addition: Supports multi-calendar management with
 * proper handling of calendar switching, renaming, and timezone changes.
 *
 * @author MH
 * @version 2.0
 */
public class CalendarManager {

  private final Map<String, Calendar> calendars;
  private Calendar currentCalendar;

  /**
   * Creates a new calendar manager with no calendars.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
    this.currentCalendar = null;
  }

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name unique calendar name
   * @param timezone timezone in IANA format
   * @return the created calendar
   * @throws IllegalArgumentException if name already exists or timezone invalid
   */
  public Calendar createCalendar(String name, ZoneId timezone) {
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }
    Calendar cal = new Calendar(name, timezone);
    calendars.put(name, cal);
    return cal;
  }

  /**
   * Gets a calendar by name.
   *
   * @param name calendar name
   * @return Optional containing the calendar if found
   */
  public Optional<Calendar> getCalendar(String name) {
    return Optional.ofNullable(calendars.get(name));
  }

  /**
   * Gets the currently active calendar.
   *
   * @return Optional containing current calendar if set
   */
  public Optional<Calendar> getCurrentCalendar() {
    return Optional.ofNullable(currentCalendar);
  }

  /**
   * Gets the currently active calendar.
   * Alias for getCurrentCalendar() for GUI controller compatibility.
   *
   * @return Optional containing current calendar if set
   */
  public Optional<Calendar> getActiveCalendar() {
    return getCurrentCalendar();
  }

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the name of active calendar, or null if no calendar is active
   */
  public String getActiveCalendarName() {
    if (currentCalendar == null) {
      return null;
    }
    return currentCalendar.getName();
  }

  /**
   * Sets the current active calendar.
   *
   * @param name name of calendar to use
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  public void useCalendar(String name) {
    Calendar cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    this.currentCalendar = cal;
  }

  /**
   * Edits a calendar property (name or timezone).
   *
   * @param calendarName current calendar name
   * @param property property to edit (name or timezone)
   * @param newValue new value for the property
   * @throws IllegalArgumentException if property invalid or calendar not found
   */
  public void editCalendar(String calendarName, String property, String newValue) {
    Calendar cal = calendars.get(calendarName);
    if (cal == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' does not exist");
    }

    switch (property.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newValue) && !newValue.equals(calendarName)) {
          throw new IllegalArgumentException("Calendar with name '" + newValue
              + "' already exists");
        }

        calendars.remove(calendarName);
        cal.setName(newValue);
        calendars.put(newValue, cal);

        if (currentCalendar == cal) {
          currentCalendar = cal;
        }
        break;

      case "timezone":
        try {
          ZoneId newZone = ZoneId.of(newValue);
          cal.setTimezone(newZone);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + newValue);
        }
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Gets all calendar names.
   *
   * @return map of all calendars (defensive copy)
   */
  public Map<String, Calendar> getAllCalendars() {
    return new HashMap<>(calendars);
  }
}