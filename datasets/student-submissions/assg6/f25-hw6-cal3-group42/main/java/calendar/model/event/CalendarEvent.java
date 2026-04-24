package calendar.model.event;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Interface representing a calendar event.
 * Provides access to event properties and metadata.
 *
 * <p>Two events are considered equal if they have the same subject,
 * start date-time, and end date-time (as defined by equals/hashCode).
 */
public interface CalendarEvent {

  /**
   * Gets the event subject.
   *
   * @return the subject/title of the event
   */
  String getSubject();

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date-time
   */
  ZonedDateTime getStartDateTime();

  /**
   * Gets the optional description of the event.
   *
   * @return the description, or null if not set
   */
  String getDescription();

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date-time
   */
  ZonedDateTime getEndDateTime();

  /**
   * Gets the optional location of the event.
   *
   * @return the location ("physical" or "online"), or null if not set
   */
  String getLocation();

  /**
   * Gets the optional status of the event.
   *
   * @return the status ("public" or "private"), or null if not set
   */
  String getStatus();

  /**
   * Gets the series UID if this event is part of a series.
   *
   * @return the series UID, or null if this is a standalone event
   */
  String getSeriesUid();

  /**
   * Gets a map representation of this event's properties.
   *
   * @return map containing all event properties
   */
  Map<String, Object> getHashMap();

  /**
   * Checks if this event is part of an event series.
   *
   * @return true if part of a series, false otherwise
   */
  boolean isPartOfSeries();

  /**
   * Compares this event with another for equality based on subject,
   * start time, and end time.
   *
   * @param obj the object to compare with
   * @return true if the events have the same subject, start, and end times
   */
  @Override
  boolean equals(Object obj);

  /**
   * Returns a hash code based on subject, start time, and end time.
   *
   * @return the hash code value
   */
  @Override
  int hashCode();
}