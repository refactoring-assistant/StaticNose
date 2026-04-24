package calendar.model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages multiple calendars in the application.
 * Provides operations to create, retrieve, and manage calendars.
 *
 * <p>Design rationale:
 * - Singleton pattern ensures only one manager exists
 * - Maps calendar names to Calendar instances
 * - Enforces unique calendar names
 */
public class CalendarManager {
  private final Map<String, Calendar> calendars;
  private static CalendarManager instance;

  /**
   * Private constructor for singleton pattern.
   */
  CalendarManager() {
    this.calendars = new HashMap<>();
  }

  /**
   * Gets the singleton instance of CalendarManager.
   *
   * @return CalendarManager instance
   */
  public static CalendarManager getInstance() {
    if (instance == null) {
      instance = new CalendarManager();
    }
    return instance;
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     Calendar name (must be unique)
   * @param timezone Calendar timezone
   * @return The created calendar
   * @throws IllegalArgumentException if name already exists or is invalid
   */
  public Calendar createCalendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }

    Calendar calendar = new CalendarImpl(name, timezone);
    calendars.put(name, calendar);
    return calendar;
  }

  /**
   * Gets a calendar by name.
   *
   * @param name Calendar name
   * @return Calendar with the given name
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  public Calendar getCalendar(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    Calendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    return calendar;
  }

  /**
   * Checks if a calendar with the given name exists.
   *
   * @param name Calendar name
   * @return true if calendar exists
   */
  public boolean calendarExists(String name) {
    return calendars.containsKey(name);


  }

  /**
   * Gets all calendar names.
   *
   * @return Set of calendar names
   */
  public Set<String> getCalendarNames() {
    return calendars.keySet();
  }

  /**
   * Removes a calendar by name.
   *
   * @param name Calendar name
   * @return true if calendar was removed
   */
  public boolean removeCalendar(String name) {
    return calendars.remove(name) != null;
  }

  /**
   * Renames a calendar.
   *
   * @param oldName Current name
   * @param newName New name
   * @throws IllegalArgumentException if old name doesn't exist or new name already exists
   */
  public void renameCalendar(String oldName, String newName) {
    if (!calendarExists(oldName)) {
      throw new IllegalArgumentException("Calendar '" + oldName + "' does not exist");
    }
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("New calendar name cannot be null or empty");
    }
    if (calendarExists(newName) && !oldName.equals(newName)) {
      throw new IllegalArgumentException("Calendar with name '" + newName + "' already exists");
    }

    Calendar calendar = calendars.remove(oldName);
    calendar.setName(newName);
    calendars.put(newName, calendar);
  }

  /**
   * Gets the number of calendars.
   *
   * @return Calendar count
   */
  public int getCalendarCount() {
    return calendars.size();
  }

  /**
   * Resets the manager (useful for testing).
   */
  public void reset() {
    calendars.clear();
  }

  /**
   * Removes all calendars.
   * Alias for reset() for backward compatibility.
   */
  public void clear() {
    calendars.clear();
  }
}