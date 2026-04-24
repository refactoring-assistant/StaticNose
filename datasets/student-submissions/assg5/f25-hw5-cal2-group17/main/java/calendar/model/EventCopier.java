package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles copying events between calendars with timezone conversion.
 */
public class EventCopier {

  /**
   * Copies a single event from source calendar to target calendar.
   *
   * @param sourceCalendar Source calendar
   * @param targetCalendar Target calendar
   * @param eventSubject   Subject of event to copy
   * @param eventStart     Start time of event to copy (in source timezone)
   * @param targetStart    New start time in target calendar (in target timezone)
   * @throws IllegalArgumentException if event not found or copy would create duplicate
   */
  public void copyEvent(Calendar sourceCalendar, Calendar targetCalendar,
                        String eventSubject, LocalDateTime eventStart,
                        LocalDateTime targetStart) {
    // Find the source event
    List<Event> matches = sourceCalendar.findEvents(eventSubject, eventStart);
    if (matches.isEmpty()) {
      throw new IllegalArgumentException(
          "No event found with subject '" + eventSubject + "' at " + eventStart);
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple events found. Cannot copy.");
    }

    Event sourceEvent = matches.get(0);

    // Calculate duration
    Duration duration = null;
    if (sourceEvent.getEndDateTime() != null) {
      duration = Duration.between(
          sourceEvent.getStartDateTime(),
          sourceEvent.getEndDateTime());
    }

    // Create new event in target calendar
    LocalDateTime targetEnd = null;
    if (duration != null) {
      targetEnd = targetStart.plus(duration);
    }

    Event newEvent = new EventImpl(sourceEvent.getSubject(), targetStart, targetEnd);
    newEvent.setDescription(sourceEvent.getDescription());
    newEvent.setLocation(sourceEvent.getLocation());
    newEvent.setStatus(sourceEvent.isPrivate() ? "private" : "public");

    // If part of a series, preserve series ID for now
    // (will be reassigned if copying multiple events from same series)
    if (sourceEvent.getSeriesId() != null) {
      newEvent.setSeriesId(sourceEvent.getSeriesId());
    }

    // Add to target calendar (will throw if duplicate)
    targetCalendar.addEvent(newEvent);
  }

  /**
   * Copies all events on a specific date from source to target calendar.
   * Times are converted between timezones.
   *
   * @param sourceCalendar Source calendar
   * @param targetCalendar Target calendar
   * @param sourceDate     Date in source calendar
   * @param targetDate     Date in target calendar (times will be adjusted)
   * @throws IllegalArgumentException if any copy would create duplicate
   */
  public void copyEventsOnDate(Calendar sourceCalendar, Calendar targetCalendar,
                               LocalDate sourceDate, LocalDate targetDate) {
    List<Event> eventsOnDate = sourceCalendar.getEventsOnDate(sourceDate);

    if (eventsOnDate.isEmpty()) {
      throw new IllegalArgumentException("No events found on " + sourceDate);
    }

    // Track series IDs for reassignment
    Map<String, String> seriesIdMap = new HashMap<>();
    int newSeriesCounter = 0;

    for (Event sourceEvent : eventsOnDate) {
      // Convert source time to target timezone
      LocalDateTime sourceStart = sourceEvent.getStartDateTime();
      LocalDateTime targetStart = convertDateTime(
          sourceStart, sourceCalendar, targetCalendar, sourceDate, targetDate);

      LocalDateTime targetEnd = null;
      if (sourceEvent.getEndDateTime() != null) {
        targetEnd = convertDateTime(
            sourceEvent.getEndDateTime(), sourceCalendar, targetCalendar,
            sourceDate, targetDate);
      }

      // Create new event
      Event newEvent = new EventImpl(sourceEvent.getSubject(), targetStart, targetEnd);
      newEvent.setDescription(sourceEvent.getDescription());
      newEvent.setLocation(sourceEvent.getLocation());
      newEvent.setStatus(sourceEvent.isPrivate() ? "private" : "public");

      // Handle series ID
      if (sourceEvent.getSeriesId() != null) {
        String oldSeriesId = sourceEvent.getSeriesId();
        String newSeriesId = seriesIdMap.get(oldSeriesId);
        if (newSeriesId == null) {
          newSeriesId = "copied-series-" + (++newSeriesCounter);
          seriesIdMap.put(oldSeriesId, newSeriesId);
        }
        newEvent.setSeriesId(newSeriesId);
      }

      targetCalendar.addEvent(newEvent);
    }
  }

  /**
   * Copies all events in a date range from source to target calendar.
   * Preserves relative timing and series relationships.
   *
   * @param sourceCalendar  Source calendar
   * @param targetCalendar  Target calendar
   * @param startDate       Start of range (inclusive)
   * @param endDate         End of range (inclusive)
   * @param targetStartDate Start date in target calendar
   * @throws IllegalArgumentException if any copy would create duplicate
   */
  public void copyEventsInRange(Calendar sourceCalendar, Calendar targetCalendar,
                                LocalDate startDate, LocalDate endDate,
                                LocalDate targetStartDate) {
    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);

    List<Event> eventsInRange = sourceCalendar.getEventsInRange(rangeStart, rangeEnd);

    if (eventsInRange.isEmpty()) {
      throw new IllegalArgumentException(
          "No events found between " + startDate + " and " + endDate);
    }

    // Calculate the time offset
    long dayOffset = Duration.between(
        startDate.atStartOfDay(),
        targetStartDate.atStartOfDay()).toDays();

    // Track series IDs for reassignment
    Map<String, String> seriesIdMap = new HashMap<>();
    int newSeriesCounter = 0;

    for (Event sourceEvent : eventsInRange) {
      // Calculate new start time preserving relative offset
      LocalDateTime sourceStart = sourceEvent.getStartDateTime();
      LocalDateTime targetStart = sourceStart.plusDays(dayOffset);

      // Convert timezone if calendars have different timezones
      if (!sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
        targetStart = sourceCalendar.convertToTimezone(
            sourceStart, targetCalendar.getTimezone());
        // Adjust for the day offset
        long actualDayDiff = Duration.between(
            sourceStart.toLocalDate().atStartOfDay(),
            targetStart.toLocalDate().atStartOfDay()).toDays();
        targetStart = targetStart.plusDays(dayOffset - actualDayDiff);
      }

      LocalDateTime targetEnd = null;
      if (sourceEvent.getEndDateTime() != null) {
        Duration duration = Duration.between(sourceStart, sourceEvent.getEndDateTime());
        targetEnd = targetStart.plus(duration);
      }

      // Create new event
      Event newEvent = new EventImpl(sourceEvent.getSubject(), targetStart, targetEnd);
      newEvent.setDescription(sourceEvent.getDescription());
      newEvent.setLocation(sourceEvent.getLocation());
      newEvent.setStatus(sourceEvent.isPrivate() ? "private" : "public");

      // Handle series ID
      if (sourceEvent.getSeriesId() != null) {
        String oldSeriesId = sourceEvent.getSeriesId();
        String newSeriesId = seriesIdMap.get(oldSeriesId);
        if (newSeriesId == null) {
          newSeriesId = "copied-series-" + (++newSeriesCounter);
          seriesIdMap.put(oldSeriesId, newSeriesId);
        }
        newEvent.setSeriesId(newSeriesId);
      }

      targetCalendar.addEvent(newEvent);
    }
  }

  /**
   * Converts a datetime from source calendar's timezone to target calendar's timezone,
   * preserving the relative time within the day.
   */
  private LocalDateTime convertDateTime(LocalDateTime sourceDateTime,
                                        Calendar sourceCalendar,
                                        Calendar targetCalendar,
                                        LocalDate sourceDate,
                                        LocalDate targetDate) {
    // If timezones are the same, just adjust the date
    if (sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
      long dayDiff = Duration.between(
          sourceDate.atStartOfDay(),
          targetDate.atStartOfDay()).toDays();
      return sourceDateTime.plusDays(dayDiff);
    }

    // Convert through timezone
    LocalDateTime converted = sourceCalendar.convertToTimezone(
        sourceDateTime, targetCalendar.getTimezone());

    // Adjust to target date
    long dayDiff = Duration.between(
        sourceDate.atStartOfDay(),
        targetDate.atStartOfDay()).toDays();
    long actualDayDiff = Duration.between(
        sourceDateTime.toLocalDate().atStartOfDay(),
        converted.toLocalDate().atStartOfDay()).toDays();

    return converted.plusDays(dayDiff - actualDayDiff);
  }
}