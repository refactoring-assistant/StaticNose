package calendar.model.interfaces;

import java.util.Map;

/**
 * Represents a calendar manager which can manage multiple calendar at a time.
 * A user can add, remove, update the name and time zone of a calendar using this calendar manager.
 */
public interface CalendarContainer {

  /**
   * Adds a new calendar advancedCalendar with give name.
   *
   * @param name             the name of the new calendar to be added
   * @param advancedCalendar the new calendar to be added
   */
  void addCalendar(String name, AdvancedCalendar advancedCalendar);

  /**
   * Updates the field value of a calendar with the new value.
   *
   * @param name     the name of the calendar to be updated
   * @param property the field value property to be updated (name or time zone)
   * @param newValue the new value for the given property to update
   */
  void updateCalendar(String name, String property, String newValue);

  /**
   * Returns the current active calendar.
   *
   * @return the active calendar at the moment.
   */
  AdvancedCalendar getActiveCalendar();

  /**
   * Sets the active calendar as calendar corresponding to the given name.
   *
   * @param name the name of the calendar to be made active.
   */
  void setActiveCalendar(String name);

  /**
   * Returns all the current existing calendars.
   *
   * @return a map of all existing calendars.
   */
  Map<String, AdvancedCalendar> getCalendars();
}
