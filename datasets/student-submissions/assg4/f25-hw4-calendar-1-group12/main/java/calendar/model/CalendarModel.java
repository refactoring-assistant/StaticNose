package calendar.model;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines all the operations that can be performed on a calendar.
 */
public interface CalendarModel {

  /**
   * Add a single event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event already exists
   */
  void addEvent(Event event) throws IllegalArgumentException;

  /**
   * Add an event series to the calendar. This generates all events
   * in the series and adds them to the calendar.
   *
   * @param eventSeries the event series to add
   * @throws IllegalArgumentException if any event in series already exists
   */
  void addEventSeries(EventSeries eventSeries) throws IllegalArgumentException;

  /**
   * Remove an event from the calendar.
   *
   * @param event the event to remove
   */
  void removeEvent(Event event);

  /**
   * Edit an event or a single event in a series.
   *
   * @param subject  the subject of the event to edit
   * @param start    start date and time of the event to edit
   * @param end      end date and time of the event to edit
   * @param property the property to be modified
   * @param value    the new value to be set
   * @throws IllegalArgumentException if event is not found or edit creates duplicate
   */
  void editEvent(String subject, LocalDateTime start, LocalDateTime end, String property,
                       Object value) throws IllegalArgumentException;

  /**
   * Edits events from a particular point onwards in a series.
   * If event is part of series, edits this event and all future events.
   * If not part of this series, same as editOneEvent.
   *
   * @param subject  the subject of the event to edit
   * @param start    start date and time of the event to edit
   * @param property the property to be modified
   * @param value    the new value to be set
   * @throws IllegalArgumentException if event is not found or edit creates duplicate
   */
  void editEventsFrom(String subject, LocalDateTime start, String property, Object value)
      throws IllegalArgumentException;

  /**
   * Edits all events in the series.
   *
   * @param subject  the subject of the event to edit
   * @param start    start date and time of the event to edit
   * @param property the property to be modified
   * @param value    the new value to be set
   * @throws IllegalArgumentException if eventseries is not found or edit creates duplicate event
   */
  void editAllEventsInSeries(String subject, LocalDateTime start, String property, Object value)
      throws IllegalArgumentException;

  /**
   * Gets all events occurring on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date, sorted by start time
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Gets all events in a time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return list of events in that range, sorted by start time
   */
  List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Check if user is busy at a specific time.
   *
   * @param dateTime the date/time to check
   * @return true if busy, false if available
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Exports all calendar events to csv format.
   *
   * @throws IOException if file cannot be written
   */
  String exportToCsv(String fileName) throws IOException;
}
