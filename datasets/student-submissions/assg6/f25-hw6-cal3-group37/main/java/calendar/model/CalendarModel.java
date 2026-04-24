package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Represents a calendar that can store and manage events.
 * Supports single events and recurring event series.
 */
public interface CalendarModel {

  /**
   * Adds a single event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event conflicts with existing event
   */
  void addEvent(CalendarEvent event);

  /**
   * Creates and adds a recurring event series.
   *
   * @param subject the event subject
   * @param startDateTime the start date and time of first occurrence
   * @param endDateTime the end date and time of first occurrence
   * @param daysOfWeek the days of week to repeat on
   * @param occurrences the number of occurrences (null for date-based end)
   * @param untilDate the end date for recurrence (null for count-based end)
   * @return the series ID
   * @throws IllegalArgumentException if parameters are invalid or conflicts exist
   */
  String addEventSeries(String subject, LocalDateTime startDateTime,
                        LocalDateTime endDateTime, Set<DayOfWeek> daysOfWeek,
                        Integer occurrences, LocalDate untilDate);

  /**
   * Edits a single event matching the criteria.
   *
   * @param subject the subject to search for
   * @param startDateTime the start datetime to search for
   * @param property the property to edit
   * @param newValue the new value
   * @throws IllegalArgumentException if event not found or edit is invalid
   */
  void editEvent(String subject, LocalDateTime startDateTime,
                 String property, String newValue);

  /**
   * Edits all events in a series starting from a specific event.
   *
   * @param subject the subject to search for
   * @param startDateTime the start datetime of the event to start from
   * @param property the property to edit
   * @param newValue the new value
   * @throws IllegalArgumentException if event not found or edit is invalid
   */
  void editEventsFromDate(String subject, LocalDateTime startDateTime,
                          String property, String newValue);

  /**
   * Edits all events in a series.
   *
   * @param subject the subject to search for
   * @param startDateTime the start datetime of any event in the series
   * @param property the property to edit
   * @param newValue the new value
   * @throws IllegalArgumentException if event not found or edit is invalid
   */
  void editEntireSeries(String subject, LocalDateTime startDateTime,
                        String property, String newValue);

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<CalendarEvent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events in a date range.
   *
   * @param startDateTime the start of the range
   * @param endDateTime the end of the range
   * @return list of events in the range
   */
  List<CalendarEvent> getEventsInRange(LocalDateTime startDateTime, LocalDateTime endDateTime);

  /**
   * Checks if user is busy at a specific datetime.
   *
   * @param dateTime the datetime to check
   * @return true if busy, false if available
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events
   */
  List<CalendarEvent> getAllEvents();
}