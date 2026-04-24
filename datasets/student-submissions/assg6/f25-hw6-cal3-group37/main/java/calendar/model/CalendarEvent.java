package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a calendar event with subject, start/end times, and optional metadata.
 * Events can be single occurrences or part of a recurring series.
 */
public interface CalendarEvent {

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
   * @return the start datetime
   */
  LocalDateTime getStartDateTime();

  /**
   * Sets the start date and time of the event.
   *
   * @param startDateTime the new start datetime
   */
  void setStartDateTime(LocalDateTime startDateTime);

  /**
   * Gets the end date and time of the event.
   *
   * @return the end datetime
   */
  LocalDateTime getEndDateTime();

  /**
   * Sets the end date and time of the event.
   *
   * @param endDateTime the new end datetime
   */
  void setEndDateTime(LocalDateTime endDateTime);

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
   * Gets the status of the event (public or private).
   *
   * @return the status
   */
  String getStatus();

  /**
   * Sets the status of the event.
   *
   * @param status the new status
   */
  void setStatus(String status);

  /**
   * Checks if this is an all-day event (8am-5pm).
   *
   * @return true if all-day event, false otherwise
   */
  boolean isAllDayEvent();

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
   * Creates a copy of this event.
   *
   * @return a new event with the same properties
   */
  CalendarEvent copy();
}