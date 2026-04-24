package calendar.util;

import calendar.model.InEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility for timezone conversion operations.
 */
public final class TimezoneUtil {

  private TimezoneUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Copies an event and adjusts its times for timezone conversion and date offset.
   *
   * @param sourceEvent the event to copy
   * @param sourceTimezone timezone of source calendar
   * @param targetTimezone timezone of target calendar
   * @param daysOffset number of days to offset
   * @return copied event with adjusted times
   */
  public static InEvent copyEventWithTimezoneConversion(
      InEvent sourceEvent,
      ZoneId sourceTimezone,
      ZoneId targetTimezone,
      long daysOffset) {

    InEvent copiedEvent = sourceEvent.copy();

    ZonedDateTime sourceStartZoned = sourceEvent.getStartDateTime().atZone(sourceTimezone);
    ZonedDateTime sourceEndZoned = sourceEvent.getEndDateTime().atZone(sourceTimezone);

    ZonedDateTime targetStartZoned = sourceStartZoned.withZoneSameInstant(targetTimezone);
    ZonedDateTime targetEndZoned = sourceEndZoned.withZoneSameInstant(targetTimezone);

    LocalDateTime newStart = targetStartZoned.toLocalDateTime().plusDays(daysOffset);
    LocalDateTime newEnd = targetEndZoned.toLocalDateTime().plusDays(daysOffset);

    setEventTimes(copiedEvent, newStart, newEnd);
    return copiedEvent;
  }

  /**
   * Copies an event to a specific target datetime without timezone conversion.
   * Preserves the duration of the original event.
   *
   * @param sourceEvent the event to copy
   * @param targetDateTime the exact start time for the copied event
   * @return copied event with adjusted times
   */
  public static InEvent copyEventToExactDateTime(
      InEvent sourceEvent,
      LocalDateTime targetDateTime) {

    InEvent copiedEvent = sourceEvent.copy();

    long durationMinutes = java.time.Duration.between(
        sourceEvent.getStartDateTime(),
        sourceEvent.getEndDateTime()
    ).toMinutes();

    LocalDateTime newStart = targetDateTime;
    LocalDateTime newEnd = targetDateTime.plusMinutes(durationMinutes);

    setEventTimes(copiedEvent, newStart, newEnd);
    return copiedEvent;
  }

  /**
   * Sets event times in the correct order to avoid validation errors.
   * If new start is after current end, sets end first, then start.
   */
  private static void setEventTimes(InEvent event, LocalDateTime newStart, LocalDateTime newEnd) {
    if (newStart.isAfter(event.getEndDateTime())) {
      event.setEndDateTime(newEnd);
      event.setStartDateTime(newStart);
    } else {
      event.setStartDateTime(newStart);
      event.setEndDateTime(newEnd);
    }
  }
}