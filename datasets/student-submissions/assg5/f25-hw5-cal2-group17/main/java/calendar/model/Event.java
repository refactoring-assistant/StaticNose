package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for a calendar event.
 */
public interface Event {

  // ========== GETTERS ==========

  /**
   * Gets the subject of the event.
   *
   * @return Event subject
   */
  String getSubject();

  /**
   * Gets the start date and time.
   *
   * @return Start date/time
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the end date and time.
   *
   * @return End date/time, or null for events without end time
   */
  LocalDateTime getEndDateTime();

  /**
   * Gets the event description.
   *
   * @return Description, or null if not set
   */
  String getDescription();

  /**
   * Gets the event location.
   *
   * @return Location, or null if not set
   */
  String getLocation();

  /**
   * Checks if event is private.
   *
   * @return true if private, false if public
   */
  boolean isPrivate();

  /**
   * Gets the series ID if event is part of a series.
   *
   * @return Series ID, or null if not part of a series
   */
  String getSeriesId();

  /**
   * Checks if this is an all-day event (8am-5pm).
   *
   * @return true if all-day event
   */
  boolean isAllDay();

  // ========== SETTERS ==========

  /**
   * Sets the event subject.
   *
   * @param subject New subject (cannot be null or empty)
   */
  void setSubject(String subject);

  /**
   * Sets the start date and time.
   *
   * @param startDateTime New start date/time (cannot be null)
   */
  void setStartDateTime(LocalDateTime startDateTime);

  /**
   * Sets the end date and time.
   *
   * @param endDateTime New end date/time (null for no end time)
   */
  void setEndDateTime(LocalDateTime endDateTime);

  /**
   * Sets the event description.
   *
   * @param description Description text (null to clear)
   */
  void setDescription(String description);

  /**
   * Sets the event location.
   *
   * @param location Location text (null to clear)
   */
  void setLocation(String location);

  /**
   * Sets the event status (private/public).
   *
   * @param status "private" for private event, anything else for public
   */
  void setStatus(String status);

  /**
   * Sets the series ID for recurring events.
   *
   * @param seriesId Series identifier (null for standalone events)
   */
  void setSeriesId(String seriesId);

  // ========== QUERY METHODS ==========

  /**
   * Checks if event is scheduled at a specific time.
   *
   * @param dateTime Time to check
   * @return true if event overlaps with this time
   */
  boolean isScheduledAt(LocalDateTime dateTime);

  /**
   * Checks if event occurs on a specific date.
   *
   * @param date Date to check
   * @return true if event occurs on this date
   */
  boolean occursOnDate(LocalDate date);

  /**
   * Checks if event overlaps with a date/time range.
   *
   * @param rangeStart Start of range (inclusive)
   * @param rangeEnd   End of range (inclusive)
   * @return true if event overlaps with range
   */
  boolean overlapsWithRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);

  /**
   * Checks equality based on subject, start time, and end time.
   *
   * @param o Object to compare
   * @return true if events are equal
   */
  @Override
  boolean equals(Object o);

  /**
   * Generates hash code based on subject, start, and end times.
   *
   * @return Hash code
   */
  @Override
  int hashCode();
}