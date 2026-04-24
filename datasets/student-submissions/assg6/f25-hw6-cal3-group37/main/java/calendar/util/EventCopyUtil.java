package calendar.util;

import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for copying events between calendars with timezone conversion.
 */
public class EventCopyUtil {

  /**
   * Copies a single event to target calendar at specified time.
   *
   * @param sourceEvent the event to copy
   * @param targetCalendar the destination calendar
   * @param targetStartTime the new start time in target timezone
   * @param sourceTimezone source calendar timezone
   * @param targetTimezone target calendar timezone
   */
  public static void copyEvent(CalendarEvent sourceEvent,
                               CalendarModel targetCalendar,
                               LocalDateTime targetStartTime,
                               ZoneId sourceTimezone,
                               ZoneId targetTimezone) {
    // Calculate duration
    long duration = ChronoUnit.MINUTES.between(
        sourceEvent.getStartDateTime(),
        sourceEvent.getEndDateTime()
    );

    // Create new event at target time
    LocalDateTime targetEndTime = targetStartTime.plusMinutes(duration);

    CalendarEvent newEvent = new Event(
        sourceEvent.getSubject(),
        targetStartTime,
        targetEndTime
    );

    // Copy optional fields
    if (sourceEvent.getDescription() != null) {
      newEvent.setDescription(sourceEvent.getDescription());
    }
    if (sourceEvent.getLocation() != null) {
      newEvent.setLocation(sourceEvent.getLocation());
    }
    newEvent.setStatus(sourceEvent.getStatus());

    // Note: seriesId is NOT copied - this is a new independent event

    targetCalendar.addEvent(newEvent);
  }

  /**
   * Copies all events on a specific date to target calendar.
   *
   * @param sourceCalendar source calendar
   * @param targetCalendar target calendar
   * @param sourceDate date in source calendar
   * @param targetDate date in target calendar
   * @param sourceTimezone source timezone
   * @param targetTimezone target timezone
   */
  public static void copyEventsOnDate(CalendarModel sourceCalendar,
                                      CalendarModel targetCalendar,
                                      LocalDate sourceDate,
                                      LocalDate targetDate,
                                      ZoneId sourceTimezone,
                                      ZoneId targetTimezone) {
    List<CalendarEvent> events = sourceCalendar.getEventsOnDate(sourceDate);

    for (CalendarEvent event : events) {
      // Convert time to target timezone
      LocalDateTime sourceStart = event.getStartDateTime();
      LocalDateTime targetStart = convertTime(
          sourceStart,
          sourceDate,
          targetDate,
          sourceTimezone,
          targetTimezone
      );

      copyEvent(event, targetCalendar, targetStart, sourceTimezone, targetTimezone);
    }
  }

  /**
   * Copies all events in a date range to target calendar.
   *
   * @param sourceCalendar source calendar
   * @param targetCalendar target calendar
   * @param startDate start of range (inclusive)
   * @param endDate end of range (inclusive)
   * @param targetStartDate start date in target
   * @param sourceTimezone source timezone
   * @param targetTimezone target timezone
   */
  public static void copyEventsBetween(CalendarModel sourceCalendar,
                                       CalendarModel targetCalendar,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       LocalDate targetStartDate,
                                       ZoneId sourceTimezone,
                                       ZoneId targetTimezone) {
    // Get events in range
    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);
    List<CalendarEvent> events = sourceCalendar.getEventsInRange(rangeStart, rangeEnd);

    // Calculate offset in days
    long dayOffset = ChronoUnit.DAYS.between(startDate, targetStartDate);

    for (CalendarEvent event : events) {
      // Calculate new start date/time
      LocalDateTime newStart = event.getStartDateTime().plusDays(dayOffset);

      // Convert timezone
      newStart = convertTime(
          event.getStartDateTime(),
          event.getStartDateTime().toLocalDate(),
          newStart.toLocalDate(),
          sourceTimezone,
          targetTimezone
      );

      copyEvent(event, targetCalendar, newStart, sourceTimezone, targetTimezone);
    }
  }

  /**
   * Converts time from source to target timezone.
   * Keeps the "wall clock" time the same but adjusts for timezone difference.
   */
  private static LocalDateTime convertTime(LocalDateTime sourceTime,
                                           LocalDate sourceDate,
                                           LocalDate targetDate,
                                           ZoneId sourceTimezone,
                                           ZoneId targetTimezone) {
    // Create zoned datetime in source timezone
    ZonedDateTime sourceZoned = ZonedDateTime.of(sourceTime, sourceTimezone);

    // Convert to target timezone
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetTimezone);

    // Adjust the date if needed
    long dayDiff = ChronoUnit.DAYS.between(sourceDate, targetDate);
    targetZoned = targetZoned.plusDays(dayDiff);

    return targetZoned.toLocalDateTime();
  }
}