package calendar.model;

import java.time.LocalDateTime;

/**
 * Interface representing a calendar event.
 * Provides read-only access to event properties.
 * This interface follows the Interface Segregation Principle.
 */
public interface Ievent {

  /**
   * Gets the subject/title of the event.
   *
   * @return the subject
   */
  String getSubject();

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date-time
   */
  LocalDateTime getStart();

  /**
   * Gets the end date and time of the event.
   * Returns null for all-day events before conversion to 8am-5pm.
   *
   * @return the end date-time
   */
  LocalDateTime getEnd();

  /**
   * Gets the description of the event.
   *
   * @return the description (may be null)
   */
  String getDescription();

  /**
   * Gets the location of the event.
   *
   * @return the location (may be null)
   */
  String getLocation();

  /**
   * Gets the status of the event (public/private).
   *
   * @return the status (may be null)
   */
  String getStatus();

  /**
   * Gets the series ID if this event is part of a series.
   *
   * @return the series ID, or null if not part of a series
   */
  String getSeriesId();

  /**
   * Checks if this event is part of a series.
   *
   * @return true if part of a series, false otherwise
   */
  boolean isPartOfSeries();

  /**
   * Creates a copy of this event with updated properties.
   * This follows the Immutable Object pattern for safer event manipulation.
   *
   * @param subject     new subject (null to keep current)
   * @param start       new start time (null to keep current)
   * @param end         new end time (null to keep current)
   * @param description new description (null to keep current)
   * @param location    new location (null to keep current)
   * @param status      new status (null to keep current)
   * @return a new event with updated properties
   */
  Ievent copyWith(String subject, LocalDateTime start, LocalDateTime end,
                  String description, String location, String status);
}