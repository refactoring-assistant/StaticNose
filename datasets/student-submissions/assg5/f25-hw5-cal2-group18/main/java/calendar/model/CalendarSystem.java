package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Facade interface for managing multiple calendars.
 * Provides a simplified interface for calendar operations.
 */
public interface CalendarSystem {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the unique calendar name
   * @param timezone the calendar timezone
   * @throws IllegalArgumentException if name is duplicate or parameters are invalid
   */
  void createCalendar(String name, ZoneId timezone);

  /**
   * Gets a calendar by name.
   *
   * @param name the calendar name
   * @return the calendar, or null if not found
   */
  Calendar getCalendar(String name);

  /**
   * Gets the currently active calendar.
   *
   * @return the current calendar, or null if none selected
   */
  Calendar getCurrentCalendar();

  /**
   * Sets the current calendar context.
   *
   * @param name the calendar name
   * @throws IllegalArgumentException if calendar not found
   */
  void setCurrentCalendar(String name);

  /**
   * Edits a calendar property (name or timezone).
   *
   * @param calendarName the calendar to edit
   * @param property     the property to change ("name" or "timezone")
   * @param newValue     the new value
   * @throws IllegalArgumentException if calendar not found or property/value invalid
   */
  void editCalendar(String calendarName, String property, String newValue);

  /**
   * Copies a single event to another calendar.
   *
   * @param eventName     the event subject
   * @param sourceStart   the event start time in source calendar
   * @param targetCalName the target calendar name
   * @param targetStart   the new start time in target calendar's timezone
   * @throws IllegalArgumentException if event or calendar not found
   * @throws IllegalStateException    if copy would create conflict
   */
  void copyEvent(String eventName, LocalDateTime sourceStart, String targetCalName,
                 LocalDateTime targetStart);

  /**
   * Copies all events on a specific date to another calendar.
   *
   * @param sourceDate    the date in source calendar
   * @param targetCalName the target calendar name
   * @param targetDate    the date in target calendar
   * @throws IllegalArgumentException if calendar not found
   * @throws IllegalStateException    if any copy would create conflict
   */
  void copyEventsOnDate(LocalDate sourceDate, String targetCalName, LocalDate targetDate);

  /**
   * Copies all events in a date range to another calendar.
   *
   * @param startDate      the range start date (inclusive)
   * @param endDate        the range end date (inclusive)
   * @param targetCalName  the target calendar name
   * @param targetStartDate the date in target calendar corresponding to range start
   * @throws IllegalArgumentException if calendar not found or dates invalid
   * @throws IllegalStateException    if any copy would create conflict
   */
  void copyEventsBetween(LocalDate startDate, LocalDate endDate, String targetCalName,
                         LocalDate targetStartDate);

  /**
   * Gets all calendar names in the system.
   *
   * @return list of calendar names
   */
  List<String> getAllCalendarNames();
}