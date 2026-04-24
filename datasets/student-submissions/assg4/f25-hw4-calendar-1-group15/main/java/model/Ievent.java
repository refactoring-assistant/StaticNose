package model;

import java.time.LocalDateTime;

/**
 * This interface represents a calendar event.
 */
public interface Ievent {

  /**
   * Get the subject of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Get the startDateTime of the event.
   *
   * @return the event start date and time
   */
  LocalDateTime getStartDateTime();

  /**
   * Get the endDateTime of the event.
   *
   * @return the event end date and time
   */
  LocalDateTime getEndDateTime();

  /**
   * Get the description of the event.
   *
   * @return the event description
   */
  String getDescription();

  /**
   * Get the location of the event.
   *
   * @return the event location
   */
  String getLocation();

  /**
   * Gets the visibility of the event - public or private.
   *
   * @return true if event is public, false if private
   */
  boolean isPublic();

  /**
   * Determines whether the event is a single event or is a part of a recurring event series.
   *
   * @return true if the event is part of a recurring event series, false if it is a single event
   */
  boolean isPartOfSeries();

  /**
   * Gets the unique identifier of the series that this event belongs to.
   *
   * @return the seriesId if the event is part of a series, null otherwise
   */
  String getSeriesId();
}
