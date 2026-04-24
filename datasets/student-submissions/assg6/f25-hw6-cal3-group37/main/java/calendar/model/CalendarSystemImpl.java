package calendar.model;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation managing multiple named calendars with timezones.
 * Uses HashMap for O(1) calendar lookup by name.
 */
public class CalendarSystemImpl implements CalendarSystem {
  // Map: calendar name → CalendarWithTimezone wrapper
  private final Map<String, CalendarWithTimezone> calendars;

  /**
   * Constructs an empty calendar system.
   */
  public CalendarSystemImpl() {
    this.calendars = new HashMap<>();
  }

  @Override
  public void createCalendar(String name, String timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }

    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }

    // Validate timezone
    ZoneId zoneId;
    try {
      zoneId = ZoneId.of(timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }

    // Create calendar with timezone
    CalendarModel calendar = new Calendar();
    CalendarWithTimezone calendarWrapper = new CalendarWithTimezone(name, calendar, zoneId);
    calendars.put(name, calendarWrapper);
  }

  @Override
  public CalendarModel getCalendar(String name) {
    CalendarWithTimezone wrapper = calendars.get(name);
    if (wrapper == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    return wrapper.getCalendar();
  }

  @Override
  public void editCalendar(String calendarName, String property, String newValue) {
    CalendarWithTimezone wrapper = calendars.get(calendarName);
    if (wrapper == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' does not exist");
    }

    switch (property.toLowerCase()) {
      case "name":
        // Check new name doesn't exist
        if (calendars.containsKey(newValue)) {
          throw new IllegalArgumentException("Calendar with name '" + newValue
              + "' already exists");
        }

        // Remove old entry, add with new name
        calendars.remove(calendarName);
        wrapper.setName(newValue);
        calendars.put(newValue, wrapper);
        break;

      case "timezone":
        // Validate and set new timezone
        try {
          ZoneId newZone = ZoneId.of(newValue);
          wrapper.setTimezone(newZone);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + newValue);
        }
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property
            + ". Valid properties are 'name' and 'timezone'");
    }
  }

  @Override
  public List<String> getAllCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public boolean calendarExists(String name) {
    return calendars.containsKey(name);
  }

  @Override
  public ZoneId getCalendarTimezone(String calendarName) {
    CalendarWithTimezone wrapper = calendars.get(calendarName);
    if (wrapper == null) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' does not exist");
    }
    return wrapper.getTimezone();
  }

  /**
   * Inner class wrapping a calendar with its name and timezone.
   */
  private static class CalendarWithTimezone {
    private String name;
    private final CalendarModel calendar;
    private ZoneId timezone;

    CalendarWithTimezone(String name, CalendarModel calendar, ZoneId timezone) {
      this.name = name;
      this.calendar = calendar;
      this.timezone = timezone;
    }

    CalendarModel getCalendar() {
      return calendar;
    }

    ZoneId getTimezone() {
      return timezone;
    }

    void setName(String name) {
      this.name = name;
    }

    void setTimezone(ZoneId timezone) {
      this.timezone = timezone;
    }
  }
}