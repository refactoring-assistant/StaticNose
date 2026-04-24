package calendar.model;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages multiple calendars.
 */
public class MultiCalendarModel {
  private final Map<String, Calendar> calendars;
  private Calendar currentCalendar;

  /**
   * Constructs a new MultiCalendarModel.
   */
  public MultiCalendarModel() {
    this.calendars = new HashMap<>();
    // Create a default calendar
    Calendar defaultCal = new Calendar("default", ZoneId.of("America/New_York"));
    calendars.put(defaultCal.getName(), defaultCal);
    this.currentCalendar = defaultCal;
  }

  /**
   * Creates a new calendar.
   *
   * @param name the calendar name
   * @param timezone the timezone
   * @throws IllegalArgumentException if calendar name already exists
   */
  public void createCalendar(String name, ZoneId timezone) {
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }

    Calendar calendar = new Calendar(name, timezone);
    calendars.put(name, calendar);
  }

  /**
   * Gets a calendar by name.
   *
   * @param name the calendar name
   * @return the calendar or null if not found
   */
  public Calendar getCalendar(String name) {
    return calendars.get(name);
  }

  /**
   * Gets all calendars.
   *
   * @return list of all calendars
   */
  public List<Calendar> getAllCalendars() {
    return new ArrayList<>(calendars.values());
  }

  /**
   * Sets the current calendar.
   *
   * @param name the calendar name
   * @throws IllegalArgumentException if calendar not found
   */
  public void setCurrentCalendar(String name) {
    Calendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found");
    }
    this.currentCalendar = calendar;
  }

  /**
   * Gets the current calendar.
   *
   * @return the current calendar
   */
  public Calendar getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Edits a calendar property.
   *
   * @param name the calendar name
   * @param property the property to edit
   * @param newValue the new value
   * @throws IllegalArgumentException if calendar not found or invalid property/value
   */
  public void editCalendar(String name, String property, String newValue) {
    Calendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found");
    }

    switch (property.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newValue) && !name.equals(newValue)) {
          throw new IllegalArgumentException("Calendar name '" + newValue + "' already exists");
        }
        // Remove old entry and add new one
        calendars.remove(name);
        calendar = new Calendar(newValue, calendar.getTimezone());
        calendars.put(newValue, calendar);
        if (currentCalendar.getName().equals(name)) {
          currentCalendar = calendar;
        }
        break;
      case "timezone":
        ZoneId newTimezone = ZoneId.of(newValue);
        calendar.setTimezone(newTimezone);
        break;
      default:
        throw new IllegalArgumentException("Unknown calendar property: " + property);
    }
  }

  /**
   * Checks if a calendar exists.
   *
   * @param name the calendar name
   * @return true if exists
   */
  public boolean hasCalendar(String name) {
    return calendars.containsKey(name);
  }
}