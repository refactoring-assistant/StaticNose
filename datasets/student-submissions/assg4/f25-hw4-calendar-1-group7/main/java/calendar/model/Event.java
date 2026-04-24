package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a calendar event with a subject, start/end times, and optional properties.
 */
public interface Event {

  /**
   * Gets the subject/title of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Sets the subject/title of the event.
   *
   * @param subject the new subject
   */
  void setSubject(String subject);

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date/time
   */
  LocalDateTime getStart();

  /**
   * Sets the start date and time of the event.
   *
   * @param start the new start date/time
   */
  void setStart(LocalDateTime start);

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date/time
   */
  LocalDateTime getEnd();

  /**
   * Sets the end date and time of the event.
   *
   * @param end the new end date/time
   */
  void setEnd(LocalDateTime end);

  /**
   * Gets the location of the event.
   *
   * @return the location, or null if not set
   */
  String getLocation();

  /**
   * Sets the location of the event.
   *
   * @param location the new location
   */
  void setLocation(String location);

  /**
   * Gets the description of the event.
   *
   * @return the description, or null if not set
   */
  String getDescription();

  /**
   * Sets the description of the event.
   *
   * @param description the new description
   */
  void setDescription(String description);

  /**
   * Gets the status of the event (public or private).
   *
   * @return the status
   */
  String getStatus();

  /**
   * Sets the status of the event.
   *
   * @param status the new status (public or private)
   */
  void setStatus(String status);

  /**
   * Gets the series ID if this event is part of a recurring series.
   *
   * @return the series ID, or null if not part of a series
   */
  String getSeriesId();

  /**
   * Sets the series ID for this event.
   *
   * @param seriesId the series ID
   */
  void setSeriesId(String seriesId);

  /**
   * Checks if this is an all-day event.
   *
   * @return true if all-day event, false otherwise
   */
  boolean isAllDay();

  /**
   * Creates a copy of this event.
   *
   * @return a new Event with the same properties
   */
  Event copy();
}