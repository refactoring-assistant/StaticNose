package multicalendarmodel;

import calendarmodel.CalendarModel;
import calendarmodel.CalendarModelImpl;
import calendarmodel.Event;
import calendarmodel.enums.EditMode;
import calendarmodel.enums.Location;
import calendarmodel.exceptions.AmbiguousEditException;
import calendarmodel.exceptions.DuplicateEventException;
import calendarmodel.exceptions.EventNotFoundException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ZonedCalendarModel} interface.
 *
 * <p>This class wraps an instance of the original {@link CalendarModelImpl}
 * and acts as a time zone-aware decorator. It stores a {@link ZoneId} and
 * handles all conversions between the calendar's local time and the
 * underlying storage, which is based on UTC-relative {@link LocalDateTime}
 * objects (the "Type Lie" pattern).</p>
 *
 * <p>All methods from the original {@link CalendarModel} are overridden to
 * be time zone-aware.</p>
 */
public class ZonedCalendarModelImpl implements ZonedCalendarModel {

  /**
   * The underlying storage, which must be treated as storing
   * all {@link LocalDateTime} objects in UTC.
   */
  private final CalendarModel storage;

  /**
   * The time zone this calendar operates in.
   */
  private ZoneId zone;

  /**
   * Constructs a new ZonedCalendarModel.
   *
   * @param zone The initial time zone for this calendar.
   */
  public ZonedCalendarModelImpl(ZoneId zone) {
    if (zone == null) {
      throw new IllegalArgumentException("ZoneId cannot be null.");
    }
    this.storage = new CalendarModelImpl();
    this.zone = zone;
  }

  @Override
  public ZoneId getZone() {
    return this.zone;
  }

  @Override
  public void setZone(ZoneId zone) {
    if (zone == null) {
      throw new IllegalArgumentException("ZoneId cannot be null.");
    }
    this.zone = zone;
  }

  @Override
  public void createZonedEvent(String subject, ZonedDateTime startTime, ZonedDateTime endTime,
                               String description, Location location, String status)
      throws DuplicateEventException {

    Event eventToStore = buildStorageEvent(subject, startTime, endTime,
        description, location, status, null);
    storage.createSingleEvent(eventToStore);
  }

  @Override
  public void createSingleEvent(Event newEvent) throws DuplicateEventException {
    storage.createSingleEvent(toStorageEvent(newEvent));
  }

  @Override
  public void createEventSeries(Event prototype, List<DayOfWeek> weekdays, int numOccurrences)
      throws DuplicateEventException {
    storage.createEventSeries(toStorageEvent(prototype), weekdays, numOccurrences);
  }

  @Override
  public void createEventSeries(Event prototype, List<DayOfWeek> weekdays, LocalDate untilDate)
      throws DuplicateEventException {
    storage.createEventSeries(toStorageEvent(prototype), weekdays, untilDate);
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    ZonedDateTime dayStart = date.atStartOfDay(this.zone);
    ZonedDateTime dayEnd = date.plusDays(1).atStartOfDay(this.zone);

    LocalDateTime utcStart = toUtcLdt(dayStart.toInstant());
    LocalDateTime utcEnd = toUtcLdt(dayEnd.toInstant());

    return storage.getEventsFrom(utcStart, utcEnd).stream()
        .map(this::fromStorageEvent)
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsFrom(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    LocalDateTime utcStart = toUtcLdt(rangeStart);
    LocalDateTime utcEnd = toUtcLdt(rangeEnd);

    return storage.getEventsFrom(utcStart, utcEnd).stream()
        .map(this::fromStorageEvent)
        .collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(Instant instant) {
    LocalDateTime utcTime = toUtcLdt(instant);
    return storage.isBusy(utcTime);
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    validateLocalDateTime(dateTime);
    return storage.isBusy(toUtcLdt(dateTime));
  }

  @Override
  public void editSingleEvent(String findSubject, LocalDateTime findStartTime,
                              LocalDateTime findEndTime, String propertyToChange, Object newValue)
      throws Exception {

    LocalDateTime utcStart = toUtcLdt(findStartTime);
    LocalDateTime utcEnd = toUtcLdt(findEndTime);
    Object utcAwareNewValue = convertNewValueToUtc(propertyToChange, newValue);

    storage.editSingleEvent(findSubject, utcStart, utcEnd, propertyToChange, utcAwareNewValue);
  }

  @Override
  public void editEventSeries(String findSubject, LocalDateTime findStartTime, EditMode mode,
                              String propertyToChange, Object newValue)
      throws Exception {

    LocalDateTime utcStart = toUtcLdt(findStartTime);
    Object utcAwareNewValue = convertNewValueToUtc(propertyToChange, newValue);

    storage.editEventSeries(findSubject, utcStart, mode, propertyToChange, utcAwareNewValue);
  }

  @Override
  public List<Event> getAllEvents() {
    return storage.getAllEvents().stream()
        .map(this::fromStorageEvent)
        .collect(Collectors.toList());
  }

  /**
   * Validates that a LocalDateTime exists unambiguously in this calendar's zone.
   *
   * <p>This method checks for DST (Daylight Saving Time) transitions:</p>
   * <ul>
   * <li>DST Gap: When clocks spring forward, some times don't exist (e.g., 2:30 AM
   *     when clocks jump from 2:00 AM to 3:00 AM).</li>
   * <li>DST Overlap: When clocks fall back, some times occur twice (e.g., 1:30 AM
   *     occurs twice when clocks go from 2:00 AM back to 1:00 AM).</li>
   * </ul>
   *
   * @param dateTime The local date-time to validate.
   * @throws DateTimeParseException if the time is in a DST gap or overlap.
   */
  private void validateLocalDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return;
    }

    ZoneRules rules = this.zone.getRules();
    List<java.time.ZoneOffset> validOffsets = rules.getValidOffsets(dateTime);

    if (validOffsets.isEmpty()) {
      throw new DateTimeParseException(
          "Time does not exist in zone " + this.zone + " due to DST gap: " + dateTime,
          dateTime.toString(), 0);
    }

    if (validOffsets.size() > 1) {
      throw new DateTimeParseException(
          "Time is ambiguous in zone " + this.zone + " due to DST overlap: " + dateTime,
          dateTime.toString(), 0);
    }
  }

  /**
   * Helper to convert a new value to its UTC representation if it's a date/time.
   *
   * @param property The property being changed.
   * @param value    The new value.
   * @return The value converted for UTC storage if applicable.
   */
  private Object convertNewValueToUtc(String property, Object value) {
    if (("start".equalsIgnoreCase(property) || "end".equalsIgnoreCase(property))
        && value instanceof LocalDateTime) {
      return toUtcLdt((LocalDateTime) value);
    }
    return value;
  }

  /**
   * Converts a "local" Event (in this.zone) to a "storage" Event (in UTC).
   *
   * @param localEvent The event with times in this.zone.
   * @return The event with times converted to UTC for storage.
   */
  private Event toStorageEvent(Event localEvent) {
    if (localEvent == null) {
      return null;
    }

    return buildStorageEvent(
        localEvent.getSubject(),
        localEvent.getStartTime().atZone(this.zone),
        localEvent.getEndTime().atZone(this.zone),
        localEvent.getDescription(),
        localEvent.getLocation(),
        localEvent.getStatus(),
        localEvent.getSeriesId()
    );
  }

  /**
   * Converts a "storage" Event (in UTC) to a "local" Event (in this.zone).
   *
   * @param utcEvent The event with times from storage (in UTC).
   * @return The event with times converted to this.zone.
   */
  private Event fromStorageEvent(Event utcEvent) {
    if (utcEvent == null) {
      return null;
    }
    ZonedDateTime localStart = fromUtcLdtToZdt(utcEvent.getStartTime());
    ZonedDateTime localEnd = fromUtcLdtToZdt(utcEvent.getEndTime());

    return Event.newBuilder(utcEvent)
        .withStartTime(localStart.toLocalDateTime())
        .withEndTime(localEnd.toLocalDateTime())
        .build();
  }

  /**
   * Builds an Event with UTC-based LocalDateTime fields.
   *
   * @param subject     The event subject.
   * @param startTime   The start time (in any zone).
   * @param endTime     The end time (in any zone).
   * @param description The event description.
   * @param location    The event location.
   * @param status      The event status.
   * @param seriesId    The event seriesId.
   * @return An event with UTC-based LocalDateTime fields.
   */
  private Event buildStorageEvent(String subject, ZonedDateTime startTime, ZonedDateTime endTime,
                                  String description, Location location, String status,
                                  String seriesId) {

    LocalDateTime utcStart = toUtcLdt(startTime.toInstant());
    LocalDateTime utcEnd = toUtcLdt(endTime.toInstant());

    return Event.newBuilder(subject, utcStart, utcEnd)
        .withDescription(description)
        .withLocation(location)
        .withStatus(status)
        .withSeriesId(seriesId)
        .build();
  }

  /**
   * Converts a local LocalDateTime to its UTC-based LocalDateTime for storage.
   *
   * @param localDateTime The local "wall time" in this.zone.
   * @return The corresponding LocalDateTime in UTC.
   */
  private LocalDateTime toUtcLdt(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    Instant instant = localDateTime.atZone(this.zone).toInstant();
    return instant.atZone(ZoneOffset.UTC).toLocalDateTime();
  }

  /**
   * Converts an Instant to its UTC-based LocalDateTime for storage.
   *
   * @param instant The instant to convert.
   * @return The corresponding LocalDateTime in UTC.
   */
  private LocalDateTime toUtcLdt(Instant instant) {
    if (instant == null) {
      return null;
    }
    return instant.atZone(ZoneOffset.UTC).toLocalDateTime();
  }

  /**
   * Converts a UTC-based LocalDateTime from storage to a ZonedDateTime in this.zone.
   *
   * @param utcLdt The UTC-based time from storage.
   * @return The ZonedDateTime in this.zone.
   */
  private ZonedDateTime fromUtcLdtToZdt(LocalDateTime utcLdt) {
    if (utcLdt == null) {
      return null;
    }
    Instant instant = utcLdt.toInstant(ZoneOffset.UTC);
    return instant.atZone(this.zone);
  }
}