package calendar.service;

import calendar.model.Calendar;
import calendar.model.CalendarEvent;
import calendar.model.CalendarInterface;
import calendar.model.Event;
import calendar.model.exceptions.ConflictException;
import calendar.model.repository.EventRepository;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is the main implementation of the CalendarService.
 * It *inherits* all event logic from EventServiceImpl.
 * It *adds* the implementation for calendar management.
 */
public class CalendarServiceImpl extends EventServiceImpl implements CalendarService {

  /**
   * Constructs a new CalendarServiceImpl.
   * The constructor of the parent (EventServiceImpl) is called automatically.
   */
  public CalendarServiceImpl() {
    super();
  }

  @Override
  public void createCalendar(String name, String timezone)
      throws IllegalArgumentException, DateTimeException {

    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("A calendar with the name '" + name + "' already exists.");
    }
    try {
      ZoneId zone = ZoneId.of(timezone);
      CalendarInterface newCalendar = new Calendar(name, zone);
      calendars.put(name, newCalendar);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid timezone ID: " + timezone, e);
    }
  }

  @Override
  public void useCalendar(String name) throws IllegalArgumentException {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found.");
    }
    this.activeCalendarName = name;
  }

  @Override
  public String getCurrentCalendarName() {
    return this.activeCalendarName;
  }

  @Override
  public ZoneId getCurrentCalendarTimezone() {
    if (activeCalendarName == null) {
      return null;
    }
    return getActiveCalendar().getTimezone();
  }

  @Override
  public void editCalendarName(String oldName, String newName) throws IllegalArgumentException {
    if (!calendars.containsKey(oldName)) {
      throw new IllegalArgumentException("Calendar '" + oldName + "' not found.");
    }
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("A calendar with the name '" + newName
          + "' already exists.");
    }

    CalendarInterface cal = calendars.remove(oldName);
    cal.setName(newName);
    calendars.put(newName, cal);

    if (Objects.equals(activeCalendarName, oldName)) {
      activeCalendarName = newName;
    }
  }

  @Override
  public void editCalendarTimezone(String calendarName, String newTimezone)
      throws IllegalArgumentException, DateTimeException {

    if (!calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' not found.");
    }
    try {
      ZoneId zone = ZoneId.of(newTimezone);
      calendars.get(calendarName).setTimezone(zone);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid timezone ID: " + newTimezone, e);
    }
  }

  @Override
  public List<CalendarInterface> getAllCalendars() {
    return new ArrayList<>(calendars.values());
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime eventStart,
                        String targetCalName, LocalDateTime newTargetStart)
      throws IllegalArgumentException, ConflictException {

    CalendarInterface targetCal = calendars.get(targetCalName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalName + "' not found.");
    }
    ZoneId targetZone = targetCal.getTimezone();
    EventRepository targetRepo = targetCal.getEventRepository();

    Event eventToCopy = this.findUniqueEvent(eventName, eventStart);
    if (eventToCopy == null) {
      throw new IllegalArgumentException("Event '"
          + eventName + "' at " + eventStart + " not found.");
    }

    Duration duration = Duration.between(eventToCopy.getStart(), eventToCopy.getEnd());

    Instant newStartInstant = newTargetStart.atZone(targetZone).toInstant();
    Instant newEndInstant = newStartInstant.plus(duration);

    Event newEvent = new CalendarEvent(
        eventToCopy.getSubject(),
        newStartInstant,
        newEndInstant,
        eventToCopy.getDescription(),
        eventToCopy.getLocation(),
        eventToCopy.isPrivate(),
        null
    );

    targetRepo.addEvent(newEvent);
  }

  @Override
  public void copyEventsOn(LocalDate date, String targetCalName, LocalDate newTargetDate)
      throws IllegalArgumentException {

    CalendarInterface sourceCal = getActiveCalendar();
    ZoneId sourceZone = sourceCal.getTimezone();
    CalendarInterface targetCal = calendars.get(targetCalName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalName + "' not found.");
    }
    ZoneId targetZone = targetCal.getTimezone();
    EventRepository targetRepo = targetCal.getEventRepository();

    List<Event> eventsToCopy = this.getEventsOn(date);

    for (Event eventToCopy : eventsToCopy) {
      Duration duration = Duration.between(eventToCopy.getStart(), eventToCopy.getEnd());

      LocalDateTime sourceLdt = LocalDateTime.ofInstant(eventToCopy.getStart(), sourceZone);

      LocalDateTime convertedLdt = convertWallTime(sourceLdt, sourceZone, targetZone);

      LocalDateTime newTargetStartLdt = newTargetDate.atTime(convertedLdt.toLocalTime());

      Instant newStartInstant = newTargetStartLdt.atZone(targetZone).toInstant();
      Instant newEndInstant = newStartInstant.plus(duration);

      Event newEvent = new CalendarEvent(
          eventToCopy.getSubject(),
          newStartInstant,
          newEndInstant,
          eventToCopy.getDescription(),
          eventToCopy.getLocation(),
          eventToCopy.isPrivate(),
          null
      );

      try {
        targetRepo.addEvent(newEvent);
      } catch (ConflictException e) {
        System.err.println("Skipped copy: " + e.getMessage());
      }
    }
  }

  @Override
  public void copyEventsBetween(LocalDate sourceStartDate, LocalDate sourceEndDate,
                                String targetCalName, LocalDate newTimelineDate)
      throws IllegalArgumentException {

    CalendarInterface sourceCal = getActiveCalendar();
    ZoneId sourceZone = sourceCal.getTimezone();
    CalendarInterface targetCal = calendars.get(targetCalName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetCalName + "' not found.");
    }
    ZoneId targetZone = targetCal.getTimezone();
    EventRepository targetRepo = targetCal.getEventRepository();

    LocalDateTime rangeStart = sourceStartDate.atStartOfDay();
    LocalDateTime rangeEnd = sourceEndDate.plusDays(1).atStartOfDay();
    List<Event> eventsToCopy = this.getEventsBetween(rangeStart, rangeEnd);

    long daysOffset = ChronoUnit.DAYS.between(sourceStartDate, newTimelineDate);

    Map<String, List<Event>> seriesMap = eventsToCopy.stream()
        .filter(Event::isSeries)
        .collect(Collectors.groupingBy(Event::getSeriesId));

    List<Event> singleEvents = eventsToCopy.stream()
        .filter(e -> !e.isSeries())
        .collect(Collectors.toList());

    for (Event eventToCopy : singleEvents) {
      copyAndOffsetEvent(eventToCopy, sourceZone, targetZone, targetRepo, daysOffset, null);
    }

    for (List<Event> series : seriesMap.values()) {
      String newSeriesId = UUID.randomUUID().toString();
      for (Event eventToCopy : series) {
        copyAndOffsetEvent(eventToCopy, sourceZone,
            targetZone, targetRepo, daysOffset, newSeriesId);
      }
    }
  }

  /**
   * Helper for copyEventsBetween.
   * Copies a single event, applying a date offset and timezone conversion.
   */
  private void copyAndOffsetEvent(Event eventToCopy, ZoneId sourceZone, ZoneId targetZone,
                                  EventRepository targetRepo, long daysOffset, String newSeriesId) {

    Duration duration = Duration.between(eventToCopy.getStart(), eventToCopy.getEnd());

    LocalDateTime sourceLdt = LocalDateTime.ofInstant(eventToCopy.getStart(), sourceZone);

    LocalDateTime offsetLdt = sourceLdt.plusDays(daysOffset);

    LocalDateTime targetLdt = convertWallTime(offsetLdt, sourceZone, targetZone);

    Instant newStartInstant = targetLdt.atZone(targetZone).toInstant();
    Instant newEndInstant = newStartInstant.plus(duration);

    Event newEvent = new CalendarEvent(
        eventToCopy.getSubject(),
        newStartInstant,
        newEndInstant,
        eventToCopy.getDescription(),
        eventToCopy.getLocation(),
        eventToCopy.isPrivate(),
        newSeriesId
    );

    try {
      targetRepo.addEvent(newEvent);
    } catch (ConflictException e) {
      System.err.println("Skipped copy: " + e.getMessage());
    }
  }

  /**
   * Converts a "wall time" from a source zone to the "wall time" in a target zone.
   * e.g., 10:00 America/New_York -> 07:00 America/Los_Angeles
   */
  private LocalDateTime convertWallTime(LocalDateTime ldtToConvert,
                                        ZoneId sourceZone, ZoneId targetZone) {

    ZonedDateTime sourceZdt = ldtToConvert.atZone(sourceZone);

    ZonedDateTime targetZdt = sourceZdt.withZoneSameInstant(targetZone);

    return targetZdt.toLocalDateTime();
  }

}