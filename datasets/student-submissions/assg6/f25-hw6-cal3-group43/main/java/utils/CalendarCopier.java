package utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import model.Calendar;
import model.Event;
import model.EventSeries;

/**
 * Helper utilities for copying events between calendars.
 */
public final class CalendarCopier {

  /**
   * Utility class; prevent instantiation.
   */
  private CalendarCopier() {
  }

  /**
   * Copies a single event identified by subject and start time into the target calendar.
   *
   * @param source      source calendar
   * @param target      destination calendar
   * @param subject     subject identifier
   * @param sourceStart start timestamp in the source calendar
   * @param targetStart desired start timestamp in the target calendar
   * @return number of events copied (0 or 1)
   */
  public static int copySingleEvent(Calendar source,
                                    Calendar target,
                                    String subject,
                                    LocalDateTime sourceStart,
                                    LocalDateTime targetStart) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(subject, "subject");
    Objects.requireNonNull(sourceStart, "sourceStart");
    Objects.requireNonNull(targetStart, "targetStart");

    Event event = source.findExactEvent(subject, sourceStart);
    if (event == null) {
      return 0;
    }
    Duration duration = Duration.between(event.getStartTime(), event.getEndTime());
    LocalDateTime targetEnd = targetStart.plus(duration);
    Event clone = duplicateEvent(event, targetStart, targetEnd);
    target.addEvent(clone);
    copySeriesMembership(event, clone, target, new HashMap<>());
    return 1;
  }

  /**
   * Copies all events that occur on a date from the source calendar to the same relative date.
   *
   * @param source     source calendar
   * @param target     destination calendar
   * @param sourceDate date to copy from in the source calendar
   * @param targetDate anchor date in the target calendar
   * @return number of events copied
   */
  public static int copyEventsOn(Calendar source,
                                 Calendar target,
                                 LocalDate sourceDate,
                                 LocalDate targetDate) {
    List<Event> matches = new ArrayList<>();
    for (Event event : source.getEvents()) {
      if (occursOnDate(event, sourceDate)) {
        matches.add(event);
      }
    }
    if (matches.isEmpty()) {
      return 0;
    }
    Map<EventSeries, EventSeries> seriesMap = new HashMap<>();
    ZoneId sourceZone = source.getTimezone();
    ZoneId targetZone = target.getTimezone();
    for (Event event : matches) {
      LocalDateTime newStart = convertAndShift(event.getStartTime(),
          sourceZone, targetZone, sourceDate, targetDate);
      LocalDateTime newEnd = convertAndShift(event.getEndTime(),
          sourceZone, targetZone, sourceDate, targetDate);
      Event clone = duplicateEvent(event, newStart, newEnd);
      target.addEvent(clone);
      copySeriesMembership(event, clone, target, seriesMap);
    }
    return matches.size();
  }

  private static boolean occursOnDate(Event event, LocalDate date) {
    return event.onDate(date);
  }

  /**
   * Copies every event that intersects a date window into the target calendar, aligned by date.
   *
   * @param source          source calendar
   * @param target          destination calendar
   * @param rangeStart      inclusive start date of the source window
   * @param rangeEnd        inclusive end date of the source window
   * @param targetStartDate date in the target calendar that corresponds to {@code rangeStart}
   * @return number of events copied
   */
  public static int copyEventsInRange(Calendar source,
                                      Calendar target,
                                      LocalDate rangeStart,
                                      LocalDate rangeEnd,
                                      LocalDate targetStartDate) {
    LocalDateTime windowStart = rangeStart.atStartOfDay();
    LocalDateTime windowEnd = rangeEnd.plusDays(1).atStartOfDay();
    List<Event> matches = new ArrayList<>();
    for (Event event : source.getEvents()) {
      LocalDateTime start = event.getStartTime();
      LocalDateTime end = event.getEndTime();
      if (end.isAfter(windowStart) && start.isBefore(windowEnd)) {
        matches.add(event);
      }
    }
    if (matches.isEmpty()) {
      return 0;
    }
    Map<EventSeries, EventSeries> seriesMap = new HashMap<>();
    ZoneId sourceZone = source.getTimezone();
    ZoneId targetZone = target.getTimezone();
    for (Event event : matches) {
      LocalDateTime newStart = convertAndShift(event.getStartTime(),
          sourceZone, targetZone, rangeStart, targetStartDate);
      LocalDateTime newEnd = convertAndShift(event.getEndTime(),
          sourceZone, targetZone, rangeStart, targetStartDate);
      Event clone = duplicateEvent(event, newStart, newEnd);
      target.addEvent(clone);
      copySeriesMembership(event, clone, target, seriesMap);
    }
    return matches.size();
  }

  private static LocalDateTime convertAndShift(LocalDateTime original,
                                               ZoneId sourceZone,
                                               ZoneId targetZone,
                                               LocalDate sourceReference,
                                               LocalDate targetReference) {
    ZonedDateTime converted = original.atZone(sourceZone).withZoneSameInstant(targetZone);
    long dayOffset = ChronoUnit.DAYS.between(sourceReference, original.toLocalDate());
    LocalDate rebasedDate = targetReference.plusDays(dayOffset);
    return LocalDateTime.of(rebasedDate, converted.toLocalTime());
  }

  private static Event duplicateEvent(Event source,
                                      LocalDateTime newStart,
                                      LocalDateTime newEnd) {
    return source.duplicateWithTimes(newStart, newEnd);
  }

  private static void copySeriesMembership(Event source,
                                           Event clone,
                                           Calendar target,
                                           Map<EventSeries, EventSeries> cache) {
    EventSeries sourceSeries = source.getEventSeries();
    if (sourceSeries == null) {
      return;
    }
    EventSeries targetSeries =
        cache.computeIfAbsent(sourceSeries, ignored -> target.createSeries());
    targetSeries.addEvent(clone);
  }
}
