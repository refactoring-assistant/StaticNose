package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * This is the Calendar class that implements the InterfaceCalendar Interface.
 */
class Calendar implements InterfaceCalendar {
  private ZoneId zone;
  private TreeMap<LocalDate, HashSet<Event>> calendar = new TreeMap<>();

  /**
   * The Calendar constructor.
   *
   * @param timeZone is the param.
   */
  public Calendar(String timeZone) {
    this.zone = ZoneId.of(timeZone);
    this.calendar = new TreeMap<>();
  }

  @Override
  public TreeMap<LocalDate, HashSet<Event>> getMap() {
    return calendar;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public void setZone(ZoneId zone) {
    this.zone = zone;
  }
}
