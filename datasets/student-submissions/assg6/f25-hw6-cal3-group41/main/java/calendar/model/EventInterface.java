package calendar.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface that defines all the methods a calendar event must support.
 */
public interface EventInterface {

  /**
   * unique ID for this event instance.
   */
  UUID getId();

  /**
   *  ID of the series this event belongs to, or null if it’s a single event.
   */
  UUID getSeriesId();

  /**
   * subject or title of the event.
   */
  String subject();

  /**
   * start date and time of the event.
   */
  LocalDateTime startDate();

  /**
   * end date and time of the event.
   */
  LocalDateTime endDate();

  /**
   * description of the event .
   */
  String description();

  /**
   * event location.
   */
  String location();

  /**
   * event status.
   */
  String status();

  /**
   * true if the event covers a full day .
   */
  boolean isAllDay();

  /**
   * true if this event is part of a recurring series.
   */
  boolean isRecurring();

  /**
   * Sets or updates the series ID this event belongs to.
   */
  void setSeriesId(UUID seriesId);

  /**
   * Updates the subject of the event.
   */
  void setSubject(String subject);

  /**
   * Updates the start date and time of the event.
   */
  void setStart(LocalDateTime start);

  /**
   * Updates the end date and time of the event.
   */
  void setEnd(LocalDateTime end);

  /**
   * Sets or updates the event description.
   */
  void setDescription(String description);

  /**
   * Sets or updates the event location.
   */
  void setLocation(String location);

  /**
   * Sets the event status .
   */
  void setStatus(String status);

  /**
   * Marks whether the event is part of a recurring series.
   */
  void setRecurring(boolean isRecurring);

  /**
   * Checks if this event conflicts with another event.
   */
  boolean conflictsWith(Event other);

  /**
   * Converts the event data into a CSV line format for export.
   */
  String toCsv();

  /**
   * Creates and returns a copy of this event.
   */
  Event copy();
}
