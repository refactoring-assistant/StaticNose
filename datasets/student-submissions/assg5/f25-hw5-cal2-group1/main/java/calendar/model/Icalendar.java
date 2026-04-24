package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Represents a calendar that can contain events.
 */
public interface Icalendar {
  /**
   * Gets the name of the calendar.
   */
  String getName();

  /**
   * Sets the name of the calendar.
   */
  void setName(String name);

  /**
   * Gets the timezone of the calendar.
   */
  ZoneId getTimezone();

  /**
   * Sets the timezone of the calendar.
   */
  void setTimezone(ZoneId timezone);

  /**
   * Adds an event to the calendar.
   */
  void addEvent(Ievent event);

  /**
   * Gets all events in the calendar.
   */
  List<Ievent> getEvents();

  /**
   * Gets events that occur on a specific date.
   */
  List<Ievent> getEventsOn(LocalDate date);

  /**
   * Gets events in a date range.
   */
  List<Ievent> getEventsInRange(LocalDate start, LocalDate end);

  /**
   * Finds an event by name and start time.
   */
  Ievent findEvent(String name, LocalDate date);
}
