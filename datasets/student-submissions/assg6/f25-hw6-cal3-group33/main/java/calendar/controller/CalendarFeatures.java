package calendar.controller;

import calendar.exceptions.NoCalendarInUseException;
import calendar.model.calendar.EditScope;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface defining high-level user actions for calendar management.
 * This interface represents the features available to users through the GUI,
 * decoupling the view from the controller implementation.
 */
public interface CalendarFeatures {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param calendarName the name for the new calendar
   * @param timezone the timezone in IANA format (e.g., "America/New_York")
   */
  void createCalendar(String calendarName, String timezone);

  /**
   * Edits calendar properties.
   * Edits timezone first (if changed), then name (if changed) for proper model lookup.
   *
   * @param originalName the current calendar name
   * @param newName the new calendar name (same as originalName if not changing)
   * @param newTimezone the new timezone
   */
  void editCalendar(String originalName, String newName, String newTimezone);

  /**
   * Selects a calendar to be the currently active calendar.
   *
   * @param calendarName the name of the calendar to select
   */
  void selectCalendar(String calendarName);

  /**
   * Selects a specific day and displays all events scheduled on that day.
   *
   * @param date the date to select
   */
  void selectDay(LocalDate date);

  /**
   * Exports the current calendar to a file in the specified format.
   * File is saved in the project directory with auto-generated name.
   *
   * @param format the export format ("ical" or "csv")
   */
  void exportCalendar(String format);

  /**
   * Creates a single timed event with optional details.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   * @param location the location (optional, can be null)
   * @param description the description (optional, can be null)
   * @param status the status (optional, can be null)
   */
  void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                         String location, String description, String status);

  /**
   * Creates a recurring timed event with optional details.
   * Must specify either count or untilDate, but not both.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   * @param weekdays the weekdays to repeat on (e.g., "MWF")
   * @param count the number of occurrences (null if using untilDate)
   * @param untilDate the end date for recurrence (null if using count)
   * @param location the location (optional, can be null)
   * @param description the description (optional, can be null)
   * @param status the status (optional, can be null)
   */
  void createRecurringEvent(String subject, LocalDateTime start, LocalDateTime end,
                            String weekdays, Integer count, String untilDate,
                            String location, String description, String status);

  /**
   * Edits an existing event with new values.
   * Only properties that differ from original values will be updated.
   *
   * @param originalSubject the current subject of the event
   * @param originalStart the current start date and time
   * @param originalEnd the current end date and time
   * @param newSubject the new subject
   * @param newStart the new start date and time
   * @param newEnd the new end date and time
   * @param newLocation the new location
   * @param newDescription the new description
   * @param newStatus the new status
   * @param scope the edit scope (SINGLE, FROM_POINT, or ENTIRE_SERIES)
   */
  void editEvent(String originalSubject, LocalDateTime originalStart, LocalDateTime originalEnd,
                 String newSubject, LocalDateTime newStart, LocalDateTime newEnd,
                 String newLocation, String newDescription, String newStatus,
                 EditScope scope);

  /**
   * Requests the controller to refresh event indicators for the current month.
   * Called after calendar selection changes or events are created/edited.
   */
  void refreshCurrentMonth();

  /**
   * Gets all calendar names from the manager.
   *
   * @return list of all calendar names
   */
  List<String> getAllCalendarNames();

  /**
   * Gets the currently selected calendar name.
   *
   * @return current calendar name
   */
  String getCurrentCalendarName();

  /**
   * Gets the timezone of the current calendar.
   *
   * @return timezone ID string
   */
  String getCurrentCalendarTimezone() throws NoCalendarInUseException;
}

