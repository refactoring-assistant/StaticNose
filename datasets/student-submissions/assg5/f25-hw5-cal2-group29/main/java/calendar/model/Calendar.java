package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Defines the core logic for a SINGLE calendar.
 */
public interface Calendar {

  /**
   * Gets the timezone associated with this calendar.
   *
   * @return The ZoneId of this calendar.
   */
  ZoneId getZoneId();

  /**
   * Adds a single, non-recurring event to the calendar.
   *
   * @param newEvent The EventSingle object to add.
   * @throws IllegalArgumentException if the event conflicts with an existing one
   *                                  (same subject, start, and end).
   */
  void createSingleEvent(EventSingle newEvent) throws IllegalArgumentException;

  /**
   * Creates a recurring event series based on a template object.
   *
   * @param seriesTemplate The EventSeries object that defines the series rules.
   * @throws IllegalArgumentException if any event in the generated series conflicts,
   *                                  or if a single event in the series spans multiple days.
   */
  void createEventSeries(EventSeries seriesTemplate) throws IllegalArgumentException;

  /**
   * Edits a single instance of an event.
   * Corresponds to the 'edit event' command (SINGLE scope).
   *
   * @param subject      The subject of the event to find.
   * @param start        The start time of the event to find.
   * @param end          The end time of the event to find.
   * @param propertyName The name of the property to change (e.g., "subject", "start", "end").
   * @param newValue     The new value for the property.
   * @throws IllegalArgumentException if the event cannot be found (is not unique),
   *                                  or if the change creates a conflict.
   */
  void editEvent(String subject, LocalDateTime start, LocalDateTime end,
                 String propertyName, Object newValue)
      throws IllegalArgumentException;

  /**
   * Edits this event and all future events in its series.
   * Corresponds to the 'edit events' command (FROM_THIS scope).
   *
   * @param subject      The subject of an event in the series.
   * @param start        The start time of an event in the series.
   * @param propertyName The name of the property to change.
   * @param newValue     The new value for the property.
   * @throws IllegalArgumentException if the event cannot be found,
   *                                  or if the change creates a conflict.
   */
  void editEventAndFuture(String subject, LocalDateTime start,
                          String propertyName, Object newValue)
      throws IllegalArgumentException;

  /**
   * Edits all events that are part of the *same original series*.
   * Corresponds to the 'edit series' command (ALL scope).
   *
   * @param subject      The subject of an event in the series.
   * @param start        The start time of an event in the series (used to identify the series).
   * @param propertyName The name of the property to change.
   * @param newValue     The new value for the property.
   * @throws IllegalArgumentException if the event cannot be found,
   *                                  or if the change creates a conflict.
   */
  void editFullSeries(String subject, LocalDateTime start,
                      String propertyName, Object newValue)
      throws IllegalArgumentException;

  /**
   * Gets all events scheduled on a specific date.
   *
   * @param date The date to query.
   * @return A list of EventSingle objects occurring on that date (empty list if none found).
   */
  List<EventSingle> getEventsOn(LocalDate date);

  /**
   * Gets all events that overlap with the given time range.
   *
   * @param start The start of the time range.
   * @param end   The end of the time range.
   * @return A list of EventSingle objects overlapping the range (empty list if none found).
   */
  List<EventSingle> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Gets all events in the calendar, primarily for the export command.
   *
   * @return A list of all EventSingle objects in the calendar (empty list if no events).
   */
  List<EventSingle> getAllEvents();

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime The date and time to check.
   * @return true if an event is scheduled at that exact moment, false otherwise.
   */
  boolean isBusy(LocalDateTime dateTime);
}