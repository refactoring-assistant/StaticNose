package calendar.model.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface representing a calendar event.
 * An event must have a subject, start date/time, and end date/time.
 * Optionally, it may have a description, location, and public/private status.
 */
public interface Ievent {

  /**
   * Gets the subject/title of the event.
   *
   * @return the event subject
   */
  String getSubject();

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date/time
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date/time
   */
  LocalDateTime getEndDateTime();

  /**
   * Gets the description of the event.
   *
   * @return the description, or null if not set
   */
  String getDescription();

  /**
   * Gets the location of the event.
   *
   * @return the location, or null if not set
   */
  String getLocation();

  /**
   * Checks if the event is public.
   *
   * @return true if public, false if private
   */
  boolean isPublic();

  /**
   * Gets the event status.
   *
   * @return the event status (PUBLIC or PRIVATE)
   */
  EventStatus getStatus();

  /**
   * Checks if this is an all-day event.
   * An all-day event runs from 8:00 AM to 5:00 PM on the same day.
   *
   * @return true if this is an all-day event
   */
  boolean isAllDay();

  /**
   * Checks if this event spans multiple days.
   *
   * @return true if the event starts and ends on different days
   */
  boolean isMultiDay();

  /**
   * Checks if this event occurs on a specific date.
   *
   * @param date the date to check
   * @return true if the event occurs on that date
   */
  boolean occursOn(LocalDate date);

  /**
   * Checks if this event overlaps with a time period.
   *
   * @param start the start of the period
   * @param end   the end of the period
   * @return true if there's any overlap
   */
  boolean overlapsWith(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if this event conflicts with another event.
   * Two events conflict if they have the same subject, start, and end times.
   *
   * @param other the other event
   * @return true if there's a conflict
   */
  boolean conflictsWith(Ievent other);

  /**
   * Creates a copy of this event with a new subject.
   *
   * @param newSubject the new subject
   * @return a new event with the updated subject
   */
  Ievent withSubject(String newSubject);

  /**
   * Creates a copy of this event with new start time.
   *
   * @param newStart the new start time
   * @return a new event with the updated start time
   */
  Ievent withStartDateTime(LocalDateTime newStart);

  /**
   * Creates a copy of this event with new end time.
   *
   * @param newEnd the new end time
   * @return a new event with the updated end time
   */
  Ievent withEndDateTime(LocalDateTime newEnd);

  /**
   * Creates a copy of this event with a new description.
   *
   * @param newDescription the new description
   * @return a new event with the updated description
   */
  Ievent withDescription(String newDescription);

  /**
   * Creates a copy of this event with a new location.
   *
   * @param newLocation the new location
   * @return a new event with the updated location
   */
  Ievent withLocation(String newLocation);

  /**
   * Creates a copy of this event with new status.
   *
   * @param newStatus the new status
   * @return a new event with the updated status
   */
  Ievent withStatus(EventStatus newStatus);

  /**
   * Formats the event for display.
   *
   * @return a formatted string representation of the event
   */
  String format();
}