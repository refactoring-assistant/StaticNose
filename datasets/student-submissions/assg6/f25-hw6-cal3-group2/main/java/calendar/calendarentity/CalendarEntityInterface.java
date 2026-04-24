package calendar.calendarentity;

import calendar.model.CalendarInterface;
import java.time.ZoneId;

/**
 * This interface contains necessary operations that a single calendar with name and timezone
 * should support.
 */
public interface CalendarEntityInterface {

  /**
   * To get the name of the calendar entity.
   *
   * @return the name of the calendar entity
   */
  String getCalendarName();

  /**
   * To get the timezone of the calendar entity.
   *
   * @return the timezone of the calendar entity
   */
  ZoneId getTimeZone();

  /**
   * To get the calendar object of the calendar entity.
   *
   * @return the calendar object of the calendar entity
   */
  CalendarInterface getCalendar();


}
