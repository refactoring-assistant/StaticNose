package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a calendar event with a subject, start time, end time,
 * and optional properties like description, location, and status.
 */
public interface Event {
  /**
   * Get the subject/title of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Get the start date and time of the event.
   *
   * @return the start date/time
   */
  LocalDateTime getStartDateTime();

  /**
   * Get the end date and time of the event.
   *
   * @return the end date/time
   */
  LocalDateTime getEndDateTime();

  /**
   * Get the optional description of the event.
   *
   * @return the description, or empty string if none
   */
  String getDescription();

  /**
   * Get the optional location of the event.
   *
   * @return the location, or empty string if none
   */
  String getLocation();

  /**
   * Get the status of the event (PUBLIC or PRIVATE).
   *
   * @return the event status
   */
  Status getStatus();

  /**
   * Get the series ID if this event belongs to a recurring series.
   *
   * @return the series ID, or null if not part of a series
   */
  String getSeriesId();

  /**
   * Checks if the event is an all day event.
   *
   * @return true if the event is an All Day event
   */
  Boolean isAllDay();
}