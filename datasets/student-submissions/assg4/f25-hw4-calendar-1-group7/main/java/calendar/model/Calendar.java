package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a calendar that manages events.
 */
public interface Calendar {

  /**
   * Adds an event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if a duplicate event exists
   */
  void addEvent(Event event);

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   * @return true if removed, false if not found
   */
  boolean removeEvent(Event event);

  /**
   * Gets all events scheduled on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Gets all events that occur within a date/time range.
   *
   * @param start the start of the range
   * @param end the end of the range
   * @return list of events in the range
   */
  List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Finds a specific event by subject, start, and end time.
   *
   * @param subject the event subject
   * @param start the start time
   * @param end the end time
   * @return the event if found, null otherwise
   */
  Event findEvent(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Finds all events matching subject and start time.
   *
   * @param subject the event subject
   * @param start the start time
   * @return list of matching events
   */
  List<Event> findEvents(String subject, LocalDateTime start);

  /**
   * Checks if the calendar has any events at a specific date/time.
   *
   * @param dateTime the date/time to check
   * @return true if busy, false if available
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Updates an existing event in the calendar.
   *
   * @param event the event to update
   */
  void updateEvent(Event event);

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events
   */
  List<Event> getAllEvents();

  /**
   * Checks if an event would be a duplicate.
   *
   * @param event the event to check
   * @return true if duplicate exists
   */
  boolean isDuplicate(Event event);

  /**
   * Gets all events belonging to a specific series.
   *
   * @param seriesId the series ID
   * @return list of events in the series
   */
  List<Event> getEventsBySeries(String seriesId);
}