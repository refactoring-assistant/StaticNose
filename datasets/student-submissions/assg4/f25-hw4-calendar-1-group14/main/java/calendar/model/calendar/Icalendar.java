package calendar.model.calendar;

import calendar.model.event.EventSeries;
import calendar.model.event.Ievent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Interface representing a calendar that manages events.
 * A calendar can contain both single events and event series.
 */
public interface Icalendar {

  /**
   * Adds a single event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if the event is null
   * @throws IllegalStateException    if an event with
   *                                  the same subject, start, and end already exists
   */
  void addEvent(Ievent event) throws IllegalArgumentException, IllegalStateException;

  /**
   * Adds an event series to the calendar.
   *
   * @param series the event series to add
   * @throws IllegalArgumentException if the series is null
   * @throws IllegalStateException    if any event in the series conflicts with existing events
   */
  void addEventSeries(EventSeries series) throws IllegalArgumentException, IllegalStateException;

  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   * @return true if the event was removed, false if it wasn't found
   */
  boolean removeEvent(Ievent event);

  /**
   * Finds an event by its unique properties.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @return an Optional containing the event if found, empty otherwise
   */
  Optional<Ievent> findEvent(String subject, LocalDateTime startDateTime);

  /**
   * Finds all events that match the given subject.
   *
   * @param subject the event subject to search for
   * @return a list of matching events (may be empty)
   */
  List<Ievent> findEventsBySubject(String subject);

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to query
   * @return a list of events on that date, sorted by start time
   */
  List<Ievent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events within a time range.
   *
   * @param startDateTime the start of the range
   * @param endDateTime   the end of the range
   * @return a list of events that overlap with the range, sorted by start time
   */
  List<Ievent> getEventsInRange(LocalDateTime startDateTime, LocalDateTime endDateTime);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if there's an event at that time, false otherwise
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events, sorted by start time
   */
  List<Ievent> getAllEvents();

  /**
   * Gets all event series in the calendar.
   *
   * @return a list of all event series
   */
  List<EventSeries> getAllEventSeries();

  /**
   * Finds the event series that contains a specific event.
   *
   * @param event the event to search for
   * @return an Optional containing the series if found, empty otherwise
   */
  Optional<EventSeries> findSeriesContaining(Ievent event);

  /**
   * Validates if an event can be added without conflicts.
   *
   * @param event the event to validate
   * @return true if the event can be added, false if there's a conflict
   */
  boolean canAddEvent(Ievent event);

  /**
   * Clears all events from the calendar.
   */
  void clear();

  /**
   * Gets the number of events in the calendar.
   *
   * @return the total number of events
   */
  int size();

  /**
   * Updates an event in a series with different modification types.
   * Handles single event updates, updates from a specific event forward,
   * or updates to all events in a series.
   *
   * @param originalEvent the original event to modify
   * @param modifiedEvent the modified version of the event
   * @param modifyType    the type of modification (SINGLE, FROM_THIS, or ALL)
   * @throws IllegalStateException    if the modification would create a conflict
   * @throws IllegalArgumentException if the original event is not found
   */
  void updateEventInSeries(Ievent originalEvent, Ievent modifiedEvent,
                           EventSeries.ModificationType modifyType)
      throws IllegalStateException, IllegalArgumentException;
}