package calendar.controller;

import calendar.model.CalendarModel;
import calendar.model.CalendarSystem;

/**
 * Maintains context for which calendar is currently in use.
 * Required for multi-calendar support.
 */
public class CalendarContext {
  private final CalendarSystem system;
  private String currentCalendarName;

  /**
   * Constructs a calendar context.
   *
   * @param system the calendar system managing all calendars
   */
  public CalendarContext(CalendarSystem system) {
    this.system = system;
    this.currentCalendarName = null;
  }

  /**
   * Gets the calendar system.
   *
   * @return the system
   */
  public CalendarSystem getSystem() {
    return system;
  }

  /**
   * Sets the current calendar by name.
   *
   * @param name the calendar name
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  public void setCurrentCalendar(String name) {
    if (!system.calendarExists(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    this.currentCalendarName = name;
  }

  /**
   * Gets the current calendar.
   *
   * @return the current calendar
   * @throws IllegalStateException if no calendar is in use
   */
  public CalendarModel getCurrentCalendar() {
    if (currentCalendarName == null) {
      throw new IllegalStateException("No calendar is currently in use. "
          + "Use 'use calendar --name <name>' first.");
    }
    return system.getCalendar(currentCalendarName);
  }

  /**
   * Gets the current calendar name.
   *
   * @return the name, or null if none set
   */
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  /**
   * Checks if a calendar is currently in use.
   *
   * @return true if a calendar is set
   */
  public boolean hasCurrentCalendar() {
    return currentCalendarName != null;
  }
}