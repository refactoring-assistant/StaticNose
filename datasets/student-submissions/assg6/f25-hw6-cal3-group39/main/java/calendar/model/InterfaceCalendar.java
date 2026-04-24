package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * This is the Calendar Interface that is used inside Model to create and maintain calendars of
 * different time zones.
 * The interface has been declared package-private as the usage of this calendar is only in
 * the model.
 */
interface InterfaceCalendar {

  /**
   * This returns the TreeMap calendar using the getMap function.
   *
   * @return is the TreeMap calendar.
   */
  TreeMap<LocalDate, HashSet<Event>> getMap();

  /**
   * This returns the time-zone of the particular calendar.
   *
   * @return ZoneId is returned.
   */
  ZoneId getZone();

  /**
   * This is the setter for the time-zone.
   *
   * @param zone is the ZoneId to set.
   */
  void setZone(ZoneId zone);

}
