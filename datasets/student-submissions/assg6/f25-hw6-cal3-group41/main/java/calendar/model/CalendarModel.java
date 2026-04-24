package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Interface for calendar operations.
 */
public interface CalendarModel {

  /**
   * Creates a single event with the given subject, start, and end times.
   *
   * @param subject the event subject
   * @param start   the start date and time
   * @param end     the end date and time
   * @throws IllegalArgumentException if a duplicate event exists or validation fails
   */
  void createEvent(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Creates an all-day event on the given date.
   *
   * @param subject the event subject
   * @param date    the date for the all-day event
   * @throws IllegalArgumentException if a duplicate event exists or validation fails
   */
  void createAllDayEvent(String subject, LocalDate date);

  /**
   * Creates a recurring event series.
   *
   * @param subject      the event subject
   * @param start        the start date and time of the first event
   * @param end          the end date and time of the first event
   * @param repeatDays   the days of the week to repeat on
   * @param occurrences  number of occurrences (null if using untilDate)
   * @param untilDate    end date for series (null if using occurrences)
   * @return the number of events created in the series
   * @throws IllegalArgumentException if validation fails or duplicate events would be created
   */
  int createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                       Set<DayOfWeek> repeatDays, Integer occurrences, LocalDate untilDate);

  /**
   * Creates a recurring all-day event series.
   *
   * @param subject      the event subject
   * @param startDate    the start date for the series
   * @param repeatDays   the days of the week to repeat on
   * @param occurrences  number of occurrences (null if using untilDate)
   * @param untilDate    end date for series (null if using occurrences)
   * @return the number of events created in the series
   * @throws IllegalArgumentException if validation fails or duplicate events would be created
   */
  int createAllDayEventSeries(String subject, LocalDate startDate,
                              Set<DayOfWeek> repeatDays, Integer occurrences, LocalDate untilDate);

  /**
   * Edits a single event identified by subject, start, and end.
   *
   * @param subject   the subject of the event to edit
   * @param start     the start time of the event to edit
   * @param end       the end time of the event to edit
   * @param property  the property to edit (subject, start, end, description,
   *                  location, status)
   * @param newValue  the new value for the property
   * @throws IllegalArgumentException if event not found, duplicate would be created,
   *                                  or validation fails
   */
  void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                String property, String newValue);

  /**
   * Edits events in a series starting from the given event forward.
   *
   * @param subject   the subject of the anchor event
   * @param start     the start time of the anchor event
   * @param property  the property to edit
   * @param newValue  the new value for the property
   * @return the number of events updated
   * @throws IllegalArgumentException if event not found, duplicate would be created,
   *                                  or validation fails
   */
  int editEventsFrom(String subject, LocalDateTime start,
                    String property, String newValue);

  /**
   * Edits all events in a series.
   *
   * @param subject   the subject of any event in the series
   * @param start     the start time of any event in the series
   * @param property  the property to edit
   * @param newValue  the new value for the property
   * @return the number of events updated
   * @throws IllegalArgumentException if event not found, duplicate would be created,
   *                                  or validation fails
   */
  int editSeries(String subject, LocalDateTime start,
                String property, String newValue);

  /**
   * Gets all events that occur on the given date.
   *
   * @param date the date to query
   * @return list of events on that date, sorted by start time
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Gets all events that overlap with the given time range.
   *
   * @param start the start of the time range
   * @param end   the end of the time range
   * @return list of events in the range, sorted by start time
   */
  List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at the given time.
   *
   * @param time the time to check
   * @return true if busy, false if available
   */
  boolean isBusyAt(LocalDateTime time);

  /**
   * Exports the calendar to a CSV file.
   *
   * @param filename the filename for the CSV file
   * @return the absolute path of the created file
   * @throws RuntimeException if file I/O fails
   */
  String exportToCsv(String filename);

  /**
   * Exports the calendar to an iCal file.
   *
   * @param filename the filename for the iCal file
   * @param timezone the timezone of the calendar
   * @return the absolute path of the created file
   * @throws RuntimeException if file I/O fails
   */
  String exportToIcal(String filename, java.time.ZoneId timezone);
}
