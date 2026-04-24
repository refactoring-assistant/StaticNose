package calendar.model;

import calendar.model.event.CalendarEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface defining the contract for calendar model operations.
 * Provides methods for creating, editing, and querying calendar events
 * including support for single events, event series, and all-day events.
 */
public interface CalendarModelInterface {

  /**
   * Creates a single calendar event.
   *
   * @param parameters map containing required keys: "subject", "start", "end"
   *                   and optional keys: "description", "location", "status"
   * @throws IllegalArgumentException if parameters are invalid or event already exists
   */
  void createSingleEvent(Map<String, Object> parameters);

  /**
   * Creates an event series that repeats on specific days of the week.
   *
   * @param parameters map containing required keys: "subject", "start", "end", "weekdays"
   *                   and either "ndays" (number of occurrences) or "untildate" (end date)
   * @throws IllegalArgumentException if parameters are invalid or would create duplicates
   */
  void createEventSeries(Map<String, Object> parameters);

  /**
   * Creates a single all-day event (8am to 5pm).
   *
   * @param parameters map containing required key: "ondate"
   *                   and optional keys: "description", "location", "status"
   * @throws IllegalArgumentException if parameters are invalid
   */
  void createAllDayEvent(Map<String, Object> parameters);

  /**
   * Creates an all-day event series that repeats on specific days.
   *
   * @param parameters map containing required keys: "ondate", "weekdays"
   *                   and either "ndays" or "untildate"
   * @throws IllegalArgumentException if parameters are invalid
   */
  void createAllDayEventSeries(Map<String, Object> parameters);

  /**
   * Edits a single event instance.
   * If the event is part of a series and the start time is changed,
   * the event is removed from the series.
   *
   * @param parameters map containing: "subject", "start", "end", "property", "value"
   * @throws IllegalArgumentException if event not found, not unique, or edit creates duplicate
   */
  void editSingleEvent(Map<String, Object> parameters);

  /**
   * Edits all events in a series.
   * If the target event is not part of a series, behaves like editSingleEvent.
   *
   * @param parameters map containing: "subject", "start", "property", "value"
   * @throws IllegalArgumentException if event not found, not unique, or edit creates duplicate
   */
  void editSeries(Map<String, Object> parameters);

  /**
   * Edits all events in a series starting from a specific date.
   * If editing the start property, splits the series into two separate series.
   * If not part of a series, behaves like editSingleEvent.
   *
   * @param parameters map containing: "subject", "start", "property", "value"
   * @throws IllegalArgumentException if event not found, not unique, or edit creates duplicate
   */
  void editEvents(Map<String, Object> parameters);

  /**
   * Retrieves all events in the calendar.
   *
   * @return list of all calendar events in chronological order
   */
  List<CalendarEvent> getAllEvents();

  /**
   * Retrieves all events scheduled on a specific date.
   *
   * @param date the date to query
   * @return list of events occurring on the specified date
   */
  List<CalendarEvent> getEventsOn(LocalDate date);

  /**
   * Retrieves all events within a specified time range.
   *
   * @param startDateTime the start of the range (inclusive)
   * @param endDateTime   the end of the range (exclusive)
   * @return list of events that overlap with the specified range
   */
  List<CalendarEvent> getEventsInRange(ZonedDateTime startDateTime, ZonedDateTime endDateTime);

  /**
   * Checks if the user is available at a specific date and time.
   *
   * @param dateTime the date-time to check
   * @return true if available (no events scheduled), false if busy
   */
  boolean checkAvailability(ZonedDateTime dateTime);

  /**
   * Gets the timezone of this calendar.
   *
   * @return the calendar's timezone.
   */
  ZoneId getTimezone();

  /**
   * Creates a new CalendarModel with all events converted to the specified timezone.
   * Single events are recreated individually, while series events are processed once
   * per series to maintain their relationships.
   *
   * @param timeZone the new timezone for the calendar
   * @return a new CalendarModel with all events in the specified timezone
   * @throws IllegalArgumentException if timezone conversion would create multi-day series events
   */
  CalendarModelInterface updateTimezone(ZoneId timeZone);

  /**
   * Copies a single event to a target calendar at a specified time.
   * The event's duration is preserved while the start time is changed.
   *
   * @param target     the calendar to copy the event to
   * @param parameters map containing "subject", "start", and "targetstart"
   * @throws IllegalArgumentException if the event is not found or not unique
   */
  void copyEvent(CalendarModelInterface target, Map<String, Object> parameters);

  /**
   * Copies all events from a source date to a target date.
   * Preserves the time-of-day for each event while changing the date.
   * Series relationships are maintained in the target calendar.
   *
   * @param target     the calendar to copy events to
   * @param parameters map containing "sourcedate" and "targetdate"
   * @throws IllegalArgumentException if copying creates multi-day series events
   *                                  or duplicate events in the target calendar
   */
  void copyEventsOn(CalendarModelInterface target, Map<String, Object> parameters);

  /**
   * Copies all events within a date range to a new starting date.
   * Events are shifted by the day offset between source and target dates.
   * Series relationships are maintained in the target calendar.
   *
   * @param target     the calendar to copy events to
   * @param parameters map containing "startdate", "enddate", and "targetdate"
   * @throws IllegalArgumentException if copying creates multi-day series events
   *                                  or duplicate events in the target calendar
   */
  void copyEventsBetween(CalendarModelInterface target, Map<String, Object> parameters);


}