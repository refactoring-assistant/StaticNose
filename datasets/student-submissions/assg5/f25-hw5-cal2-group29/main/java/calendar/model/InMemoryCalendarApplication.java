package calendar.model;

import calendar.model.utils.DateTimeCheck;
import calendar.model.utils.DayOfWeek;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The in-memory implementation of the {@link CalendarApplication} interface.
 *
 * <p>This class acts as the top-level model, managing a collection of named
 * {@link Calendar} objects. It handles the creation, editing, and selection
 * (use) of calendars, as well as all cross-calendar operations like copying events.
 */
public class InMemoryCalendarApplication implements CalendarApplication {

  private final Map<String, Calendar> calendars;
  private Calendar activeCalendar;

  /**
   * Constructs a new, empty calendar application manager.
   */
  public InMemoryCalendarApplication() {
    this.calendars = new HashMap<>();
    this.activeCalendar = null;
  }

  /**
   * {@inheritDoc}
   *
   * @param name     The unique name for the new calendar.
   * @param timezone The IANA timezone string (e.g., "America/New_York").
   * @throws IllegalArgumentException if the name is null, empty, or already exists,
   *                                  or if the timezone string is null or invalid.
   */
  @Override
  public void createCalendar(String name, String timezone) throws IllegalArgumentException {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty.");
    }
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("A calendar with the name '" + name + "' already exists.");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }

    try {
      ZoneId zoneId = ZoneId.of(timezone);
      Calendar newCalendar = new InMemoryCalendar(zoneId);
      calendars.put(name, newCalendar);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid or unsupported timezone: " + timezone, e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Note: Changing the 'timezone' property is a destructive operation that
   * creates a new {@link Calendar} object with the new ZoneId and copies all
   * existing events into it.
   *
   * @param name     The current (unique) name of the calendar to edit.
   * @param property The property to change ("name" or "timezone").
   * @param newValue The new value for the property.
   * @throws IllegalArgumentException if the calendar isn't found, the property
   *                                  is unknown, the new value is invalid
   *                                  (e.g., duplicate name), or the new timezone is invalid.
   */
  @Override
  public void editCalendar(String name, String property, String newValue)
      throws IllegalArgumentException {

    Calendar calendarToEdit = findCalendar(name);

    if (property == null || newValue == null || newValue.trim().isEmpty()) {
      throw new IllegalArgumentException("Property and new value cannot be null or empty.");
    }

    switch (property.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newValue)) {
          throw new IllegalArgumentException(
              "A calendar with the name '" + newValue + "' already exists.");
        }
        calendars.remove(name);
        calendars.put(newValue, calendarToEdit);
        break;
      case "timezone":
        try {
          ZoneId newZoneId = ZoneId.of(newValue);
          ZoneId oldZoneId = calendarToEdit.getZoneId();
          if (newZoneId.equals(oldZoneId)) {
            return;
          }

          Calendar updatedCalendar = new InMemoryCalendar(newZoneId);
          for (EventSingle event : calendarToEdit.getAllEvents()) {
            ZonedDateTime sourceStartInstant = event.getStart().atZone(oldZoneId);
            ZonedDateTime sourceEndInstant = event.getEnd().atZone(oldZoneId);

            LocalDateTime newStart = sourceStartInstant.withZoneSameInstant(newZoneId)
                .toLocalDateTime();
            LocalDateTime newEnd = sourceEndInstant.withZoneSameInstant(newZoneId)
                .toLocalDateTime();

            try {
              DateTimeCheck.validateSingleDayEvent(newStart, newEnd);
            } catch (IllegalArgumentException e) {
              throw new IllegalArgumentException(
                  "Edit failed: Event '" + event.getSubject()
                      + "' would span multiple days in the new timezone.");
            }

            if (event.getStart().toLocalDate().getDayOfWeek()
                != newStart.toLocalDate().getDayOfWeek()) {
              throw new IllegalArgumentException(
                  "Edit failed: Event '" + event.getSubject()
                      + "' would change its day of the week in the new timezone."
                      + " (Series edit not supported)");
            }

            EventSingle newEvent = new EventSingle.Builder(event.getSubject(), newStart)
                .withEnd(newEnd)
                .withDescription(event.getDescription())
                .withLocation(event.getLocation())
                .withStatus(event.getStatus())
                .build();

            updatedCalendar.createSingleEvent(newEvent);
          }

          calendars.put(name, updatedCalendar);

          if (activeCalendar == calendarToEdit) {
            activeCalendar = updatedCalendar;
          }
        } catch (DateTimeException e) {
          throw new IllegalArgumentException(
              "Invalid or unsupported timezone: " + newValue, e);
        }
        break;
      default:
        throw new IllegalArgumentException(
            "Unknown property: '" + property + "'. Can only edit 'name' or 'timezone'.");
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param name The name of the calendar to "use".
   * @throws IllegalArgumentException if the calendar name doesn't exist.
   */
  @Override
  public void useCalendar(String name) throws IllegalArgumentException {
    Calendar calendar = findCalendar(name);
    this.activeCalendar = calendar;
  }

  /**
   * {@inheritDoc}
   *
   * @return The active Calendar instance.
   * @throws IllegalStateException if no calendar is currently in use.
   */
  @Override
  public Calendar getActiveCalendar() throws IllegalStateException {
    if (activeCalendar == null) {
      throw new IllegalStateException(
          "No calendar is currently in use. Use 'use calendar --name <name>' first.");
    }
    return activeCalendar;
  }

  /**
   * {@inheritDoc}
   *
   * @param subject            The subject of the event to copy from the active calendar.
   * @param start              The start time of the event to copy (in active calendar's timezone).
   * @param targetCalendarName The name of the calendar to copy to.
   * @param targetStart        The new start time for the copied event
   *                           (in target calendar's timezone).
   * @throws IllegalArgumentException if the event isn't found, the target calendar
   *                                  doesn't exist, or the copy creates a conflict.
   * @throws IllegalStateException    if no calendar is active.
   */
  @Override
  public void copyEvent(String subject, LocalDateTime start, String targetCalendarName,
                        LocalDateTime targetStart) throws IllegalArgumentException {
    Calendar sourceCalendar = getActiveCalendar();
    Calendar targetCalendar = findCalendar(targetCalendarName);

    EventSingle sourceEvent = findEventByStart(sourceCalendar, subject, start);
    Duration duration = Duration.between(sourceEvent.getStart(), sourceEvent.getEnd());
    LocalDateTime targetEnd = targetStart.plus(duration);

    try {
      DateTimeCheck.validateSingleDayEvent(targetStart, targetEnd);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Copy failed: Copied event would span multiple days in the target timezone.");
    }

    EventSingle newEvent = new EventSingle.Builder(sourceEvent.getSubject(), targetStart)
        .withEnd(targetEnd)
        .withDescription(sourceEvent.getDescription())
        .withLocation(sourceEvent.getLocation())
        .withStatus(sourceEvent.getStatus())
        .build();

    targetCalendar.createSingleEvent(newEvent);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This operation is timezone-aware and series-aware.
   *
   * @param date               The date to copy events from (in active calendar's timezone).
   * @param targetCalendarName The name of the calendar to copy to.
   * @param targetDate         The corresponding start date for the events in the target calendar.
   * @throws IllegalArgumentException if calendars aren't found or a conflict occurs,
   *                                  or if a copied event would span multiple days in the target
   *                                  timezone.
   * @throws IllegalStateException    if no calendar is active.
   */
  @Override
  public void copyEventsOnDate(LocalDate date, String targetCalendarName, LocalDate targetDate)
      throws IllegalArgumentException {
    Calendar sourceCalendar = getActiveCalendar();
    Calendar targetCalendar = findCalendar(targetCalendarName);

    List<EventSingle> eventsToCopy = sourceCalendar.getEventsOn(date);
    if (eventsToCopy.isEmpty()) {
      return;
    }

    Map<UUID, List<EventSingle>> seriesEventsMap = new HashMap<>();
    List<EventSingle> singleEvents = new ArrayList<>();

    if (sourceCalendar instanceof InMemoryCalendar) {
      InMemoryCalendar imSource = (InMemoryCalendar) sourceCalendar;
      for (EventSingle event : eventsToCopy) {
        UUID seriesId = imSource.getSeriesIdForEvent(event);
        if (seriesId != null) {
          seriesEventsMap.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(event);
        } else {
          singleEvents.add(event);
        }
      }
    } else {
      singleEvents.addAll(eventsToCopy);
    }

    for (EventSingle event : singleEvents) {
      copySingleEventWithTimezone(event, targetCalendar, targetDate, date, sourceCalendar);
    }

    if (sourceCalendar instanceof InMemoryCalendar) {
      for (Map.Entry<UUID, List<EventSingle>> entry : seriesEventsMap.entrySet()) {
        processSeriesCopy(
            (InMemoryCalendar) sourceCalendar,
            targetCalendar,
            entry.getKey(),
            entry.getValue(),
            date,
            targetDate
        );
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>This operation is timezone-aware and series-aware.
   *
   * @param startDate          The start of the range (inclusive, in active calendar's timezone).
   * @param endDate            The end of the range (inclusive, in active calendar's timezone).
   * @param targetCalendarName The name of the calendar to copy to.
   * @param targetStartDate    The date in the target calendar that corresponds to the
   *                           startDate of the source range.
   * @throws IllegalArgumentException if calendars aren't found or a conflict occurs,
   *                                  or if a copied event would span multiple days
   *                                  in the target timezone.
   * @throws IllegalStateException    if no calendar is active.
   */
  @Override
  public void copyEventsBetween(LocalDate startDate, LocalDate endDate, String targetCalendarName,
                                LocalDate targetStartDate) throws IllegalArgumentException {
    Calendar sourceCalendar = getActiveCalendar();
    Calendar targetCalendar = findCalendar(targetCalendarName);

    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.plusDays(1).atStartOfDay();
    List<EventSingle> eventsToCopy = sourceCalendar.getEventsInRange(rangeStart, rangeEnd);

    if (eventsToCopy.isEmpty()) {
      return;
    }

    Map<UUID, List<EventSingle>> seriesEventsMap = new HashMap<>();
    List<EventSingle> singleEvents = new ArrayList<>();

    if (sourceCalendar instanceof InMemoryCalendar) {
      InMemoryCalendar imSource = (InMemoryCalendar) sourceCalendar;
      for (EventSingle event : eventsToCopy) {
        UUID seriesId = imSource.getSeriesIdForEvent(event);
        if (seriesId != null) {
          seriesEventsMap.computeIfAbsent(seriesId, k -> new ArrayList<>()).add(event);
        } else {
          singleEvents.add(event);
        }
      }
    } else {
      singleEvents.addAll(eventsToCopy);
    }

    for (EventSingle event : singleEvents) {
      copySingleEventWithTimezone(event, targetCalendar, targetStartDate, startDate,
          sourceCalendar);
    }

    if (sourceCalendar instanceof InMemoryCalendar) {
      for (Map.Entry<UUID, List<EventSingle>> entry : seriesEventsMap.entrySet()) {
        processSeriesCopy(
            (InMemoryCalendar) sourceCalendar,
            targetCalendar,
            entry.getKey(),
            entry.getValue(),
            startDate,
            targetStartDate
        );
      }
    }
  }

  /**
   * Helper method for copyEventsOnDate and copyEventsBetween.
   * Copies a single event, adjusting its time based on timezone and date offset.
   */
  private void copySingleEventWithTimezone(EventSingle event, Calendar targetCalendar,
                                           LocalDate targetRefDate, LocalDate sourceRefDate,
                                           Calendar sourceCalendar) {
    ZoneId sourceZone = sourceCalendar.getZoneId();
    ZoneId targetZone = targetCalendar.getZoneId();

    ZonedDateTime sourceStartInstant = event.getStart().atZone(sourceZone);
    ZonedDateTime sourceEndInstant = event.getEnd().atZone(sourceZone);
    Duration duration = Duration.between(sourceStartInstant, sourceEndInstant);

    Duration dayOffset = Duration.between(
        sourceRefDate.atStartOfDay(), event.getStart().toLocalDate().atStartOfDay());
    LocalDate newEventDate = targetRefDate.plusDays(dayOffset.toDays());

    ZonedDateTime newStartInstant = sourceStartInstant.withZoneSameInstant(targetZone);
    LocalDateTime newStart = LocalDateTime.of(newEventDate, newStartInstant.toLocalTime());
    LocalDateTime newEnd = newStart.plus(duration);

    try {
      DateTimeCheck.validateSingleDayEvent(newStart, newEnd);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Copy failed: Event '" + event.getSubject()
              + "' would span multiple days in the target timezone.");
    }

    EventSingle newEvent = new EventSingle.Builder(event.getSubject(), newStart)
        .withEnd(newEnd)
        .withDescription(event.getDescription())
        .withLocation(event.getLocation())
        .withStatus(event.getStatus())
        .build();
    targetCalendar.createSingleEvent(newEvent);
  }

  /**
   * Helper method to process the copying of a partial (or full) series.
   * This creates a *new* EventSeries in the target calendar.
   */
  private void processSeriesCopy(InMemoryCalendar sourceCalendar, Calendar targetCalendar,
                                 UUID seriesId, List<EventSingle> eventsInSeries,
                                 LocalDate sourceRefDate, LocalDate targetRefDate) {

    EventSeries originalTemplate = sourceCalendar.getSeriesTemplateById(seriesId);
    if (originalTemplate == null) {
      for (EventSingle event : eventsInSeries) {
        copySingleEventWithTimezone(event, targetCalendar, targetRefDate, sourceRefDate,
            sourceCalendar);
      }
      return;
    }

    EventSingle firstEvent = eventsInSeries.stream()
        .min(Comparator.comparing(EventSingle::getStart))
        .orElse(null);
    if (firstEvent == null) {
      return;
    }

    ZoneId sourceZone = sourceCalendar.getZoneId();
    ZoneId targetZone = targetCalendar.getZoneId();

    Duration dayOffset = Duration.between(
        sourceRefDate.atStartOfDay(), firstEvent.getStart().toLocalDate().atStartOfDay());
    LocalDate newSeriesStartDate = targetRefDate.plusDays(dayOffset.toDays());

    LocalDate actualNewSeriesStartDate = findNextValidDay(
        newSeriesStartDate, originalTemplate.getDaysOfWeek());

    LocalTime newStartTime = originalTemplate.getStartTime()
        .atDate(firstEvent.getStart().toLocalDate())
        .atZone(sourceZone)
        .withZoneSameInstant(targetZone)
        .toLocalTime();

    LocalTime newEndTime = originalTemplate.getEndTime()
        .atDate(firstEvent.getStart().toLocalDate())
        .atZone(sourceZone)
        .withZoneSameInstant(targetZone)
        .toLocalTime();

    EventSeries.Builder newTemplateBuilder = new EventSeries.Builder(originalTemplate)
        .withStartDate(actualNewSeriesStartDate)
        .withStartTime(newStartTime)
        .withEndTime(newEndTime)
        .clearEndCondition()
        .forOccurrences(eventsInSeries.size());

    try {
      targetCalendar.createEventSeries(newTemplateBuilder.build());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Copy failed for series '" + originalTemplate.getSubject()
              + "': " + e.getMessage(), e);
    }
  }

  /**
   * Finds a calendar by its unique name.
   *
   * @param name The name of the calendar to find.
   * @return The {@link Calendar} object.
   * @throws IllegalArgumentException if the name is null, empty, or not found.
   */
  private Calendar findCalendar(String name) throws IllegalArgumentException {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty.");
    }
    Calendar calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    return calendar;
  }

  /**
   * Finds a *unique* event in a given calendar by its subject and start time.
   *
   * @param calendar The calendar to search within.
   * @param subject  The subject of the event.
   * @param start    The start time of the event.
   * @return The matching {@link EventSingle}.
   * @throws IllegalArgumentException if no event is found, or if multiple
   *                                  events match the criteria.
   */
  private EventSingle findEventByStart(Calendar calendar, String subject, LocalDateTime start) {
    List<EventSingle> matches = calendar.getAllEvents().stream()
        .filter(e -> e.getSubject().equals(subject) && e.getStart().equals(start))
        .collect(Collectors.toList());

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No event found with matching subject and start time.");
    }
    if (matches.size() > 1) {
      throw new IllegalArgumentException("Multiple events match.");
    }
    return matches.get(0);
  }

  /**
   * Converts a java.time.DayOfWeek to the model's DayOfWeek.
   */
  private calendar.model.utils.DayOfWeek getModelDayOfWeek(java.time.DayOfWeek jdkDay) {
    switch (jdkDay) {
      case MONDAY:
        return calendar.model.utils.DayOfWeek.MONDAY;
      case TUESDAY:
        return calendar.model.utils.DayOfWeek.TUESDAY;
      case WEDNESDAY:
        return calendar.model.utils.DayOfWeek.WEDNESDAY;
      case THURSDAY:
        return calendar.model.utils.DayOfWeek.THURSDAY;
      case FRIDAY:
        return calendar.model.utils.DayOfWeek.FRIDAY;
      case SATURDAY:
        return calendar.model.utils.DayOfWeek.SATURDAY;
      default:
        return calendar.model.utils.DayOfWeek.SUNDAY;
    }
  }

  /**
   * Finds the next valid day of a series on or after a given start date.
   */
  private LocalDate findNextValidDay(LocalDate startDate,
                                     List<calendar.model.utils.DayOfWeek> validDays) {
    LocalDate currentDate = startDate;
    Set<DayOfWeek> validDaySet = EnumSet.copyOf(validDays);

    for (int i = 0; i < 7; i++) {
      java.time.DayOfWeek jdkDay = currentDate.getDayOfWeek();
      calendar.model.utils.DayOfWeek modelDay = getModelDayOfWeek(jdkDay);

      if (validDaySet.contains(modelDay)) {
        return currentDate;
      }
      currentDate = currentDate.plusDays(1);
    }
    return startDate;
  }
}