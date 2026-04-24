package calendar.model;

import java.time.LocalDateTime;

/**
 * Read-only view of an event. This avoids leaking implementation details
 * while still allowing controllers/exporters to consume event data.
 */
public interface EventView {

  /**
   * Returns the subject or title of the event.
   *
   * @return the event subject
   */
  String subject();

  /**
   * Returns the event's start date and time.
   *
   * @return the start timestamp
   */
  LocalDateTime start();

  /**
   * Returns the event's end date and time.
   *
   * @return the end timestamp
   */
  LocalDateTime end();

  /**
   * Returns the description associated with the event.
   *
   * @return the event description, or empty string if none
   */
  String description();

  /**
   * Returns the event's location.
   *
   * @return the location string, or empty string if none
   */
  String location();

  /**
   * Returns the visibility or status of the event.
   *
   * @return the event status enum value
   */
  EventSpec.Status status();

  /**
   * Indicates whether the event is marked as an all-day event.
   *
   * @return true if the event is all-day, false otherwise
   */
  boolean allDay();
}