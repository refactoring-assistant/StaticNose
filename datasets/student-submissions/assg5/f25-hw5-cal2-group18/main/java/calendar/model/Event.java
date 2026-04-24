package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a single calendar event.
 */
public interface Event {

  /**
   * Sets the subject of the event.
   *
   * @param subject the new subject
   * @throws IllegalArgumentException if subject is null
   */
  void setSubject(String subject) throws IllegalArgumentException;

  /**
   * Gets the subject of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Sets the start date and time of the event.
   *
   * @param startDateTime the new start date/time
   * @throws IllegalArgumentException if startDateTime is null
   */
  void setStartDateTime(LocalDateTime startDateTime) throws IllegalArgumentException;

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date/time
   */
  LocalDateTime getStartDateTime();

  /**
   * Sets the end date and time of the event.
   *
   * @param endDateTime the new end date/time
   * @throws IllegalArgumentException if endDateTime is null or before start time
   */
  void setEndDateTime(LocalDateTime endDateTime) throws IllegalArgumentException;

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date/time
   */
  LocalDateTime getEndDateTime();

  /**
   * Sets the description of the event.
   *
   * @param description the description, can be null
   */
  void setDescription(String description);

  /**
   * Gets the description of the event.
   *
   * @return the description
   */
  String getDescription();

  /**
   * Sets the location of the event.
   *
   * @param location the location
   */
  void setLocation(String location);

  /**
   * Gets the location of the event.
   *
   * @return the location
   */
  String getLocation();

  /**
   * Sets the status of the event.
   *
   * @param status the new status
   * @throws IllegalArgumentException if status is null
   */
  void setStatus(EventStatus status) throws IllegalArgumentException;

  /**
   * Gets the status of the event.
   *
   * @return the event status
   */
  EventStatus getStatus();

  /**
   * Checks if this is an all day event, 8:00 AM to 5:00 PM.
   *
   * @return true if this is an all day event
   */
  boolean isAllDay();

  /**
   * Sets the series ID if this event is part of a recurring series.
   *
   * @param seriesId the series ID
   */

  void setSeriesId(String seriesId);

  /**
   * Gets the series ID if this event is part of a recurring series.
   *
   * @return the series ID, or null if this is a single event
   */
  String getSeriesId();

  /**
   * Checks if this event conflicts with another event.
   *
   * @param other the other event to check
   * @return true if the events conflict
   */
  boolean conflictsWith(Event other);

  /**
   * Checks if this event overlaps with a given time.
   *
   * @param dateTime the time to check
   * @return true if the event is occurring at the given time
   */
  boolean occursAt(LocalDateTime dateTime);

  /**
   * Checks if this event occurs within a given date range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return true if the event overlaps with the range
   */
  boolean occursInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Creates a deep copy of this event.
   *
   * @return a new event with the same properties
   */
  Event copy();
}

