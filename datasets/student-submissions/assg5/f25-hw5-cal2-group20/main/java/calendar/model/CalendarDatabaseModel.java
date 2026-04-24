package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Represents a Calendar database with calendar models. The application should support the ability
 * to create and maintain several calendars. It should be possible to edit the name of the calendar.
 */
public interface CalendarDatabaseModel extends CalendarDatabaseModelReadOnly {

  /**
   * Constructs a new Calendar to add to the database based on the name and timezone.
   *
   * @param name the calendar name.
   * @param timezoneName the string form of the timezone of the calendar.
   */
  void createCalendar(String name, String timezoneName);

  /**
   * Edits an existing calendar to change either the name or the timezone.
   *
   * @param name the calendar name.
   * @param property either name or timezone.
   * @param value the new name or timezone value to use.
   */
  void editCalendar(String name, CalendarProperty property, String value);

  /**
   * Selects calendar as the one to be currently in use.
   *
   * @param name the calendar name.
   */
  void useCalendar(String name);

  /**
   * Copy event from current calendar to target calendar.
   *
   * @param subject subject of the event.
   * @param targetCal the name of the target calendar.
   * @param startDateTime the starting time/date of the event.
   * @param toDateTime the new time for the event.
   */
  void copyEvent(String subject, String targetCal, String startDateTime, String toDateTime);

  /**
   * Copy events that fall on a specific day from one calendar to another.
   *
   * @param startDate day to pull events from.
   * @param targetCal the name of the target calendar.
   * @param toDate new date to move events to.
   */
  void copyEvents(String startDate, String targetCal, String toDate);

  /**
   * Copy events that fall inside a range of dates from one calendar to another.
   *
   * @param startDate the starting day of the range.
   * @param targetCal the name of the target calendar.
   * @param endDate the ending date of the range.
   * @param toDate new date to move events to.
   */
  void copyEventsInterval(String startDate, String endDate, String targetCal,
                          String toDate);
}
