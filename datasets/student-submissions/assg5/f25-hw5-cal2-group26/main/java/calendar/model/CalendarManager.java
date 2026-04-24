package calendar.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages multiple calendars and the active calendar context.
 * Provides creation, deletion, selection, and editing of calendars.
 */
public class CalendarManager implements CalendarManagerInterface {

  private final Map<String, CalendarInterface> calendars = new HashMap<>();
  private CalendarInterface activeCalendar;

  @Override
  public void createCalendar(String name, ZoneId zone) {
    Objects.requireNonNull(name, "Calendar name cannot be null");
    Objects.requireNonNull(zone, "Timezone cannot be null");

    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException(
          "Calendar with name '" + name + "' already exists.");
    }

    CalendarInterface cal = new CalendarImpl(name);
    cal.setCalendarZone(zone);
    calendars.put(name, cal);

    if (activeCalendar == null) {
      activeCalendar = cal;
    }
  }

  @Override
  public void deleteCalendar(String name) {
    CalendarInterface removed = calendars.remove(name);
    if (removed == null) {
      throw new IllegalArgumentException(
          "Calendar '" + name + "' does not exist.");
    }

    if (activeCalendar == removed) {
      activeCalendar =
          calendars.isEmpty() ? null : calendars.values().iterator().next();
    }
  }

  @Override
  public void useCalendar(String name) {
    CalendarInterface cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException(
          "Calendar '" + name + "' does not exist.");
    }
    activeCalendar = cal;
  }

  @Override
  public CalendarInterface getActiveCalendar() {
    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar set.");
    }
    return activeCalendar;
  }

  @Override
  public CalendarInterface getCalendar(String name) {
    CalendarInterface cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException(
          "Calendar '" + name + "' does not exist.");
    }
    return cal;
  }

  @Override
  public Collection<CalendarInterface> getAllCalendars() {
    return Collections.unmodifiableCollection(calendars.values());
  }

  @Override
  public void editCalendarName(String currentName, String newName) {
    Objects.requireNonNull(currentName, "Current calendar name cannot be null");
    Objects.requireNonNull(newName, "New calendar name cannot be null");

    CalendarInterface cal = getCalendar(currentName);

    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException(
          "Calendar with name '" + newName + "' already exists.");
    }

    calendars.remove(currentName);
    cal.setName(newName);
    calendars.put(newName, cal);
  }

  @Override
  public void changeCalendarTimezone(String name, ZoneId newZone) {
    Objects.requireNonNull(name, "Calendar name cannot be null");
    Objects.requireNonNull(newZone, "New timezone cannot be null");

    CalendarInterface cal = getCalendar(name);
    ZoneId oldZone = cal.getCalendarZone();
    cal.setCalendarZone(newZone);

    for (EventInterface event : cal.getAllCalendarEvents()) {
      adjustEventToNewZone(event, oldZone, newZone);
    }
  }


  private void adjustEventToNewZone(EventInterface event, ZoneId oldZone, ZoneId newZone) {
    ZonedDateTime start = event.getStart().withZoneSameInstant(newZone);
    event.setStart(start);

    if (event.getEnd() != null) {
      ZonedDateTime end = event.getEnd().withZoneSameInstant(newZone);
      event.setEnd(end);
    }
  }
}
