package calendar.model.calendar;

import calendar.exceptions.InvalidDateTimeException;
import calendar.model.event.EventInterface;
import java.time.ZoneId;
import java.util.List;

/**
 * Read-only interface for querying calendar data.
 * Provides access to calendar events and status without allowing modifications.
 * Used by components like views and exporters that need to read calendar data
 * but should not have the ability to mutate it.
 */
public interface ReadOnlyCalendar {

  /**
   * Checks if the calendar has any events scheduled at the specified date and time.
   * Returns true if there is at least one event occurring at the given moment,
   * false if the calendar is available (no events) at that time.
   *
   * @param dateTimes the date and time to check in format YYYY-MM-DDThh:mm
   * @return true if there are events at the specified time (busy), false if available
   * @throws InvalidDateTimeException if the datetime format is invalid or cannot be parsed
   */
  boolean busyStatus(String dateTimes) throws InvalidDateTimeException;

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  String getCalendarName();

  /**
   * Returns the timezone of this calendar.
   * All events in this calendar are interpreted in this timezone context.
   *
   * @return the timezone of this calendar
   */
  ZoneId getCalendarTimeZone();

  /**
   * Retrieves all events scheduled on a specific date.
   *
   * @param date the date to query in format YYYY-MM-DD
   * @return list of events on that date, empty list if no events
   * @throws IllegalArgumentException if date format is invalid
   */
  List<EventInterface> getEvents(String date) throws InvalidDateTimeException;

  /**
   * Retrieves all events that occur within a date range (inclusive).
   * Events that partially or fully overlap the range are included.
   *
   * @param startdateTime the start date in format YYYY-MM-DDThh:mm (inclusive)
   * @param enddateTime   the end date in format YYYY-MM-DDThh:mm (inclusive)
   * @return list of events in the range with duplicates removed, empty list if no events
   * @throws IllegalArgumentException if date format is invalid
   */
  List<EventInterface> getEvents(String startdateTime, String enddateTime)
      throws InvalidDateTimeException;

  /**
   * Retrieves all the events from the entire calendar and returns a list.
   *
   * @return list of all events from the calendar
   * @throws InvalidDateTimeException if date format is invalid
   */
  List<EventInterface> getAllEvents() throws InvalidDateTimeException;
}
