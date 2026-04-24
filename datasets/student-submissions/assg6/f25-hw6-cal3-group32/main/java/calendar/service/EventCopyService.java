package calendar.service;

import calendar.model.Calendar;
import calendar.model.Event;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service for copying events within and across calendars.
 */
public class EventCopyService {

  /**
   * Copies a single event to a target calendar at a specified time.
   *
   * @param sourceEvent     the event to copy
   * @param targetCalendar  the destination calendar
   * @param targetStartTime the start time in target calendar
   * @throws IllegalArgumentException if any parameter is null or copy creates duplicate
   */
  public void copyEvent(Event sourceEvent,
                        Calendar targetCalendar,
                        ZonedDateTime targetStartTime) {
    if (sourceEvent == null) {
      throw new IllegalArgumentException("Source event cannot be null");
    }
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar cannot be null");
    }
    if (targetStartTime == null) {
      throw new IllegalArgumentException("Target start time cannot be null");
    }
    Duration duration = sourceEvent.getDuration();
    ZonedDateTime targetStart = targetStartTime.withZoneSameInstant(
        targetCalendar.getTimezone());
    ZonedDateTime targetEnd = targetStart.plus(duration);
    Event copiedEvent = new Event.Builder(sourceEvent.getSubject(), targetStart)
        .end(targetEnd)
        .description(sourceEvent.getDescription())
        .location(sourceEvent.getLocation())
        .isPublic(sourceEvent.isPublic())
        .seriesId(sourceEvent.getSeriesId())
        .build();
    targetCalendar.addEvent(copiedEvent);
  }

  /**
   * Copies all events on a specific date to target calendar.
   *
   * @param sourceCalendar the source calendar
   * @param sourceDate     the date to copy events from
   * @param targetCalendar the destination calendar
   * @param targetDate     the date to copy events to
   * @throws IllegalArgumentException if any parameter is null
   */
  public void copyEventsOnDate(Calendar sourceCalendar,
                               LocalDate sourceDate,
                               Calendar targetCalendar,
                               LocalDate targetDate) {
    if (sourceCalendar == null) {
      throw new IllegalArgumentException("Source calendar cannot be null");
    }
    if (sourceDate == null) {
      throw new IllegalArgumentException("Source date cannot be null");
    }
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar cannot be null");
    }
    if (targetDate == null) {
      throw new IllegalArgumentException("Target date cannot be null");
    }
    List<Event> eventsOnDate = sourceCalendar.getEventsOn(sourceDate);
    for (Event event : eventsOnDate) {
      LocalTime eventTime = event.getStart().toLocalTime();
      ZonedDateTime targetStart = ZonedDateTime.of(
          targetDate,
          eventTime,
          targetCalendar.getTimezone()
      );
      copyEvent(event, targetCalendar, targetStart);
    }
  }

  /**
   * Copies all events in a date range to target calendar.
   *
   * @param sourceCalendar  the source calendar
   * @param startDate       the start of range (inclusive)
   * @param endDate         the end of range (inclusive)
   * @param targetCalendar  the destination calendar
   * @param targetStartDate the start date in target calendar
   * @throws IllegalArgumentException if any parameter is null
   */
  public void copyEventsBetween(Calendar sourceCalendar,
                                LocalDate startDate,
                                LocalDate endDate,
                                Calendar targetCalendar,
                                LocalDate targetStartDate) {
    if (sourceCalendar == null) {
      throw new IllegalArgumentException("Source calendar cannot be null");
    }
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date cannot be null");
    }
    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar cannot be null");
    }
    if (targetStartDate == null) {
      throw new IllegalArgumentException("Target start date cannot be null");
    }

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }
    ZonedDateTime rangeStart = startDate.atStartOfDay(sourceCalendar.getTimezone());
    ZonedDateTime rangeEnd = endDate.plusDays(1).atStartOfDay(sourceCalendar.getTimezone());
    List<Event> eventsInRange = sourceCalendar.getEventsBetween(rangeStart, rangeEnd);
    long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(startDate, targetStartDate);
    for (Event event : eventsInRange) {
      LocalDate eventDate = event.getStart().toLocalDate();
      LocalDate targetDate = eventDate.plusDays(dayOffset);
      LocalTime eventTime = event.getStart().toLocalTime();
      ZonedDateTime targetStart = ZonedDateTime.of(
          targetDate,
          eventTime,
          targetCalendar.getTimezone()
      );
      copyEvent(event, targetCalendar, targetStart);
    }
  }
}