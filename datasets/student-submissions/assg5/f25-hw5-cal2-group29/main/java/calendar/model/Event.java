package calendar.model;

import calendar.model.utils.EventStatus;

/**
 * Interface representing a calendar item, which can be a single event
 * or a recurring series. It defines the common properties that all
 * calendar items must have.
 */
public interface Event {
  /**
   * Gets the subject or title of the event.
   *
   * @return The event subject.
   */
  String getSubject();

  /**
   * Gets the description for the event.
   *
   * @return The event description, or null if not set.
   */
  String getDescription();

  /**
   * Gets the location for the event.
   *
   * @return The event location, or null if not set.
   */
  String getLocation();

  /**
   * Gets the visibility status of the event.
   *
   * @return The event status (e.g., PUBLIC or PRIVATE).
   */
  EventStatus getStatus();
}