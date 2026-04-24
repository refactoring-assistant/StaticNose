package calendar.model;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Abstraction for a calendar event.
 */
public interface InterfaceEvent {

  /**
   * Returns the subject of the event.
   *
   * @return subject text
   */
  String getSubject();

  /**
   * Returns the start instant of the event.
   *
   * @return start instant
   */
  Instant getStart();

  /**
   * Returns the end instant of the event.
   *
   * @return end instant
   */
  Instant getEnd();

  /**
   * Returns the description of the event.
   *
   * @return description text
   */
  String getDescription();

  /**
   * Returns the location of the event.
   *
   * @return location text
   */
  String getLocation();

  /**
   * Returns true if the event is public.
   *
   * @return true if public
   */
  boolean isPublicEvent();

  /**
   * Returns the timezone associated with this event.
   *
   * @return timezone
   */
  ZoneId getZone();

  /**
   * Returns the duration of the event in seconds.
   *
   * @return duration in seconds
   */
  long getDurationInSeconds();
}
