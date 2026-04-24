package calendar.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * A calendar that stores events and recurring events. It lets you add, remove, search,
 * and check for busy times.
 */
public interface CalendarInterface {

  /**
   * Gets the name of this calendar. Required for multi-calendar support and export.
   *
   * @return the calendar's name (never null or empty).
   */
  String getName();

  /**
   * Sets the name of this calendar. Names must be unique across all calendars.
   *
   * @param name the new calendar name (cannot be null or empty).
   */
  void setName(String name);

  /**
   * Adds a single event to the calendar. If another event has the same subject, start time,
   * and end time, it throws a ConflictException error.
   *
   * @param event the event to add (must not be null).
   * @throws calendar.util.ConflictException if the event conflicts with an existing event.
   */
  void addEvent(EventInterface event) throws calendar.util.ConflictException;

  /**
   * Adds a recurring event to the calendar. It creates all the individual events in the series
   * and adds them one by one. If any event in the series conflicts with an existing event,
   * nothing is added and a ConflictException error is thrown.
   *
   * @param recurringEvent the series to add (must not be null).
   * @throws calendar.util.ConflictException if any event in the series conflicts.
   */
  void addRecurringEvent(RecurringEventInterface recurringEvent)
      throws calendar.util.ConflictException;

  /**
   * Removes a single event from the calendar. Does nothing if the event isn't found.
   *
   * @param event the event to be removed.
   * @return boolean value.
   */
  boolean removeEvent(EventInterface event);

  /**
   * Removes a recurring event series from the calendar. This removes all individual events
   * that belong to this series.
   *
   * @param recurringEvent the series to be removed.
   */
  void removeRecurringEvent(RecurringEventInterface recurringEvent);

  /**
   * Returns all events that happen on a specific date. Includes events that start/end on that day,
   * even if they span over multiple days.
   *
   * @param date the date to be checked.
   * @return a list of events on that day (never null, may be empty).
   */
  List<EventInterface> getEventsOn(ZonedDateTime date);

  /**
   * Returns all events that overlap within a time range. An event is included if any part of it
   * falls between start and end.
   *
   * @param start the start of the time range.
   * @param end the end of the time range.
   * @return a list of events in the range (never null, may be empty).
   */
  List<EventInterface> getEventsBetween(ZonedDateTime start, ZonedDateTime end);

  /**
   * Checks if is busy at a specific date and time. Returns true if any event is happening
   * at that exact moment.
   *
   * @param dateTime the exact time to be checked.
   * @return true if there's an event at that time, false if available.
   */
  boolean isBusy(ZonedDateTime dateTime);

  /**
   * Finds a single event by its subject, start time, and end time. Used when an event is to
   * be uniquely identified.
   *
   * @param subject the event's subject (should be the exact same title).
   * @param start the event's start time.
   * @param end the event's end time (can be null for all-day events).
   * @return the matching event, or null if not found.
   */
  EventInterface getEvent(String subject, ZonedDateTime start, ZonedDateTime end);

  /**
   * Finds a recurring event by the subject and start time of one of its instances. Identifies
   * the whole series, not just one event in it.
   *
   * @param subject the series subject (should be the exact same title).
   * @param start the start time of any one event in the series.
   * @return the matching recurring event, or null if not found.
   */
  RecurringEventInterface getRecurringEvent(String subject, ZonedDateTime start);

  /**
   * Gets the timezone associated with this calendar. All events in this calendar
   * use this timezone by default.
   *
   * @return the calendar's timezone (never null).
   */
  ZoneId getCalendarZone();

  /**
   * Updates the timezone of this calendar.
   *
   * @param zone the new timezone (cannot be null).
   */
  void setCalendarZone(ZoneId zone);

  /**
   * Edit a single event (ignores series rules).
   */
  void editEvent(EventInterface target, EditableField field, Object newValue)
      throws calendar.util.ConflictException;

  /**
   * Edit all events in the series starting from a specific event.
   */
  void editEventsFrom(EventInterface target, EditableField field, Object newValue)
      throws calendar.util.ConflictException;

  /**
   * Edit all events in the series.
   */
  void editEntireSeries(EventInterface target, EditableField field, Object newValue)
      throws calendar.util.ConflictException;

  /**
   * Returns all events stored in this calendar, including single and recurring events.
   *
   * @return list of all events in the calendar (never null, may be empty).
   */
  List<EventInterface> getAllCalendarEvents();
}