package calendar.model.calendar;

import calendar.exceptions.InvalidDateTimeException;
import java.time.ZoneId;

/**
 * Interface for managing calendar metadata and properties.
 * Provides access to calendar name and timezone operations without
 * exposing event manipulation capabilities.
 */
public interface CalendarPropertiesInterface {

  /**
   * Returns the name of this calendar.
   *
   * @return the calendar name
   */
  String getCalendarName();

  /**
   * Sets the name of this calendar.
   *
   * @param name the new calendar name
   */
  void setName(String name);

  /**
   * Returns the timezone of this calendar.
   *
   * @return the calendar timezone
   */
  ZoneId getTimezone();

  /**
   * Sets the timezone of this calendar and converts all existing events.
   *
   * @param timezone the new timezone
   * @throws InvalidDateTimeException if timezone conversion fails
   */
  void setTimezone(ZoneId timezone) throws InvalidDateTimeException;
}