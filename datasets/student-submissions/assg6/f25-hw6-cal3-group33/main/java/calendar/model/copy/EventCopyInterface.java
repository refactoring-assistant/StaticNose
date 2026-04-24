package calendar.model.copy;

import calendar.exceptions.DuplicateEventException;
import calendar.exceptions.EventNotFoundException;
import calendar.exceptions.InvalidDateTimeException;
import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.ReadOnlyCalendar;

/**
 * Interface for copying events between calendars.
 * Provides methods to copy single events, events on a specific date,
 * or events within a date range from one calendar to another.
 * All copy operations preserve event properties (subject, location, description, status)
 * while adjusting start and end times to the target calendar's timezone and specified offset.
 */
public interface EventCopyInterface {

  /**
   * Copies a single event from the source calendar to the target calendar.
   * The event is identified by its subject and start datetime, then copied with
   * the specified target start datetime. The duration is preserved.
   *
   * @param sourceCalendar the calendar to copy from
   * @param eventSubject the subject of the event to copy
   * @param sourceStartDateTime the start datetime of the event to copy (format: YYYY-MM-DDThh:mm)
   * @param targetCalendar the calendar to copy to
   * @param targetStartDateTime the new start datetime for the copied event
   *                            (format: YYYY-MM-DDThh:mm)
   * @return the number of events successfully copied (1 if successful)
   * @throws EventNotFoundException if no event with the given subject and start time exists
   * @throws InvalidDateTimeException if datetime formats are invalid
   * @throws DuplicateEventException if an event with the same subject and time already exists
   *                                 in target
   */
  int copyEvents(ReadOnlyCalendar sourceCalendar, String eventSubject,
                 String sourceStartDateTime, CalendarInterface targetCalendar,
                 String targetStartDateTime)
      throws EventNotFoundException, InvalidDateTimeException, DuplicateEventException;

  /**
   * Copies all events occurring on a specific date from the source calendar to the target calendar.
   * Events are copied to the corresponding date in the target calendar, preserving
   * their time offsets from the start of the source date.
   *
   * @param sourceCalendar the calendar to copy from
   * @param sourceDate the date to copy events from (format: YYYY-MM-DD)
   * @param targetCalendar the calendar to copy to
   * @param targetDate the date to copy events to (format: YYYY-MM-DD)
   * @return the number of events successfully copied
   * @throws InvalidDateTimeException if date formats are invalid
   * @throws DuplicateEventException if any event already exists in the target calendar
   */
  int copyEvents(ReadOnlyCalendar sourceCalendar, String sourceDate,
                 CalendarInterface targetCalendar, String targetDate)
      throws InvalidDateTimeException, DuplicateEventException;

  /**
   * Copies all events within a date range from the source calendar to the target calendar.
   * Events are copied starting from the target start date, maintaining their relative
   * time offsets from the source start date.
   *
   * @param sourceCalendar the calendar to copy from
   * @param sourceStartDate the start date of the range to copy (format: YYYY-MM-DD, inclusive)
   * @param sourceEndDate the end date of the range to copy (format: YYYY-MM-DD, inclusive)
   * @param targetCalendar the calendar to copy to
   * @param targetStartDate the start date to begin copying to (format: YYYY-MM-DD)
   * @return the number of events successfully copied
   * @throws InvalidDateTimeException if date formats are invalid or start date is after end date
   * @throws DuplicateEventException if any event already exists in the target calendar
   */
  int copyEventsBetween(ReadOnlyCalendar sourceCalendar, String sourceStartDate,
                        String sourceEndDate, CalendarInterface targetCalendar,
                        String targetStartDate)
      throws InvalidDateTimeException, DuplicateEventException;
}