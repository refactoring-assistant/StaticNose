package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Represents a calendar event with required and optional properties.
 * All events must have a subject and start date/time.
 * Supports both single events and recurring event instances.
 */
public interface InEvent {

  /**
   * Gets the subject of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Sets the subject of the event.
   *
   * @param subject the new subject
   */
  void setSubject(String subject);

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date/time
   */
  LocalDateTime getStartDateTime();

  /**
   * Sets the start date and time of the event.
   *
   * @param startDateTime the new start date/time
   */
  void setStartDateTime(LocalDateTime startDateTime);

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date/time
   */
  LocalDateTime getEndDateTime();

  /**
   * Sets the end date and time of the event.
   *
   * @param endDateTime the new end date/time
   */
  void setEndDateTime(LocalDateTime endDateTime);

  /**
   * Gets the optional description of the event.
   *
   * @return Optional containing description if present, empty otherwise
   */
  Optional<String> getDescription();

  /**
   * Sets the description of the event.
   *
   * @param description the event description, or null to clear
   */
  void setDescription(String description);

  /**
   * Gets the optional location of the event.
   *
   * @return Optional containing location if present, empty otherwise
   */
  Optional<String> getLocation();

  /**
   * Sets the location of the event.
   *
   * @param location the event location, or null to clear
   */
  void setLocation(String location);

  /**
   * Gets the privacy status of the event.
   *
   * @return the event status (PUBLIC or PRIVATE)
   */
  EventStatus getStatus();

  /**
   * Sets the privacy status of the event.
   *
   * @param status the new status
   */
  void setStatus(EventStatus status);

  /**
   * Checks if this is an all-day event (8am to 5pm).
   *
   * @return true if this is an all-day event, false otherwise
   */
  boolean isAllDayEvent();

  /**
   * Checks if this event conflicts with another event.
   * Two events conflict if their time ranges overlap.
   *
   * @param other the other event to check against
   * @return true if events conflict, false otherwise
   */
  boolean conflictsWith(InEvent other);

  /**
   * Checks if this event occurs on a specific date.
   *
   * @param date the date to check
   * @return true if event occurs on this date, false otherwise
   */
  boolean occursOn(LocalDate date);

  /**
   * Checks if this event occurs between two date/times.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return true if event occurs in this range, false otherwise
   */
  boolean occursBetween(LocalDateTime start, LocalDateTime end);

  /**
   * Creates a deep copy of this event.
   *
   * @return a new event with the same properties
   */
  InEvent copy();
}


