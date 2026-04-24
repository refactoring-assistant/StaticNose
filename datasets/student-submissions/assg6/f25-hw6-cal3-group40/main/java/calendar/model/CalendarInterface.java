package calendar.model;

import calendar.model.repository.EventRepository;
import java.time.ZoneId;

/**
 * Interface representing a Calendar.
 */
public interface CalendarInterface {

  /**
   * Gets the calendar's name.
   *
   * @return The calendar name.
   */
  String getName();

  /**
   * Sets the calendar's name.
   *
   * @param name the new name
   */
  void setName(String name);

  /**
   * Gets the calendar's timezone.
   *
   * @return The timezone.
   */
  ZoneId getTimezone();

  /**
   * Sets the calendar's timezone.
   *
   * @param timezone the new timezone
   */
  void setTimezone(ZoneId timezone);

  /**
   * Gets the event repository associated with this calendar.
   *
   * @return the event repository
   */
  EventRepository getEventRepository();
}
