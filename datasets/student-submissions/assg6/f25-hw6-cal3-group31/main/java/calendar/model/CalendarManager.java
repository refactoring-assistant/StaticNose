package calendar.model;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of IntCalendarManager that manages multiple calendars.
 * Maintains a registry of calendars and tracks the currently active calendar.
 */
public class CalendarManager implements IntCalendarManager {

  private final Map<String, IntCalendar> calendars;
  private String activeCalendarName;

  /**
   * Constructs a new CalendarManager with no calendars.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
    this.activeCalendarName = null;
  }

  @Override
  public void createCalendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("A calendar with the name '" + name + "' already exists");
    }

    IntCalendar calendar = new Calendar(name, timezone);
    calendars.put(name, calendar);

    // Set as active if it's the first calendar
    if (activeCalendarName == null) {
      activeCalendarName = name;
    }
  }

  @Override
  public IntCalendar getCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No calendar found with name: " + name);
    }
    return calendars.get(name);
  }

  @Override
  public void setActiveCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No calendar found with name: " + name);
    }
    this.activeCalendarName = name;
  }

  @Override
  public IntCalendar getActiveCalendar() {
    if (activeCalendarName == null) {
      throw new IllegalStateException("No active calendar set");
    }
    return calendars.get(activeCalendarName);
  }

  @Override
  public String getActiveCalendarName() {
    if (activeCalendarName == null) {
      throw new IllegalStateException("No active calendar set");
    }
    return activeCalendarName;
  }

  @Override
  public List<String> getAllCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public boolean calendarExists(String name) {
    return calendars.containsKey(name);
  }

  @Override
  public void deleteCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No calendar found with name: " + name);
    }
    if (name.equals(activeCalendarName)) {
      throw new IllegalStateException(
          "Cannot delete the active calendar. Please switch to another calendar first.");
    }
    calendars.remove(name);
  }

  @Override
  public void editCalendarName(String oldName, String newName) {
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("New calendar name cannot be null or empty");
    }
    if (!calendars.containsKey(oldName)) {
      throw new IllegalArgumentException("No calendar found with name: " + oldName);
    }
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException(
          "A calendar with the name '" + newName + "' already exists");
    }

    IntCalendar calendar = calendars.get(oldName);
    IntCalendar updatedCalendar = calendar.withName(newName);

    calendars.remove(oldName);
    calendars.put(newName, updatedCalendar);

    // Update active calendar name if necessary
    if (oldName.equals(activeCalendarName)) {
      activeCalendarName = newName;
    }
  }

  @Override
  public void editCalendarTimezone(String name, ZoneId newTimezone) {
    if (newTimezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No calendar found with name: " + name);
    }

    IntCalendar calendar = calendars.get(name);
    IntCalendar updatedCalendar = calendar.withTimezone(newTimezone);
    calendars.put(name, updatedCalendar);
  }
}

