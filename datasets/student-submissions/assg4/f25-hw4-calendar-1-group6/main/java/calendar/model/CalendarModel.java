package calendar.model;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Public model API for calendar operations (controller talks to this).
 * This is the core logic for managing events.
 */
public interface CalendarModel {

  /**
   * Creates a single calendar event with specific start and end times.
   * This is for non-all-day, non-recurring events.
   *
   * @param subject the event title
   * @param start the start date and time
   * @param end the end date and time
   * @param description optional event description
   * @param location optional event location
   * @param status optional event status
   * @return the created CalendarEvent
   */
  CalendarEvent createSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                                  String description, String location, String status);

  /**
   * Creates an all-day event on a specific date.
   *
   * @param subject the event title
   * @param date the day of the event
   * @param description optional event description
   * @param location optional event location
   * @param status optional event status
   * @return the created CalendarEvent
   */
  CalendarEvent createAllDayEvent(String subject, LocalDate date,
                                  String description, String location, String status);

  /**
   * Creates a recurring event series that stops after a specific number of occurrences (N times).
   *
   * @param subject the event title
   * @param start the start date and time of the first event
   * @param end the end date and time of the first event
   * @param rule recurrence rule (e.g., repeating on Mon, Wed)
   * @param description optional event description
   * @param location optional event location
   * @param status optional event status
   * @return a list of created CalendarEvent instances
   */
  List<CalendarEvent> createSeriesByCount(String subject,
                                          LocalDateTime start, LocalDateTime end,
                                          RecurrenceRule rule,
                                          String description, String location, String status);

  /**
   * Creates a recurring event series that stops on a specific date.
   *
   * @param subject the event title
   * @param start the start date and time of the first event
   * @param end the end date and time of the first event
   * @param rule recurrence rule (e.g., repeating on Mon, Wed)
   * @param description optional event description
   * @param location optional event location
   * @param status optional event status
   * @return a list of created CalendarEvent instances
   */
  List<CalendarEvent> createSeriesUntil(String subject,
                                        LocalDateTime start, LocalDateTime end,
                                        RecurrenceRule rule,
                                        String description, String location, String status);


  /**
   * Edits a property of an existing event or series.
   * The scope determines how many events are changed.
   *
   * @param property the name of the property to edit
   * @param subject the original event subject
   * @param startsAt the original start time
   * @param endsAt the original end time
   * @param newValue the new value for the property
   * @param scope the scope of the edit
   */
  void editEvent(String property, String subject, LocalDateTime startsAt, LocalDateTime endsAt,
                 String newValue, EditScope scope);

  /**
   * Returns all events occurring on a given date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<CalendarEvent> eventsOn(LocalDate date);

  /**
   * Returns all events occurring between two date-time points.
   *
   * @param fromInclusive start of the range (inclusive)
   * @param toInclusive   end of the range (inclusive)
   * @return list of events in the specified range
   */
  List<CalendarEvent> eventsBetween(LocalDateTime fromInclusive, LocalDateTime toInclusive);

  /**
   * Checks if there is any event at a given date-time.
   *
   * @param when the date and time to check
   * @return true if the time is busy, false otherwise
   */
  boolean isBusy(LocalDateTime when);

  /**
   * Exports all calendar events to a CSV file at the given path.
   *
   * @param outputCsvPath the target CSV file path
   * @return the path to the exported CSV file
   */
  Path exportCsv(Path outputCsvPath);

  /**
   * Checks whether an event with the specified subject and times exists.
   *
   * @param subject the event title
   * @param start   the start time
   * @param end     the end time
   * @return true if the event exists, false otherwise
   */
  boolean exists(String subject, LocalDateTime start, LocalDateTime end);
}
