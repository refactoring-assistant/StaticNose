package calendar.event;

import calendar.enums.EventStatus;
import calendar.enums.UserStatus;
import java.time.LocalDateTime;

/**
 * This interface contains necessary operations that an event should support.
 */
public interface EventInterface {

  /**
   * To get the subject of the event series.
   *
   * @return the subject of the event series
   */
  String getSubject();

  /**
   * To get the start date/time of the event series.
   *
   * @return the start date/time of the event series
   */
  LocalDateTime getStartDateTime();

  /**
   * To get the end date/time of the event series.
   *
   * @return the end date/time of the event series
   */
  LocalDateTime getEndDateTime();

  /**
   * To get the description of the event series.
   *
   * @return the description of the event series
   */
  String getDescription();

  /**
   * To get the location of the event series.
   *
   * @return the location of the event series
   */
  String getLocation();

  /**
   * To get the event status of the event series.
   *
   * @return the event status of the event series
   */
  EventStatus getEventStatus();

  /**
   * To get the series id of the event series.
   *
   * @return the series id of the event series
   */
  String getSeriesId();

  /**
   * To update the description of the event series.
   *
   * @param newDescription the new description of the event series
   */
  void editDescription(String newDescription);

  /**
   * To update the location of the event series.
   *
   * @param newLocation the new location of the event series
   */
  void editLocation(String newLocation);

  /**
   * To update the event status of the event series.
   *
   * @param newEventStatus the new eventStatus (public/private) of the event series
   */
  void editEventStatus(String newEventStatus);
}
