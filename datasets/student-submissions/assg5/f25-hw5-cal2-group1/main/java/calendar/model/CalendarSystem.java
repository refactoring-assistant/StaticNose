package calendar.model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages multiple calendars in the system.
 * Representation: Uses a HashMap for O(1) lookup by calendar name.
 */
public class CalendarSystem {
  private final Map<String, Icalendar> calendars;
  private Icalendar currentCalendar;

  /**
   * Creates a new calendar system.
   */
  public CalendarSystem() {
    this.calendars = new HashMap<>();
    this.currentCalendar = null;
  }

  /**
   * Creates a new calendar.
   */
  public void createCalendar(String name, ZoneId timezone) {
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException(
          "A calendar with name '" + name + "' already exists");
    }
    Icalendar calendar = new Calendar(name, timezone);
    calendars.put(name, calendar);
  }

  /**
   * Gets a calendar by name.
   */
  public Icalendar getCalendar(String name) {
    return calendars.get(name);
  }

  /**
   * Sets the current calendar context.
   */
  public void useCalendar(String name) {
    Icalendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    this.currentCalendar = calendar;
  }

  /**
   * Gets the current calendar.
   */
  public Icalendar getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Edits a calendar property.
   */
  public void editCalendar(String calendarName, String property, String value) {
    Icalendar calendar = calendars.get(calendarName);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' does not exist");
    }

    if ("name".equalsIgnoreCase(property)) {
      if (calendars.containsKey(value)) {
        throw new IllegalArgumentException(
            "A calendar with name '" + value + "' already exists");
      }
      calendars.remove(calendarName);
      calendar.setName(value);
      calendars.put(value, calendar);
      if (currentCalendar == calendar) {
        currentCalendar = calendar;
      }
    } else if ("timezone".equalsIgnoreCase(property)) {
      try {
        ZoneId timezone = ZoneId.of(value);
        calendar.setTimezone(timezone);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid timezone: " + value);
      }
    } else {
      throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Checks if a calendar exists.
   */
  public boolean hasCalendar(String name) {
    return calendars.containsKey(name);
  }
}
