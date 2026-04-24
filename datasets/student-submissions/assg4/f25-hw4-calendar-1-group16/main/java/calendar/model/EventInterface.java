package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for Event (Single and Event Series).
 */
public interface EventInterface {

  /**
   * Obtain the Event name.
   *
   * @return the event name in string.
   */
  String getSubject();

  /**
   * Get Date and Time at which the event starts.
   *
   * @return the start date and time for the start time.
   */
  LocalDateTime getStartDateTime();

  /**
   * Get Date and Time at which the event ends.
   *
   * @return the end date and time for the start time.
   */
  LocalDateTime getEndDateTime();

  /**
   * Get the description(optional) of the event set when creating the event.
   *
   * @return the description for the event.
   */
  String getDescription();

  /**
   * Get the location(optional) of the event set when creating the event.
   *
   * @return the location for the event.
   */
  String getLocation();

  /**
   * Get the SeriesID if the event belongs to a series or return null.
   *
   * @return the string representation of the UUID.
   */
  String getSeriesId();

  /**
   * Get the status of the event. Could be Public or Private.
   *
   * @return The event status (PUBLIC or PRIVATE).
   */
  EventStatus getStatus();

  /**
   * Sets this object's subject to given subject.
   *
   * @param subject the new subject value
   */

  void setSubject(String subject);

  /**
   * Sets this object's startDateTime to given startDateTime.
   *
   * @param startDateTime the new startDateTime value
   */

  void setStartDateTime(LocalDateTime startDateTime);

  /**
   * Sets this object's endDateTime to given endDateTime.
   *
   * @param endDateTime the new endDateTime
   */

  void setEndDateTime(LocalDateTime endDateTime);

  /**
   * Sets this object's description to given description.
   *
   * @param description the new description
   */

  void setDescription(String description);

  /**
   * Sets this object's location to given location.
   *
   * @param location the new location
   */

  void setLocation(String location);

  /**
   * Sets the seriesId to this object to the given one. If null, sets to "None"
   *
   * @param seriesId the new seriesId
   */

  void setSeriesId(String seriesId);

  /**
   * Sets the status to public or private, depending on the input.
   *
   * @param status the updated status
   */

  void setStatus(EventStatus status);
}
