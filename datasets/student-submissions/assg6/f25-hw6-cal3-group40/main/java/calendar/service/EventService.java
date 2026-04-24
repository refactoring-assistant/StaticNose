package calendar.service;

import calendar.model.Event;
import calendar.model.exceptions.ConflictException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * An interface dedicated to event-related operations.
 * Assumes an "active" calendar is managed by the implementation.
 */
public interface EventService {

  /**
   * Creates a new calendar event in the current calendar.
   */
  void createEvent(String subject, String fromStr, String toStr, String onStr,
      String description, String location, boolean isPrivate,
      String repeats, Integer occurrences, String untilStr)
      throws ConflictException, IllegalArgumentException;

  /**
   * Edits an existing event's property in the current calendar.
   */
  void editEvent(String subject, String fromStr, String toStr,
      String property, String newValueStr,
      boolean singleEventUpdate, boolean updateAll);

  /**
   * Retrieves all events occurring on the specified date in the current calendar.
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Retrieves all events between two date/time values in the current calendar.
   */
  List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Determines whether the current calendar is busy at the specified date/time.
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Returns the current calendar's event data in CSV format.
   */
  String getCsvData();

  /**
   * Exports the current calendar data to a file.
   */
  String exportCalendar(String fileName) throws IOException;

  /**
   * Finds a single, unique event by its subject and start time.
   *
   * @param subject The event's subject.
   * @param start   The event's start time (in the calendar's local time).
   * @return The matching Event, or null if not found.
   */
  Event findUniqueEvent(String subject, LocalDateTime start);

  /**
   * Updates the start and end time of an event (and potentially its series).
   *
   * @param subject           The subject of the event.
   * @param currentStartStr   The current start time string.
   * @param newStartStr       The new start time string.
   * @param newEndStr         The new end time string.
   * @param singleEventUpdate True to update only this event.
   * @param updateAll         True to update all events in the series.
   */
  void updateEventTime(String subject, String currentStartStr, String newStartStr, String newEndStr,
      boolean singleEventUpdate, boolean updateAll);
}