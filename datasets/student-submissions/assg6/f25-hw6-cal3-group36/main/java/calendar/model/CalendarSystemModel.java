package calendar.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Interface defining the features of model by creating a layer on top of the calendar models.
 * below are the individual features explained in detail.
 */
public interface CalendarSystemModel {

  /**
   * Creates a new calendar with a unique name and a timezone.
   *
   * @param name calendar name
   * @param timezone IANA timezone identifier
   */
  void createCalendar(String name, String timezone);

  /**
   * Renames an existing calendar.
   *
   * @param oldName current name
   * @param newName new unique name
   */
  void renameCalendar(String oldName, String newName);

  /**
   * Changes the timezone of a calendar.
   *
   * @param calendarName calendar name
   * @param timezone IANA timezone identifier
   */
  void changeCalendarTimezone(String calendarName, String timezone);

  /**
   * Sets the active calendar context for event operations.
   *
   * @param calendarName calendar to use
   */
  void useCalendar(String calendarName);

  /**
   * Creates a single event in the active calendar.
   *
   * @param event event instance
   */
  void createEvent(InterfaceEvent event);

  /**
   * Creates a recurring series by expanding the template.
   *
   * @param template event template
   * @param rule recurrence rule
   */
  void createRecurringEvent(InterfaceEvent template, RecurrenceRule rule);

  /**
   * Replaces a single identified event with a replacement.
   * Here subject and start instant to find a unique event.
   *
   * @param subject subject of the target event
   * @param start start instant of the target event
   * @param replacement replacement event instance
   */
  void editEvent(String subject, Instant start, InterfaceEvent replacement);

  /**
   * Edits events within a series based on the identified event.
   *
   * @param subject subject of the identified event
   * @param start start instant of the identified event
   * @param replacement replacement event instance
   * @param affectFuture whether to affect the identified event and future events in that series
   * @param affectAll whether to affect all events in that series
   */
  void editSeries(String subject, Instant start, InterfaceEvent replacement,
                  boolean affectFuture, boolean affectAll);

  /**
   * Copies a single event to a target calendar at a target start instant while preserving duration.
   *
   * @param subject subject of the source event
   * @param start start instant of the source event
   * @param targetCalendar target calendar name
   * @param newStart start instant in the target calendar's timezone
   */
  void copyEvent(String subject, Instant start, String targetCalendar, Instant newStart);

  /**
   * Copies all events scheduled on the given date.
   *
   * @param date source date in active calendar timezone
   * @param targetCalendar target calendar name
   * @param targetDate date in target calendar timezone to align the copy
   */
  void copyEventsOn(LocalDate date, String targetCalendar, LocalDate targetDate);

  /**
   * Copies all events that overlap the inclusive date interval to the target calendar.
   *
   * @param start start date inclusive in active calendar timezone
   * @param end end date inclusive in active calendar timezone
   * @param targetCalendar target calendar name
   * @param targetStart date in target calendar timezone representing the start alignment point
   */
  void copyEventsBetween(LocalDate start, LocalDate end, String targetCalendar,
                         LocalDate targetStart);

  /**
   * Export the active calendar to a CSV file.
   *
   * @param fileName file name or relative path
   * @return absolute path to the written file
   */
  String exportCsv(String fileName);

  /**
   * Export the active calendar to an iCal file.
   *
   * @param fileName file name or relative path.
   * @return absolute path to the written file
   */
  String exportIcal(String fileName);

  /**
   * Returns the timezone of the given calendar.
   *
   * @param calendarName calendar name
   * @return timezone of that calendar
   */
  ZoneId getCalendarTimeZone(String calendarName);

  /**
   * Returns the currently active calendar.
   *
   * @return active calendar model
   */
  CalendarModel getActiveCalendar();
}