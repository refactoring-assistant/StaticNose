package calendar.controller;

import calendar.model.CalendarInterface;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Defines operations for managing multiple calendars. Supports creating, switching,
 * editing, and copying events across calendars. Maintains proper MVC separation:
 * commands/controllers call this, not the model directly.
 */
public interface CalendarManagerControllerInterface {

  /**
   * Gets the currently active calendar.
   *
   * @return the active calendar.
   * @throws RuntimeException if no calendar is currently active.
   */
  CalendarInterface getActiveCalendar();

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name unique name for the calendar.
   * @param zone timezone for the calendar.
   * @throws RuntimeException if name already exists or zone is null.
   */
  void createCalendar(String name, ZoneId zone);

  /**
   * Deletes a calendar by name.
   *
   * @param name name of the calendar to delete.
   * @throws RuntimeException if calendar does not exist.
   */
  void deleteCalendar(String name);

  /**
   * Updates the name of an existing calendar.
   *
   * @param currentName current name of the calendar.
   * @param newName new name to assign.
   * @throws RuntimeException if calendar doesn't exist or newName already exists.
   */
  void editCalendarName(String currentName, String newName);

  /**
   * Changes the timezone of an existing calendar. All events are adjusted to the new
   * timezone.
   *
   * @param calendarName name of the calendar to update.
   * @param newZone new timezone.
   * @throws RuntimeException if calendar doesn't exist or events would span multiple days.
   */
  void changeCalendarTimezone(String calendarName, ZoneId newZone);

  /**
   * Switches the active calendar context.
   *
   * @param name name of the calendar to activate.
   * @throws RuntimeException if calendar does not exist.
   */
  void switchCalendar(String name);


  /**
   * Lists all existing calendar names.
   *
   * @return collection of all calendar names.
   */
  Collection<String> listCalendars();

  /**
   * Returns the name of the currently active calendar.
   *
   * @return name of the active calendar.
   * @throws RuntimeException if no calendar is currently active.
   */
  String getActiveCalendarName();

  /**
   * Returns the timezone of the currently active calendar. This ensures that all event
   * operations (create/edit) are interpreted in the correct timezone.
   *
   * @return ZoneId of the active calendar.
   * @throws RuntimeException if no calendar is currently active.
   */
  ZoneId getActiveCalendarZone();

  /**
   * Copies a single event from the currently active calendar to a target calendar. The
   * event is identified by its name and start time. The target start time is interpreted
   * in the timezone of the target calendar.
   *
   * @param eventName name of the event to copy.
   * @param sourceStart start time of the event in the source calendar's timezone.
   * @param targetCalendarName name of the calendar to copy the event to.
   * @param targetStart start time in the target calendar's timezone.
   * @throws RuntimeException if event not found or copy fails.
   */
  void copyEvent(String eventName, ZonedDateTime sourceStart, String targetCalendarName,
                 ZonedDateTime targetStart);

  /**
   * Copies all events on a given date from the currently active calendar to a target
   * calendar. The times physically remain the same, but are converted to the target
   * calendar's timezone.
   *
   * @param sourceDate date in the source calendar to copy events from.
   * @param targetCalendarName name of the calendar to copy the events to.
   * @param targetStart start date in the target calendar to place the copied events.
   */
  void copyEventsOn(ZonedDateTime sourceDate, String targetCalendarName, ZonedDateTime targetStart);

  /**
   * Copies all events within the inclusive interval [sourceStart, sourceEnd] from the active
   * calendar to the target calendar, starting at targetStart. Preserves relative timing and
   * recurring series status.
   *
   * @param sourceStart start of the interval in the source calendar (inclusive).
   * @param sourceEnd end of the interval in the source calendar (inclusive).
   * @param targetCalendarName name of the calendar to copy the events to.
   * @param targetStart start date/time in the target calendar to place the copied interval.
   */
  void copyEventsBetween(ZonedDateTime sourceStart, ZonedDateTime sourceEnd,
                         String targetCalendarName, ZonedDateTime targetStart);
}