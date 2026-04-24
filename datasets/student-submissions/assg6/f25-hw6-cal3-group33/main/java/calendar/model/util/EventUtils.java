package calendar.model.util;

import calendar.model.event.EventInterface;
import java.time.ZonedDateTime;

/**
 * Utility methods for working with calendar events.
 */
public final class EventUtils {


  /**
   * Checks if an event is an all-day event (8:00 AM to 5:00 PM on same day).
   *
   * @param event the event to check
   * @return true if all-day event, false otherwise
   */
  public static boolean isAllDayEvent(EventInterface event) {
    ZonedDateTime start = event.getStartDateTime();
    ZonedDateTime end = event.getEndDateTime();

    if (!start.toLocalDate().equals(end.toLocalDate())) {
      return false;
    }
    if (start.getHour() != 8 || start.getMinute() != 0) {
      return false;
    }
    if (end.getHour() != 17 || end.getMinute() != 0) {
      return false;
    }
    return true;
  }

  /**
   * Validates that a series event remains single-day.
   *
   * <p>Series events must start and end on the same date. This constraint
   * is enforced when events are created, but must also be checked during
   * timezone conversions and copy operations where times may shift.
   *
   * @param eventSubject the subject of the event (for error message)
   * @param startDateTime the start datetime
   * @param endDateTime the end datetime
   * @param context additional context for error message
   *                (e.g., "timezone conversion", "copy operation")
   * @throws IllegalArgumentException if event would span multiple days
   */
  public static void validateSeriesEventSingleDay(String eventSubject,
                                                  ZonedDateTime startDateTime,
                                                  ZonedDateTime endDateTime,
                                                  String context) {
    if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new IllegalArgumentException(
          "Series event '" + eventSubject + "' would span multiple days after " + context + ". "
              + "Start: " + startDateTime + " (" + startDateTime.toLocalDate() + "), "
              + "End: " + endDateTime + " (" + endDateTime.toLocalDate() + "). "
              + "Series events must remain single-day."
      );
    }
  }
}