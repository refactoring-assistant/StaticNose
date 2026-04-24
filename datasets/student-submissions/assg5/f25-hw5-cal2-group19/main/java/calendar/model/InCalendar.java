package calendar.model;

import calendar.exception.DuplicateEventException;
import calendar.exception.EventNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a calendar that manages events.
 * Provides operations for adding, removing, querying, and exporting events.
 */
public interface InCalendar {

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  String getCalendarName();

  /**
   * Sets the name of this calendar.
   * supports calendar renaming.
   *
   * @param newName the new calendar name
   */
  void setCalendarName(String newName);

  /**
   * Adds an event to the calendar.
   *
   * @param event the event to add
   * @throws DuplicateEventException if an event with same subject/start/end exists
   */
  void addEvent(InEvent event) throws DuplicateEventException;

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   * @throws EventNotFoundException if the event does not exist
   */
  void removeEvent(InEvent event) throws EventNotFoundException;

  /**
   * Gets all events occurring on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<InEvent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events occurring between two date/times.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return list of events in the range
   */
  List<InEvent> getEventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific date/time.
   *
   * @param dateTime the date/time to check
   * @return true if busy, false if available
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events
   */
  List<InEvent> getAllEvents();

  /**
   * Finds a specific event by subject and start/end times.
   *
   * @param subject the event subject
   * @param start   the start date/time
   * @param end     the end date/time
   * @return the matching event, or null if not found
   */
  InEvent findEvent(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Filters the events in the calendar based on a given condition.
   *
   * @param predicate The condition to test each event against.
   * @return A new list containing only the events that match the predicate.
   */
  List<InEvent> filterEvents(Predicate<InEvent> predicate);
}