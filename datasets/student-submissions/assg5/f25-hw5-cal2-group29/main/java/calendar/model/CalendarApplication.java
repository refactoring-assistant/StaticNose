package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Defines the contract for managing multiple calendars, handling
 * calendar-wide operations like creation, editing, and copying events
 * between them.
 */
public interface CalendarApplication {

  /**
   * Creates a new, empty calendar with a unique name and a specific timezone.
   *
   * @param name     The unique name for the new calendar.
   * @param timezone The IANA timezone string (e.g., "America/New_York").
   * @throws IllegalArgumentException if the name is not unique or the timezone is invalid.
   */
  void createCalendar(String name, String timezone) throws IllegalArgumentException;

  /**
   * Edits a property of an existing calendar.
   *
   * @param name     The current (unique) name of the calendar to edit.
   * @param property The property to change ("name" or "timezone").
   * @param newValue The new value for the property.
   * @throws IllegalArgumentException if the calendar isn't found, the property
   *                                  is unknown, or the new value is invalid.
   */
  void editCalendar(String name, String property, String newValue) throws IllegalArgumentException;

  /**
   * Sets a calendar as the "active" context for all event-related commands.
   *
   * @param name The name of the calendar to "use".
   * @throws IllegalArgumentException if the calendar name doesn't exist.
   */
  void useCalendar(String name) throws IllegalArgumentException;

  /**
   * Retrieves the currently active calendar.
   *
   * @return The active Calendar instance.
   * @throws IllegalStateException if no calendar is currently in use
   *                               (i.e., 'use calendar' has not been called).
   */
  Calendar getActiveCalendar() throws IllegalStateException;

  /**
   * Copies a specific event from the active calendar to a target calendar.
   *
   * @param subject            The subject of the event to copy.
   * @param start              The start time of the event to copy (in active calendar's timezone).
   * @param targetCalendarName The name of the calendar to copy to.
   * @param targetStart        The new start time for the copied event
   *                           (in target calendar's timezone).
   * @throws IllegalArgumentException if the event isn't found, the target calendar
   *                                  doesn't exist, or the copy creates a conflict.
   */
  void copyEvent(String subject, LocalDateTime start, String targetCalendarName,
                 LocalDateTime targetStart) throws IllegalArgumentException;

  /**
   * Copies all events on a specific date from the active calendar to a target calendar.
   *
   * @param date               The date to copy events from (in active calendar's timezone).
   * @param targetCalendarName The name of the calendar to copy to.
   * @param targetDate         The corresponding start date for the events in the target calendar.
   * @throws IllegalArgumentException if calendars aren't found or a conflict occurs.
   */
  void copyEventsOnDate(LocalDate date, String targetCalendarName, LocalDate targetDate)
      throws IllegalArgumentException;

  /**
   * Copies all events within a date range from the active calendar to a target calendar.
   *
   * @param startDate          The start of the range (inclusive, in active calendar's timezone).
   * @param endDate            The end of the range (inclusive, in active calendar's timezone).
   * @param targetCalendarName The name of the calendar to copy to.
   * @param targetStartDate    The date in the target calendar that corresponds to the
   *                           startDate of the source range.
   * @throws IllegalArgumentException if calendars aren't found or a conflict occurs.
   */
  void copyEventsBetween(LocalDate startDate, LocalDate endDate, String targetCalendarName,
                         LocalDate targetStartDate) throws IllegalArgumentException;
}