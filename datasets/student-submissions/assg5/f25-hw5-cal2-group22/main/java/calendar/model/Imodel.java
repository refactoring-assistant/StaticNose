package calendar.model;

import calendar.control.editmodes.EditMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * Interface defining core operations for a calendar system.
 * Supports creating, editing, querying, and exporting events.
 * Implementations define actual behavior.
 */

public interface Imodel {


  /**
   * Creates a new event with the given details.
   *
   * @param subject event title
   * @param start   start date and time
   * @param end     end date and time
   * @return true if created successfully, false if a conflict exists
   */
  boolean createEvent(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Creates an all-day event (8 AM–5 PM) on a given date.
   *
   * @param subject event title
   * @param date    event date
   * @return true if created successfully
   */
  boolean createAllDayEvent(String subject, LocalDate date);


  /**
   * Create a recurring event series that repeats on selected weekdays
   * for a fixed number of occurrences.
   *
   * @param subject   - subject of the event series
   * @param startDate - start date of the series
   * @param startTime - start time of the event
   * @param endTime   - end time of the event
   * @param days      - MTWRFSU - days of the week
   * @param count     - the number of occurrences
   * @return - true if created successfully
   */
  boolean createEventSeries(String subject, LocalDate startDate, LocalTime startTime,
                            int count, LocalTime endTime, Set<DayOfWeek> days);

  /**
   * Create a recurring event series that repeats on selected weekdays
   * for a fixed number of occurrences.
   *
   * @param subject   - subject of the event series
   * @param startDate - start date of the series
   * @param startTime - start time of the events
   * @param endTime   - end time of the events
   * @param days      - MTWRFSU - days of the week
   * @param endDate   - end date of the events
   * @return - true if created successfully
   */
  boolean createEventSeriesUntil(String subject, LocalDate startDate, LocalTime startTime,
                                 LocalDate endDate, LocalTime endTime, Set<DayOfWeek> days);

  /**
   * Edits a property of an existing event.
   *
   * @param subject  title of the event
   * @param start    start time used to identify the event
   * @param property property name (subject, start, end, etc.)
   * @param newValue new value for the property
   * @param mode     edit mode (single event, forward series, or entire series)
   * @return true if edited successfully
   */
  boolean editEvent(String subject, LocalDateTime start, LocalDateTime end,
                    String property, String newValue, EditMode mode);

  /**
   * Retrieves all events on a specific date.
   *
   * @param date the date to check
   * @return list of event descriptions
   */
  List<String> getEventsOn(LocalDate date);

  /**
   * Retrieves all events within a given date-time range.
   *
   * @param from start of the range
   * @param to   end of the range
   * @return list of event descriptions
   */
  List<String> getEventsBetween(LocalDateTime from, LocalDateTime to);

  /**
   * Retrieves all the events of a calendar.
   *
   * @return all events of a calendar
   */
  List<AbstractEvent> getAllEvents();

  /**
   * Gets the timezone of this calendar.
   *
   * @return the ZoneId of this calendar
   */
  ZoneId getTimeZone();

  /**
   * Checks if the user is busy at a specific time.
   *
   * @param dateTime the time to check
   * @return true if busy, false otherwise
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Changes the calendar's timezone and converts all existing events to the new timezone.
   * The absolute moment in time for each event is preserved, but local times are adjusted.
   *
   * @param newTimeZone the new timezone for this calendar
   * @throws IllegalArgumentException if newTimeZone is null
   */
  void changeTimeZone(ZoneId newTimeZone);

}
