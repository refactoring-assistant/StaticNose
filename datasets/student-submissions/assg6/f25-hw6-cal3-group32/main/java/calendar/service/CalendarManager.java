package calendar.service;

import calendar.model.Calendar;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages multiple calendars in the application.
 */
public class CalendarManager {
  private final Map<String, Calendar> calendars;

  /**
   * Creates calendar manager with empty calendar collection.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
  }

  /**
   * Creates new calendar with name and timezone.
   *
   * @param name the calendar name
   * @param timezone the calendar timezone
   * @throws IllegalArgumentException if name exists or is invalid
   */
  public void createCalendar(String name, ZoneId timezone) {
    validateName(name);
    if (exists(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' already exists");
    }
    calendars.put(name, new Calendar(name, timezone));
  }

  /**
   * Gets calendar by name.
   *
   * @param name the calendar name
   * @return the calendar
   * @throws IllegalArgumentException if name is invalid or calendar not found
   */
  public Calendar getCalendar(String name) {
    validateName(name);
    if (!exists(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
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
   * Renames calendar.
   *
   * @param oldName the current calendar name
   * @param newName the new calendar name
   * @throws IllegalArgumentException if names are invalid or calendar not found
   */
  public void renameCalendar(String oldName, String newName) {
    String oldValidated = validateAndReturn(oldName);
    String newValidated = validateAndReturn(newName);
    if (!exists(oldValidated)) {
      throw new IllegalArgumentException("Calendar '" + oldValidated + "' does not exist");
    }
    if (exists(newValidated)) {
      throw new IllegalArgumentException("Calendar '" + newValidated + "' already exists");
    }
    Calendar cal = calendars.remove(oldValidated);
    cal.setName(newValidated);
    calendars.put(newValidated, cal);
  }

  /**
   * Validates name and returns it.
   *
   * @param name the name to validate
   * @return the validated name
   * @throws IllegalArgumentException if name is invalid
   */
  private String validateAndReturn(String name) {
    validateName(name);
    return name;
  }

  /**
   * Changes calendar timezone.
   *
   * @param name the calendar name
   * @param newTimezone the new timezone
   * @throws IllegalArgumentException if name is invalid or calendar not found
   */
  public void changeTimezone(String name, ZoneId newTimezone) {
    getCalendar(name).setTimezone(newTimezone);
  }

  /**
   * Checks if calendar exists.
   *
   * @param name the calendar name
   * @return true if calendar exists
   */
  public boolean exists(String name) {
    return calendars.containsKey(name);
  }

  /**
   * Deletes calendar.
   *
   * @param name the calendar name
   * @throws IllegalArgumentException if name is invalid or calendar not found
   */
  public void deleteCalendar(String name) {
    validateName(name);
    if (!exists(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    calendars.remove(name);
  }

  /**
   * Gets number of calendars.
   *
   * @return the calendar count
   */
  public int size() {
    return calendars.size();
  }

  /**
   * Validates calendar name is not null or empty.
   * Made public for comprehensive testing (professor-approved).
   *
   * @param name the name to validate
   * @throws IllegalArgumentException if name is null or empty
   */
  public void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
  }
}