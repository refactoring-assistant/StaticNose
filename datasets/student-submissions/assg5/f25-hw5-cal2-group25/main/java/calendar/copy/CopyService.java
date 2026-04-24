package calendar.copy;

import calendar.model.CalendarModel;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Defines operations for copying events between calendar models.
 * Provides support for copying a single event, all events on a date,
 * or all events within a date range.
 */
public interface CopyService {

  /**
   * Copies a single event—identified by subject and start time—from the source
   * calendar to the destination calendar at a new start time.
   *
   * @param src      the source calendar model
   * @param subject  the subject of the event to copy
   * @param srcStart the original event’s start date and time
   * @param dst      the destination calendar model
   * @param dstStart the new start date and time for the copied event
   */
  void copyEvent(CalendarModel src, String subject, LocalDateTime srcStart,
                 CalendarModel dst, LocalDateTime dstStart);


  /**
   * Copies all events occurring on a specific date from the source calendar
   * to the destination calendar, aligned relative to the provided destination date.
   *
   * @param src          the source calendar model
   * @param srcDate      the date whose events should be copied
   * @param dst          the destination calendar model
   * @param dstStartDate the date to anchor copied events to in the destination
   */
  void copyEventsOnDate(CalendarModel src, LocalDate srcDate,
                        CalendarModel dst, LocalDate dstStartDate);

  /**
   * Copies all events within the given date range (inclusive) from the source
   * calendar to the destination, shifting them relative to the destination's start date.
   *
   * @param src          the source calendar model
   * @param fromDate     the start date of the range to copy
   * @param toDate       the end date of the range to copy
   * @param dst          the destination calendar model
   * @param dstStartDate the date to anchor the copied sequence in the destination
   */
  void copyEventsBetween(CalendarModel src, LocalDate fromDate, LocalDate toDate,
                         CalendarModel dst, LocalDate dstStartDate);
}