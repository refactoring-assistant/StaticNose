package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for a calendar that manages events.
 * Represents the core model operations for a calendar application.
 * This interface follows the Interface Segregation Principle by providing
 * only essential calendar operations.
 */
public interface Icalendar {

  /**
   * Creates a single event in the calendar.
   *
   * @param subject the subject/title of the event
   * @param start   the start date and time
   * @param end     the end date and time (null for all-day events)
   * @throws IllegalArgumentException if event with same subject, start, and end already exists
   * @throws IllegalArgumentException if subject is null or empty
   * @throws IllegalArgumentException if start is null
   */
  void createEvent(String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Creates a recurring event series with a specified number of occurrences.
   *
   * @param subject     the subject/title of the event
   * @param start       the start date and time of the first occurrence
   * @param end         the end date and time of each occurrence
   * @param weekdays    string representing days (e.g., "MWF" for Mon/Wed/Fri)
   * @param occurrences number of times the event should repeat
   * @throws IllegalArgumentException if parameters are invalid
   * @throws IllegalArgumentException if event spans multiple days
   */
  void createEventSeries(String subject, LocalDateTime start, LocalDateTime end,
                         String weekdays, int occurrences);

  /**
   * Creates a recurring event series until a specific end date.
   *
   * @param subject  the subject/title of the event
   * @param start    the start date and time of the first occurrence
   * @param end      the end date and time of each occurrence
   * @param weekdays string representing days (e.g., "MWF")
   * @param until    the last date to create events (inclusive)
   * @throws IllegalArgumentException if parameters are invalid
   * @throws IllegalArgumentException if event spans multiple days
   */
  void createEventSeriesUntil(String subject, LocalDateTime start, LocalDateTime end,
                              String weekdays, LocalDate until);

  /**
   * Edits a single event's property.
   *
   * @param subject  the subject to search for
   * @param start    the start time to search for
   * @param property the property to edit (subject, start, end, description, location, status)
   * @param newValue the new value for the property
   * @throws IllegalArgumentException if event not found or not unique
   * @throws IllegalArgumentException if edit would create duplicate event
   */
  void editEvent(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits all events in a series starting from a specific event.
   *
   * @param subject  the subject to search for
   * @param start    the start time to search for
   * @param property the property to edit
   * @param newValue the new value for the property
   * @throws IllegalArgumentException if event not found or not unique
   * @throws IllegalArgumentException if edit would create duplicate events
   */
  void editEventsFrom(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits all events in a series.
   *
   * @param subject  the subject to search for
   * @param start    the start time to search for
   * @param property the property to edit
   * @param newValue the new value for the property
   * @throws IllegalArgumentException if event not found or not unique
   * @throws IllegalArgumentException if edit would create duplicate events
   */
  void editSeries(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date (empty list if none)
   */
  List<Ievent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events within a date-time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return list of events in the range (empty list if none)
   */
  List<Ievent> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return true if any event is scheduled at that time, false otherwise
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Exports the calendar to CSV format compatible with Google Calendar.
   *
   * @return CSV string representation of all events
   */
  String exportToCsv();

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events
   */
  List<Ievent> getAllEvents();
}