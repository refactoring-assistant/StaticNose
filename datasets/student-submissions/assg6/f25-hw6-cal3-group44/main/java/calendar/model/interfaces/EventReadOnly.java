package calendar.model.interfaces;

import calendar.model.datatypes.EventStatus;
import calendar.model.datatypes.Location;
import calendar.model.datatypes.TypeOfEvent;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a read-only view of an event in the calendar.
 * Provides access to all immutable properties of an event such as
 * its subject, description, timing, location, and status.
 * Can be used when we have to give access the event data but not allowed to modify.
 */
public interface EventReadOnly {
  /**
   * Returns the subject.
   */
  String getSubject();

  /**
   * Returns the StartDateTime.
   */
  LocalDateTime getStartDateTime();

  /**
   * Returns the EndDateTime.
   */
  LocalDateTime getEndDateTime();

  /**
   * Returns the description.
   */
  String getDescription();

  /**
   * Returns the location.
   */
  Location getLocation();

  /**
   * Returns the Event Status.
   */
  EventStatus getEventStatus();

  /**
   * Returns the Event Type.
   */
  TypeOfEvent getEventType();

  /**
   * Returns the Unique ID.
   */
  UUID getId();

  /**
   * Indicates whether the event lasts for the entire day.
   */
  boolean isAllDay();
}
