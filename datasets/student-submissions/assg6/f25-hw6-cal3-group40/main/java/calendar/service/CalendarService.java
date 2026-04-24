package calendar.service;

import calendar.model.CalendarInterface;
import calendar.model.exceptions.ConflictException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Represents the main service layer for managing multiple calendars.
 * It extends EventService to also provide all event-related operations
 * for the currently active calendar.
 */
public interface CalendarService extends EventService {

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name     The unique name for the calendar.
   * @param timezone The timezone ID (e.g., "America/New_York").
   * @throws IllegalArgumentException if a calendar with that name already exists.
   */
  void createCalendar(String name, String timezone) throws IllegalArgumentException,
      DateTimeException;

  /**
   * Sets the active calendar for subsequent operations.
   *
   * @param name The name of the calendar to use.
   * @throws IllegalArgumentException if the calendar doesn't exist.
   */
  void useCalendar(String name) throws IllegalArgumentException;

  /**
   * Gets the name of the currently active calendar.
   *
   * @return The active calendar name, or null if none is set.
   */
  String getCurrentCalendarName();

  /**
   * Gets the timezone of the currently active calendar.
   *
   * @return The timezone, or null if no calendar is active.
   */
  ZoneId getCurrentCalendarTimezone();

  /**
   * Edits the name of a calendar.
   *
   * @param oldName The current name.
   * @param newName The new name.
   * @throws IllegalArgumentException if the calendar doesn't exist or new name is taken.
   */
  void editCalendarName(String oldName, String newName) throws IllegalArgumentException;

  /**
   * Edits the timezone of a calendar.
   *
   * @param calendarName The name of the calendar.
   * @param newTimezone  The new timezone ID.
   * @throws IllegalArgumentException if the calendar doesn't exist or timezone is invalid.
   */
  void editCalendarTimezone(String calendarName, String newTimezone)
      throws IllegalArgumentException, DateTimeException;

  /**
   * Lists all available calendars.
   *
   * @return A list of all calendars.
   */
  List<CalendarInterface> getAllCalendars();

  /**
   * Copies a single event from the active calendar to a target calendar
   * at a new, specified start time.
   *
   * @param eventName      The subject of the event to copy.
   * @param eventStart     The original start time (in active calendar's timezone)
   * @param targetCalName  The name of the destination calendar.
   * @param newTargetStart The new start time (in the *target* calendar's timezone)
   * @throws IllegalArgumentException if calendars/events aren't found.
   * @throws ConflictException      if the new event conflicts in the target.
   */
  void copyEvent(String eventName, LocalDateTime eventStart,
                 String targetCalName, LocalDateTime newTargetStart)
      throws IllegalArgumentException, ConflictException;

  /**
   * Copies all events on a specific date from the active calendar
   * to a new date in a target calendar, converting timezones.
   *
   * @param date          The source date.
   * @param targetCalName The name of the destination calendar.
   * @param newTargetDate The new date for the copied events.
   * @throws IllegalArgumentException if calendars aren't found.
   */
  void copyEventsOn(LocalDate date, String targetCalName, LocalDate newTargetDate)
      throws IllegalArgumentException;

  /**
   * Copies all events in a date range from the active calendar to a
   * new timeline in a target calendar, preserving relative dates.
   *
   * @param sourceStartDate The start of the source date range (inclusive).
   * @param sourceEndDate   The end of the source date range (inclusive).
   * @param targetCalName   The name of the destination calendar.
   * @param newTimelineDate The date that corresponds to the sourceStartDate.
   * @throws IllegalArgumentException if calendars aren't found.
   */
  void copyEventsBetween(LocalDate sourceStartDate, LocalDate sourceEndDate,
                         String targetCalName, LocalDate newTimelineDate)
      throws IllegalArgumentException;
}