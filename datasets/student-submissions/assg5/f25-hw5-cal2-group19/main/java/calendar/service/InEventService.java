package calendar.service;

import calendar.exception.CalendarException;
import calendar.exception.DuplicateEventException;
import calendar.exception.EventNotFoundException;
import calendar.model.InEvent;
import calendar.model.Weekday;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service layer for event business operations.
 * Encapsulates complex event logic and validation.
 */
public interface InEventService {

  /**
   * Creates a single event in the calendar.
   *
   * @param subject       the event subject
   * @param start         the start date/time
   * @param end           the end date/time (null for all-day)
   * @param optionalProps map of optional properties
   * @throws DuplicateEventException if duplicate event exists
   */
  void createSingleEvent(String subject, LocalDateTime start,
                         LocalDateTime end, Map<String, String> optionalProps)
      throws DuplicateEventException;

  /**
   * Creates an event series with occurrence count.
   *
   * @param subject       the event subject
   * @param start         the start date/time of first occurrence
   * @param end           the end date/time (null for all-day)
   * @param weekdays      set of weekdays to repeat on
   * @param occurrences   number of times to repeat
   * @param optionalProps map of optional properties
   * @throws DuplicateEventException if any duplicate occurs
   */
  void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                         Set<Weekday> weekdays, int occurrences,
                         Map<String, String> optionalProps)
      throws DuplicateEventException, CalendarException;

  /**
   * Creates an event series until a specific date.
   *
   * @param subject       the event subject
   * @param start         the start date/time of first occurrence
   * @param end           the end date/time (null for all-day)
   * @param weekdays      set of weekdays to repeat on
   * @param endDate       the last date to repeat until (inclusive)
   * @param optionalProps map of optional properties
   * @throws DuplicateEventException if any duplicate occurs
   */
  void createEventSeriesUntil(String subject, LocalDateTime start, LocalDateTime end,
                              Set<Weekday> weekdays, LocalDate endDate,
                              Map<String, String> optionalProps)
      throws DuplicateEventException, CalendarException;

  /**
   * Edits a single event instance.
   *
   * @param subject  the event subject to find
   * @param start    the start date/time to identify event
   * @param property the property to edit
   * @param newValue the new property value
   * @throws EventNotFoundException if event not found
   * @throws CalendarException      if edit would create duplicate
   */
  void editSingleEvent(String subject, LocalDateTime start,
                       String property, String newValue)
      throws EventNotFoundException, CalendarException;

  /**
   * Edits all events in series starting from a date.
   *
   * @param subject  the event subject to find
   * @param start    the start date/time to identify event
   * @param property the property to edit
   * @param newValue the new property value
   * @throws EventNotFoundException if event not found
   * @throws CalendarException      if edit would create duplicates
   */
  void editSeriesFromDate(String subject, LocalDateTime start,
                          String property, String newValue)
      throws EventNotFoundException, CalendarException;

  /**
   * Edits entire event series.
   *
   * @param subject  the event subject to find
   * @param start    the start date/time to identify event
   * @param property the property to edit
   * @param newValue the new property value
   * @throws EventNotFoundException if event not found
   * @throws CalendarException      if edit would create duplicates
   */
  void editEntireSeries(String subject, LocalDateTime start,
                        String property, String newValue)
      throws EventNotFoundException, CalendarException;

  /**
   * Queries events on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<InEvent> queryEventsOnDate(LocalDate date);

  /**
   * Queries events between two date/times.
   *
   * @param start the start of range
   * @param end   the end of range
   * @return list of events in range
   */
  List<InEvent> queryEventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if user is busy at a specific date/time.
   *
   * @param dateTime the date/time to check
   * @return true if busy, false if available
   */
  boolean checkBusyStatus(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events
   */
  List<InEvent> getAllEvents();
}