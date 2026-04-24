package calendar.model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages multiple calendars, acting as a facade for the calendar subsystem.
 */
public class CalendarManager {

  private final Map<String, MyCalendar> calendars;
  private MyCalendar activeCalendar;

  /**
   * Constructs a new CalendarManager.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
  }

  /**
   * Creates a new calendar.
   *
   * @param name   The name of the calendar.
   * @param zoneId The time zone of the calendar.
   * @throws IllegalArgumentException if a calendar with the same name already exists.
   */
  public void createCalendar(String name, ZoneId zoneId) {
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("A calendar with the name '" + name + "' already exists.");
    }
    MyCalendar newCalendar = new MyCalendar(zoneId);
    calendars.put(name, newCalendar);
  }

  /**
   * Gets the currently active calendar.
   *
   * @return An Optional containing the active calendar, or empty if no calendar is active.
   */
  public Optional<MyCalendar> getActiveCalendar() {
    return Optional.ofNullable(activeCalendar);
  }

  /**
   * Sets the active calendar.
   *
   * @param name The name of the calendar to set as active.
   * @throws IllegalArgumentException if no calendar with the given name exists.
   */
  public void useCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No calendar with the name '" + name + "' found.");
    }
    this.activeCalendar = calendars.get(name);
  }

  /**
   * Gets a calendar by its name.
   *
   * @param name The name of the calendar.
   * @return An Optional containing the calendar, or empty if not found.
   */
  public Optional<MyCalendar> getCalendar(String name) {
    return Optional.ofNullable(calendars.get(name));
  }

  /**
   * Renames an existing calendar, updating both the calendar instance and manager lookup table.
   *
   * @param currentName The existing calendar name.
   * @param newName     The new calendar name.
   * @throws IllegalArgumentException if the current calendar is not found or the new name is taken.
   */
  public void renameCalendar(String currentName, String newName) {
    if (!calendars.containsKey(currentName)) {
      throw new IllegalArgumentException("Calendar '" + currentName + "' not found.");
    }
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("A calendar with the name '" + newName
              + "' already exists.");
    }

    MyCalendar calendar = calendars.remove(currentName);
    calendars.put(newName, calendar);

  }
}
