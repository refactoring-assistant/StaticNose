package calendar.model;

import java.time.ZonedDateTime;

/**
 * Represents event(s) on the calendar. Every event has a subject and start time.
 * Other details are optional. This interface lets you get or change any part of the event safely.
 */
public interface EventInterface {

  /**
   * Gets the title or name of the event. This is required and must be unique with start/end times.
   *
   * @return the event's subject (never null).
   */
  String getSubject();

  /**
   * Changes the title of the event. Used when editing an event, also making sure the new subject
   * doesn't create a conflict.
   *
   * @param subject the new title (cannot be null or empty).
   */
  void setSubject(String subject);

  /**
   * Gets when the event starts and includes both date and time. For all-day events,
   * time is usually 8:00 AM.
   *
   * @return the start date and time (never null).
   */
  ZonedDateTime getStart();

  /**
   * Changes the start time of the event. Must be before the end time, if there is one.
   *
   * @param start the new start time (cannot be null).
   */
  void setStart(ZonedDateTime start);

  /**
   * Gets when the event ends and includes date and time. Returns null for all-day
   * events (they end at 5:00 PM same day).
   *
   * @return the end time, or null if all-day.
   */
  ZonedDateTime getEnd();

  /**
   * Changes the end time of the event. Must be after the start time. Uses null for all-day events.
   *
   * @param end the new end time, or null for all-day.
   */
  void setEnd(ZonedDateTime end);

  /**
   * Gets the extra notes or details about the event. This is optional and can be empty or null.
   *
   * @return the description, or null if none.
   */
  String getDescription();

  /**
   * Sets or updates the event description. Pass null or empty string to clear it.
   *
   * @param description the new description (can be null).
   */
  void setDescription(String description);

  /**
   * Gets where the event happens(location). Optional field.
   *
   * @return the location, or null if not set.
   */
  String getLocation();

  /**
   * Changes the event location. Uses null to remove it.
   *
   * @param location the new location (can be null).
   */
  void setLocation(String location);

  /**
   * Gets whether the event is public or private. Usually returns "public" or "private" as a string.
   *
   * @return the status, or null if not set
   */
  EventStatus getStatus();

  /**
   * Sets the event to public or private. Use "public" or "private" (anything
   * else might be ignored).
   *
   * @param status the new status (can be null).
   */
  void setStatus(EventStatus status);

  /**
   * Checks if this is an all-day event (example - holiday). True if it runs from 8 AM to 5 PM with
   * no specific end time.
   *
   * @return true if all-day, false otherwise.
   */
  boolean isAllDay();

  /**
   * Marks the event as all-day or not. When set to true, end time is ignored and treated as 5 PM.
   *
   * @param allDay true to make it all-day, false for timed event.
   */
  void setAllDay(boolean allDay);
}