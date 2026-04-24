package calendar.model;

import java.time.ZoneId;

/**
 * Represents a Calendar Database Model Readonly interface containing getters
 * and non mutating methods.
 */
public interface CalendarDatabaseModelReadOnly {

  /**
   * Retrieves the current calendar's name.
   *
   * @return the name of the calendar.
   */
  String getCurrCalendarName();

  /**
   * Retrieves the current calendar's timezone.
   *
   * @return the timezone of the current calendar.
   */
  ZoneId getCurrTimezone();

  /**
   * Retrieves the current calendar's model.
   *
   * @return the current model
   */
  CalendarModel getCurrCalendarModel();
}
