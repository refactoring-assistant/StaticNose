package calendar.model.debug;

import calendar.model.CalendarManager;
import calendar.model.IntCalendar;
import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Debug only class for testing Database commands.
 */
public class DebugCalendarManager extends CalendarManager {
  private final Map<String, DebugCalendar> calendars;
  private String activeCalendarName;
  private final Appendable log;

  /**
   * DebugDatabase constructor.
   *
   * @param log the appendable to write to for tests to read from
   */
  public DebugCalendarManager(Appendable log) {
    this.calendars = new HashMap<>();
    this.activeCalendarName = null;
    this.log = log;
  }

  @Override
  public void createCalendar(String name, ZoneId zoneId) {
    try {
      log.append(name)
          .append(", ")
          .append(zoneId.toString());
      DebugCalendar calendar;
      if (name.equals("debug")) {
        calendar = new DebugCalendar(name, new DebugAppendable());
      } else {
        calendar = new DebugCalendar(name, new StringBuilder());
      }
      calendars.put(name, calendar);

      activeCalendarName = name;
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public DebugCalendar getActiveCalendar() {
    if (activeCalendarName == null) {
      throw new IllegalStateException("No active calendar set");
    }
    return calendars.get(activeCalendarName);
  }

  @Override
  public void setActiveCalendar(String name) {
    this.activeCalendarName = name;
  }

  @Override
  public void editCalendarName(String oldName, String newName) {
    DebugCalendar calendar = calendars.get(oldName);
    DebugCalendar updatedCalendar = calendar.withName(newName);

    calendars.remove(oldName);
    calendars.put(newName, updatedCalendar);

    activeCalendarName = newName;
  }

  @Override
  public void editCalendarTimezone(String name, ZoneId newTimezone) {
    DebugCalendar calendar = calendars.get(name);
    DebugCalendar updatedCalendar = calendar.withTimezone(newTimezone);
    calendars.put(name, updatedCalendar);
  }
}
