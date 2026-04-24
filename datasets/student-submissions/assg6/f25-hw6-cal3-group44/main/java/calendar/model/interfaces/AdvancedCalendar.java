package calendar.model.interfaces;

import java.time.ZoneId;

/**
 * Represents an advanced calendar that includes additional
 * information like name and time zone, and the calendar.
 */
public interface AdvancedCalendar extends CalendarEditable {

  /**
   * Returns the name of this calendar.
   *
   * @return the calendar name
   */
  String getName();

  /**
   * Returns the time zone associated with this calendar.
   *
   * @return the calendar's time zone
   */
  ZoneId getZoneId();

  /**
   * Returns the underlying editable calendar instance.
   *
   * @return the editable calendar
   */
  CalendarEditable getCalendar();
}
