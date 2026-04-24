package calendar.model.copy;

import calendar.exceptions.DuplicateEventException;
import calendar.exceptions.EventNotFoundException;
import calendar.exceptions.InvalidDateTimeException;
import calendar.model.calendar.CalendarInterface;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.event.Event;
import calendar.model.event.EventBuilder;
import calendar.model.event.EventInterface;
import calendar.model.util.EventUtils;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for copying events between calendars.
 * Handles single event copy, event series copy, and batch copy operations.
 * Preserves event properties and handles timezone conversions.
 */
public class EventCopyService implements EventCopyInterface {

  /**
   * Copies a single event from source calendar to target calendar.
   * The copied event maintains all properties of the original including
   * description, location, and status. Duration is preserved in the copy.
   *
   * @param sourceCalendar      the calendar containing the event to copy
   * @param eventSubject        the subject of the event to copy
   * @param sourceStartDateTime the start datetime of the event to copy
   * @param targetCalendar      the calendar to copy the event into
   * @param targetStartDateTime the start datetime for the copied event in target calendar
   * @return the number of events copied (always 1 for single event)
   * @throws EventNotFoundException   if the event cannot be found in source calendar
   * @throws InvalidDateTimeException if datetime format is invalid
   * @throws DuplicateEventException  if event already exists in target calendar
   */
  public int copyEvents(ReadOnlyCalendar sourceCalendar, String eventSubject,
                        String sourceStartDateTime, CalendarInterface targetCalendar,
                        String targetStartDateTime)
      throws EventNotFoundException, InvalidDateTimeException, DuplicateEventException {

    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar cannot be null");
    }

    String sourceDate = sourceStartDateTime.substring(0, 10);
    List<EventInterface> eventsOnDate = sourceCalendar.getEvents(sourceDate);

    ZonedDateTime sourceStartParsed = parseDateTime(sourceStartDateTime,
        sourceCalendar.getCalendarTimeZone());

    EventInterface sourceEvent = null;
    for (EventInterface event : eventsOnDate) {
      if (event.getSubject().equals(eventSubject)
          && event.getStartDateTime().equals(sourceStartParsed)) {
        sourceEvent = event;
        break;
      }
    }

    if (sourceEvent == null) {
      throw new EventNotFoundException(
          "Event not found: '" + eventSubject + "' on " + sourceStartDateTime
      );
    }

    ZonedDateTime newStartDateTime =
        parseDateTime(targetStartDateTime, targetCalendar.getCalendarTimeZone());

    copySingleEvent(sourceEvent, targetCalendar, newStartDateTime);

    return 1;
  }

  /**
   * Copies all events from a specific date in source calendar to a target date.
   * All events maintain their relative times and durations. Times are converted
   * to the target calendar's timezone while preserving the actual moment in time.
   *
   * @param sourceCalendar the calendar containing events to copy
   * @param sourceDate     the date to copy events from
   * @param targetCalendar the calendar to copy events into
   * @param targetDate     the date to copy events to
   * @return the number of events copied
   * @throws InvalidDateTimeException if date format is invalid
   * @throws DuplicateEventException  if copying creates duplicate events
   */
  public int copyEvents(ReadOnlyCalendar sourceCalendar, String sourceDate,
                        CalendarInterface targetCalendar, String targetDate)
      throws InvalidDateTimeException, DuplicateEventException {

    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar cannot be null");
    }

    List<EventInterface> eventsOnDate = sourceCalendar.getEvents(sourceDate);

    LocalDate sourceDateParsed = LocalDate.parse(sourceDate);
    LocalDate targetDateParsed = LocalDate.parse(targetDate);
    long dayOffset = ChronoUnit.DAYS.between(sourceDateParsed, targetDateParsed);

    int successCount = 0;
    List<String> skippedDuplicates = new ArrayList<>();

    for (EventInterface event : eventsOnDate) {
      try {
        ZonedDateTime sourceStart = event.getStartDateTime();
        ZonedDateTime targetStart = sourceStart.plusDays(dayOffset)
            .withZoneSameInstant(targetCalendar.getCalendarTimeZone());

        copySingleEvent(event, targetCalendar, targetStart);
        successCount++;
      } catch (DuplicateEventException e) {
        skippedDuplicates.add(event.getSubject() + " at " + event.getStartDateTime());
      }
    }

    if (!skippedDuplicates.isEmpty()) {
      throw new DuplicateEventException(
          "Copied " + successCount + " events. Skipped duplicates: "
              + String.join(", ", skippedDuplicates)
      );
    }

    return successCount;
  }

  /**
   * Copies all events within a date range from source calendar to target calendar.
   * Events are copied starting from the target date while maintaining relative
   * date offsets. Series relationships are preserved with new series IDs generated
   * for the target calendar.
   *
   * @param sourceCalendar  the calendar containing events to copy
   * @param sourceStartDate the start date of the range (inclusive)
   * @param sourceEndDate   the end date of the range (inclusive)
   * @param targetCalendar  the calendar to copy events into
   * @param targetStartDate the date to start copying events to
   * @return the number of events copied
   * @throws InvalidDateTimeException if date format is invalid
   * @throws DuplicateEventException  if copying creates duplicate events
   */
  public int copyEventsBetween(ReadOnlyCalendar sourceCalendar, String sourceStartDate,
                               String sourceEndDate, CalendarInterface targetCalendar,
                               String targetStartDate)
      throws InvalidDateTimeException, DuplicateEventException {

    if (targetCalendar == null) {
      throw new IllegalArgumentException("Target calendar cannot be null");
    }

    validateDateRange(sourceStartDate, sourceEndDate);

    String sourceStartDateTime = sourceStartDate + "T00:00";
    String sourceEndDateTime = sourceEndDate + "T23:59";

    List<EventInterface> eventsInRange = sourceCalendar.getEvents(sourceStartDateTime,
        sourceEndDateTime);

    long dayOffset = calculateDayOffset(sourceStartDate, targetStartDate);
    Map<String, String> seriesIdMapping = new HashMap<>();
    int successCount = 0;
    List<String> skippedDuplicates = new ArrayList<>();
    List<String> skippedMultiDay = new ArrayList<>();

    for (EventInterface event : eventsInRange) {
      try {
        copyEventWithSeriesSupport(event, targetCalendar, dayOffset, seriesIdMapping);
        successCount++;
      } catch (DuplicateEventException e) {
        skippedDuplicates.add(event.getSubject() + " at " + event.getStartDateTime());
      } catch (IllegalArgumentException e) {
        skippedMultiDay.add(event.getSubject() + " at " + event.getStartDateTime());
      }
    }

    if (!skippedDuplicates.isEmpty() || !skippedMultiDay.isEmpty()) {
      StringBuilder message = new StringBuilder();
      message.append("Copied ").append(successCount).append(" events. ");

      if (!skippedDuplicates.isEmpty()) {
        message.append("Skipped duplicates: ")
                .append(String.join(", ", skippedDuplicates)).append(". ");
      }

      if (!skippedMultiDay.isEmpty()) {
        message.append("Skipped multi-day series events: ")
            .append(String.join(", ", skippedMultiDay)).append(".");
      }

      throw new DuplicateEventException(message.toString());
    }

    return successCount;
  }

  /**
   * Validates that start date is not after end date.
   *
   * @param startDate the start date string
   * @param endDate   the end date string
   * @throws IllegalArgumentException if start date is after end date
   */
  private void validateDateRange(String startDate, String endDate) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);

    if (start.isAfter(end)) {
      throw new IllegalArgumentException(
          "Source start date must be before or equal to end date. Start: "
              + startDate + ", End: " + endDate
      );
    }
  }

  /**
   * Calculates the number of days between source and target dates.
   *
   * @param sourceStartDate the source start date
   * @param targetStartDate the target start date
   * @return the number of days offset
   */
  private long calculateDayOffset(String sourceStartDate, String targetStartDate) {
    LocalDate sourceStart = LocalDate.parse(sourceStartDate);
    LocalDate targetStart = LocalDate.parse(targetStartDate);
    return ChronoUnit.DAYS.between(sourceStart, targetStart);
  }

  /**
   * Copies a single event with series support to target calendar.
   * Handles timezone conversion, validates single-day constraint for series events,
   * and assigns appropriate series IDs.
   *
   * @param event           the event to copy
   * @param targetCalendar  the target calendar
   * @param dayOffset       the number of days to offset
   * @param seriesIdMapping mapping of old series IDs to new series IDs
   * @throws DuplicateEventException  if event already exists in target
   * @throws InvalidDateTimeException if datetime operations fail
   */
  private void copyEventWithSeriesSupport(EventInterface event, CalendarInterface targetCalendar,
                                          long dayOffset, Map<String, String> seriesIdMapping)
      throws DuplicateEventException, InvalidDateTimeException {

    ZonedDateTime targetStart = event.getStartDateTime()
        .plusDays(dayOffset)
        .withZoneSameInstant(targetCalendar.getCalendarTimeZone());

    Duration eventDuration = Duration.between(
        event.getStartDateTime(),
        event.getEndDateTime()
    );
    ZonedDateTime targetEnd = targetStart.plus(eventDuration);

    validateSeriesConstraint(event, targetStart, targetEnd);

    EventInterface copiedEvent = buildCopiedEvent(event, targetCalendar, targetStart, targetEnd);

    assignSeriesId(copiedEvent, event.getSeriesId(), seriesIdMapping);

    targetCalendar.storeEvents(List.of(copiedEvent));
  }

  /**
   * Validates that series events remain single-day after timezone conversion.
   *
   * @param event       the event to validate
   * @param targetStart the target start datetime
   * @param targetEnd   the target end datetime
   * @throws IllegalArgumentException if series event would span multiple days
   */
  private void validateSeriesConstraint(EventInterface event, ZonedDateTime targetStart,
                                        ZonedDateTime targetEnd) {
    if (event.getSeriesId() != null) {
      EventUtils.validateSeriesEventSingleDay(
          event.getSubject(),
          targetStart,
          targetEnd,
          "copy operation"
      );
    }
  }

  /**
   * Builds a copied event with all properties from the source event.
   *
   * @param sourceEvent    the source event
   * @param targetCalendar the target calendar
   * @param targetStart    the target start datetime
   * @param targetEnd      the target end datetime
   * @return the built event (not yet stored)
   * @throws InvalidDateTimeException if datetime operations fail
   */
  private EventInterface buildCopiedEvent(EventInterface sourceEvent,
                                          CalendarInterface targetCalendar,
                                          ZonedDateTime targetStart, ZonedDateTime targetEnd)
      throws InvalidDateTimeException {

    EventBuilder builder = targetCalendar.newEvent(sourceEvent.getSubject(),
            formatDateTime(targetStart))
        .end(formatDateTime(targetEnd))
        .description(sourceEvent.getDescription())
        .location(sourceEvent.getLocation())
        .status(sourceEvent.getStatus());

    List<EventInterface> builtEvents = builder.build();
    return builtEvents.get(0);
  }

  /**
   * Assigns series ID to copied event if source was part of a series.
   * Generates new series ID or reuses existing mapping.
   *
   * @param copiedEvent     the copied event
   * @param oldSeriesId     the original series ID (may be null)
   * @param seriesIdMapping the mapping of old to new series IDs
   */
  private void assignSeriesId(EventInterface copiedEvent, String oldSeriesId,
                              Map<String, String> seriesIdMapping) {
    if (oldSeriesId != null) {
      String newSeriesId;
      if (seriesIdMapping.containsKey(oldSeriesId)) {
        newSeriesId = seriesIdMapping.get(oldSeriesId);
      } else {
        newSeriesId = UUID.randomUUID().toString();
        seriesIdMapping.put(oldSeriesId, newSeriesId);
      }
      copiedEvent.setSeriesId(newSeriesId);
    }
  }

  /**
   * Copies a single event to the target calendar at the specified datetime.
   * Preserves all event properties including description, location, and status.
   *
   * @param sourceEvent         the event to copy
   * @param targetCalendar      the calendar to copy into
   * @param targetStartDateTime the start datetime for the copied event
   * @throws DuplicateEventException  if event already exists in target
   * @throws InvalidDateTimeException if datetime operations fail
   */
  private void copySingleEvent(EventInterface sourceEvent,
                               CalendarInterface targetCalendar,
                               ZonedDateTime targetStartDateTime)
      throws DuplicateEventException, InvalidDateTimeException {
    Duration eventDuration = Duration.between(
        sourceEvent.getStartDateTime(),
        sourceEvent.getEndDateTime()
    );

    ZonedDateTime newEndDateTime = targetStartDateTime.plus(eventDuration);

    targetCalendar.newEvent(sourceEvent.getSubject(), formatDateTime(targetStartDateTime))
        .end(formatDateTime(newEndDateTime))
        .description(sourceEvent.getDescription())
        .location(sourceEvent.getLocation())
        .status(sourceEvent.getStatus())
        .create(targetCalendar);
  }

  /**
   * Parses a datetime string and applies the specified timezone.
   *
   * @param dateTimeStr the datetime string in ISO format
   * @param timezone    the timezone to apply
   * @return ZonedDateTime with the specified timezone
   */
  private ZonedDateTime parseDateTime(String dateTimeStr, ZoneId timezone) {
    LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr);
    return localDateTime.atZone(timezone);
  }

  /**
   * Formats a ZonedDateTime to ISO local datetime string format.
   *
   * @param dateTime the ZonedDateTime to format
   * @return formatted datetime string
   */
  private String formatDateTime(ZonedDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }
}