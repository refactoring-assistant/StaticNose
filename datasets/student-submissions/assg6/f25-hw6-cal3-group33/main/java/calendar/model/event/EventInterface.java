package calendar.model.event;

import java.time.ZonedDateTime;

/**
 * Represents a calendar event with a subject, start/end times, and optional properties.
 * An event can be standalone or part of a recurring series.
 *
 * <p>All events use America/New_York timezone.
 * Events maintain the invariant: startDateTime is always before endDateTime.
 */
public interface EventInterface {

  /**
   * Gets the event subject/title.
   *
   * @return the event subject, never null or empty
   */
  String getSubject();

  /**
   * Gets the event start date and time.
   *
   * @return the start datetime in America/New_York timezone
   */
  ZonedDateTime getStartDateTime();

  /**
   * Gets the event end date and time.
   *
   * @return the end datetime in America/New_York timezone, always after start
   */
  ZonedDateTime getEndDateTime();

  /**
   * Gets the event description.
   *
   * @return the description, or null if not set
   */
  String getDescription();

  /**
   * Gets the event location (physical address or online link).
   *
   * @return the location, or null if not set
   */
  String getLocation();

  /**
   * Gets the event status/visibility.
   *
   * @return the status (PUBLIC or PRIVATE), never null
   */
  EventStatus getStatus();

  /**
   * Gets the series ID if this event is part of a recurring series.
   *
   * @return the series UUID, or null if standalone event
   */
  String getSeriesId();

  /**
   * Sets the event description.
   *
   * @param description the description text, or null to clear
   */
  void setDescription(String description);

  /**
   * Sets the event location.
   *
   * @param location the location text (physical or online), or null to clear
   */
  void setLocation(String location);

  /**
   * Sets the event status/visibility.
   *
   * @param status the status (PUBLIC or PRIVATE), must not be null
   * @throws NullPointerException if status is null
   */
  void setStatus(EventStatus status);

  /**
   * Sets the series ID for this event, making it part of a recurring series.
   *
   * @param seriesId the series UUID, or null to make standalone
   * @throws IllegalStateException if event is multi-day and being added to series
   */
  void setSeriesId(String seriesId);

  /**
   * Removes this event from its series by setting seriesId to null.
   * FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   */
  void removeFromSeries();

  /**
   * Sets the subject. FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   * Bypasses duplicate checking - Calendar must validate before calling.
   *
   * @param subject the new subject, must not be null or empty
   * @throws IllegalArgumentException if subject is null or empty
   */
  void setSubjectInternal(String subject);

  /**
   * Sets the start datetime. FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   * Does NOT validate temporal constraints - Calendar must validate before calling.
   *
   * @param startDateTime the new start datetime, must not be null
   * @throws NullPointerException if startDateTime is null
   */
  void setStartDateTimeInternal(ZonedDateTime startDateTime);

  /**
   * Sets the end datetime. FOR INTERNAL USE BY CALENDAR CLASS ONLY.
   * Does NOT validate temporal constraints - Calendar must validate before calling.
   *
   * @param endDateTime the new end datetime, must not be null
   * @throws NullPointerException if endDateTime is null
   */
  void setEndDateTimeInternal(ZonedDateTime endDateTime);

  /**
   * Checks if this event spans multiple days.
   *
   * @return true if start date and end date are different
   */
  boolean isMultiDay();
}