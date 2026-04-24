package model;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Repository that manages a collection of calendars along with the active calendar context.
 * All creation, renaming, and timezone updates are centralized here to keep model mutations
 * within the model layer.
 */
public class CalendarStore {

  private final Map<String, Calendar> calendars = new LinkedHashMap<>();
  private Calendar activeCalendar;

  private static String normalizeName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }
    String trimmed = name.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be blank");
    }
    return trimmed;
  }

  private static String keyFor(String name) {
    return normalizeName(name).toLowerCase();
  }

  private static ZoneId parseZone(String timezoneId) {
    Objects.requireNonNull(timezoneId, "timezoneId");
    try {
      return ZoneId.of(timezoneId);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Unsupported timezone: " + timezoneId, e);
    }
  }

  /**
   * Returns the currently active calendar.
   *
   * @return active calendar
   */
  public Calendar getActiveCalendar() {
    return activeCalendar;
  }

  /**
   * Returns true when a calendar is currently active.
   *
   * @return true if active calendar exists
   */
  public boolean hasActiveCalendar() {
    return activeCalendar != null;
  }

  /**
   * Returns a snapshot of all calendars in insertion order.
   *
   * @return ordered list of calendars
   */
  public List<Calendar> getCalendars() {
    return List.copyOf(calendars.values());
  }

  /**
   * Creates and registers a new calendar.
   *
   * @param name       unique name
   * @param timezoneId timezone identifier
   * @return created calendar
   */
  public Calendar createCalendar(String name, String timezoneId) {
    String normalizedName = normalizeName(name);
    String key = keyFor(normalizedName);
    if (calendars.containsKey(key)) {
      throw new IllegalArgumentException("Calendar already exists: " + normalizedName);
    }
    Calendar calendar = new Calendar(normalizedName, parseZone(timezoneId));
    calendars.put(key, calendar);
    if (activeCalendar == null) {
      activeCalendar = calendar;
    }
    return calendar;
  }

  /**
   * Renames an existing calendar, ensuring uniqueness.
   *
   * @param currentName existing calendar name
   * @param newName     new desired name
   * @return renamed calendar
   */
  public Calendar renameCalendar(String currentName, String newName) {
    Calendar calendar = requireCalendar(currentName);
    String normalizedNew = normalizeName(newName);
    String newKey = keyFor(normalizedNew);

    Calendar existing = calendars.get(newKey);
    if (existing != null && existing != calendar) {
      throw new IllegalArgumentException("Calendar already exists: " + normalizedNew);
    }

    calendars.remove(keyFor(calendar.getName()));
    calendar.rename(normalizedNew);
    calendars.put(newKey, calendar);
    if (activeCalendar == calendar) {
      activeCalendar = calendar;
    }
    return calendar;
  }

  /**
   * Updates the timezone for a calendar.
   *
   * @param calendarName calendar to update
   * @param timezoneId   timezone identifier
   * @return updated calendar
   */
  public Calendar updateTimezone(String calendarName, String timezoneId) {
    Calendar calendar = requireCalendar(calendarName);
    calendar.updateTimezone(parseZone(timezoneId));
    return calendar;
  }

  /**
   * Switches the active calendar context.
   *
   * @param calendarName calendar to activate
   * @return active calendar
   */
  public Calendar setActiveCalendar(String calendarName) {
    Calendar calendar = requireCalendar(calendarName);
    activeCalendar = calendar;
    return calendar;
  }

  /**
   * Returns a calendar by name.
   *
   * @param calendarName name lookup
   * @return calendar
   */
  public Calendar getCalendar(String calendarName) {
    return requireCalendar(calendarName);
  }

  private Calendar requireCalendar(String name) {
    Calendar calendar = calendars.get(keyFor(name));
    if (calendar == null) {
      throw new IllegalArgumentException("Unknown calendar: " + name);
    }
    return calendar;
  }
}
