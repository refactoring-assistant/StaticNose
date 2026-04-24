package calendar.model;

import java.time.LocalDateTime;

/**
 * This interface represents an Event object that can be created or changed. It is a part of the
 * Calendar, can be included in the Event Series.
 */
public interface EventObject {
  /**
   * Gets the subject of the event.
   *
   * @return event subject.
   */
  String getSubject();

  /**
   * Gets the start datetime of the event.
   *
   * @return event start datetime.
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the description of the event. Description is an optional field for events.
   *
   * @return event description.
   */
  String getDescription();

  /**
   * Gets the end datetime of the event. If end datetime is not provided, it is an all day event.
   *
   * @return event end datetime.
   */
  LocalDateTime getEndDateTime();

  /**
   * Gets the location of the event. One of the enum values of EventLocation.
   *
   * @return event location.
   */
  EventLocation getLocation();

  /**
   * Gets the status of the event. One of the enum values of EventStatus.
   *
   * @return event status.
   */
  EventStatus getStatus();

  /**
   * Gets an event series id if the event is a part of event series.
   *
   * @return event series id.
   */
  int getEventSeriesId();

  /**
   * Gets the event in string form for printing out as a bullet point.
   *
   * @return the string event prepared for a bullet point list.
   */
  String eventForBulletPoint();
}
