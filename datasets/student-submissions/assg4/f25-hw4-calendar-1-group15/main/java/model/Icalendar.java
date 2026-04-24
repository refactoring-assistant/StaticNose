package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface defining the contract for a calendar system.
 * All date/time operations assume Eastern Standard Time (EST).
 * This interface provides methods for managing events, including
 * creation, modification, querying, and export functionality.
 */
public interface Icalendar {

  /**
   * Adds a single event to the calendar.
   *
   * @param event the single event to add
   * @throws IllegalArgumentException if the event conflicts with an existing event
   *                                  (same subject, start time, and end time)
   */
  void addSingleEvent(SingleEvent event);

  /**
   * Adds a recurring event series to the calendar.
   * All events in the series are generated and added based on the
   * series configuration.
   *
   * @param series the event series manager containing recurrence configuration
   * @throws IllegalArgumentException if any event in the series conflicts with existing events
   */
  void addEventSeries(EventSeriesManager series);

  /**
   * Retrieves all events occurring on the specified date.
   * This includes both single-day events and multi-day events that
   * span the given date.
   *
   * @param date the date to query
   * @return a list of events on the specified date (never null, may be empty)
   */
  List<Ievent> getEventsOnDate(LocalDate date);

  /**
   * Retrieves all events occurring within the specified date range.
   *
   * @param start the start date of the range (inclusive)
   * @param end   the end date of the range (inclusive)
   * @return a list of events within the range (never null, may be empty)
   */
  List<Ievent> getEventsInRange(LocalDate start, LocalDate end);

  /**
   * Edits a single event or a single instance of a series event.
   * The event is identified by its subject, start time, and optionally end time.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time (optional, used for verification)
   * @param changes       a map of property names to new values
   * @throws IllegalArgumentException if the event is not found or if the edit
   *                                  would create a conflict
   */
  void editEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                 Map<String, Object> changes);

  /**
   * Checks if the user is busy at the specified date and time.
   *
   * @param dateTime the date and time to check
   * @return true if there is an event at the specified time, false otherwise
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Exports the calendar to a CSV file in Google Calendar format.
   *
   * @param filename the name of the file to create
   * @throws RuntimeException if there is an error writing to the file
   */
  void exportToCsv(String filename);

  /**
   * Shows the availability status at the specified date and time.
   *
   * @param dateTime the date and time to check
   * @return "busy" if there is an event at that time, "available" otherwise
   */
  String showStatus(LocalDateTime dateTime);

  /**
   * Edits events in a series with the specified scope.
   *
   * @param subject     the subject of the event to edit
   * @param startTime   the start time of the reference event
   * @param endDateTime the end time (optional, used for verification)
   * @param changes     a map of property names to new values
   * @param scope       the scope of the edit (SINGLE, FROM_THIS, or ALL_IN_SERIES)
   * @throws IllegalArgumentException if the event is not found or if the edit
   *                                  would create conflicts
   */
  void editSeriesEvent(String subject, LocalDateTime startTime, LocalDateTime endDateTime,
                       Map<String, Object> changes, EditScope scope);
}
