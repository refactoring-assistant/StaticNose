package calendar.model;

import calendar.control.editmodes.EditMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the Calendar Model.
 * Manages multiple calendars and their events.
 * Thread-safe design with defensive copying where needed.
 */

public class ModelImpl implements Imodel {

  private final List<AbstractEvent> events = new ArrayList<>();
  private ZoneId calendarTimezone;

  /**
   * Implementation of Calendar model with the ZoneId.
   *
   * @param zoneId - the time zone of the calendar
   * @throws IllegalArgumentException - if the zoneId is null
   */
  public ModelImpl(ZoneId zoneId) {
    if (zoneId == null) {
      throw new IllegalArgumentException("zoneId can not be null");
    }
    this.calendarTimezone = zoneId;
  }

  /**
   * Maps a string property name to an EventProperty enum, validating legality.
   *
   * @param property string representation of the property to edit
   * @return corresponding EventProperty enum
   * @throws IllegalArgumentException if property is invalid
   */
  private static EventProperty toProperty(String property) {
    String p = (property == null) ? "" : property.trim().toUpperCase();

    Map<String, EventProperty> propertyMap = Map.ofEntries(
        Map.entry("SUBJECT", EventProperty.SUBJECT),
        Map.entry("DESCRIPTION", EventProperty.DESCRIPTION),
        Map.entry("STATUS", EventProperty.STATUS),
        Map.entry("LOCATION", EventProperty.LOCATION),
        Map.entry("START_DATE", EventProperty.START_DATE),
        Map.entry("STARTDATE", EventProperty.START_DATE),
        Map.entry("START_TIME", EventProperty.START_TIME),
        Map.entry("STARTTIME", EventProperty.START_TIME),
        Map.entry("END_DATE", EventProperty.END_DATE),
        Map.entry("ENDDATE", EventProperty.END_DATE),
        Map.entry("END_TIME", EventProperty.END_TIME),
        Map.entry("ENDTIME", EventProperty.END_TIME),
        Map.entry("ALL_DAY", EventProperty.ALL_DAY_EVENT),
        Map.entry("ALL_DAY_EVENT", EventProperty.ALL_DAY_EVENT)
    );
    EventProperty result = propertyMap.get(p);
    if (result == null) {
      throw new IllegalArgumentException("Invalid property: " + property);
    }
    return result;
  }

  /**
   * Creates a new event with the given details.
   *
   * @param subject event title
   * @param start   start time
   * @param end     end time (defaults to +1h if null)
   * @return true if created successfully, false if conflict exists
   */
  @Override
  public boolean createEvent(String subject, LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      return false;
    }

    AbstractEvent ev = new SingleEvent.SingleEventBuilder()
        .subject(subject)
        .start(start)
        .end(end)
        .timezone(calendarTimezone)
        .build();

    if (conflicts(ev)) {
      return false;
    }
    events.add(ev);
    return true;
  }

  /**
   * Creates an all-day event on the given date from 8 AM to 5 PM.
   *
   * @param subject event title
   * @param date    the date of the all-day event
   * @return true if created successfully without conflict
   */
  @Override
  public boolean createAllDayEvent(String subject, LocalDate date) {
    LocalDateTime start = date.atTime(8, 0);
    LocalDateTime end = date.atTime(17, 0);
    return createEvent(subject, start, end);
  }

  /**
   * The code is used to create event series based on the number of occurrences.
   *
   * @param subject   - subject of the event series
   * @param startDate - start date of the series
   * @param startTime - start time of the event
   * @param count     - the number of occurrences
   * @param endTime   - end time of the event
   * @param days      - MTWRFSU - week days
   * @return - true if the series is created
   */
  @Override
  public boolean createEventSeries(String subject, LocalDate startDate, LocalTime startTime,
                                   int count, LocalTime endTime, Set<DayOfWeek> days) {
    if (count <= 0 || !endTime.isAfter(startTime)) {
      return false;
    }
    List<AbstractEvent> toAdd =
        generateEventInstances(subject, startDate, null, count, startTime, endTime, days);

    for (AbstractEvent e : toAdd) {
      if (conflicts(e)) {
        return false;
      }
    }
    events.addAll(toAdd);
    return true;

  }

  /**
   * The code is used to create event series based on the number of occurrences.
   *
   * @param subject   - subject of the event series
   * @param startDate - start date of the series
   * @param startTime - start time of the event
   * @param untilDate - end date of the series
   * @param endTime   - end time of the event
   * @param days      - MTWRFSU - week days
   * @return - true if the series is created
   */
  @Override
  public boolean createEventSeriesUntil(String subject, LocalDate startDate, LocalTime startTime,
                                        LocalDate untilDate, LocalTime endTime,
                                        Set<DayOfWeek> days) {
    if (untilDate.isBefore(startDate) || !endTime.isAfter(startTime)) {
      return false;
    }

    List<AbstractEvent> toAdd =
        generateEventInstances(subject, startDate, untilDate, 0, startTime, endTime, days);

    for (AbstractEvent e : toAdd) {
      if (conflicts(e)) {
        return false;
      }
    }
    events.addAll(toAdd);
    return true;

  }

  /**
   * Edits an event or series of events based on the mode provided.
   * - SINGLE + start_time change → the instance becomes its own series (new seriesId).
   * - FORWARD + start_time change → the edited suffix becomes a new series (one
   * new seriesId shared).
   * - SERIES + start_time change → entire series stays together (seriesId unchanged).
   */
  @Override
  public boolean editEvent(String subject, LocalDateTime start, LocalDateTime end,
                           String property, String newValue, EditMode mode) {

    AbstractEvent target = findTargetEvent(subject, start, end);
    if (target == null) {
      return false;
    }

    Collection<AbstractEvent> toUpdate = getEventsToUpdate(target, mode);
    updateEvents(toUpdate, toProperty(property), newValue, mode, target);

    return true;
  }

  /**
   * Gets event descriptions for all events occurring on the specified date.
   *
   * @param date the date to query
   * @return list of event description strings
   */
  @Override
  public List<String> getEventsOn(LocalDate date) {
    return events.stream()
        .filter(e -> (e.getStart().toLocalDate().isBefore(date)
            || e.getStart().toLocalDate().equals(date)) && (e.getEnd().toLocalDate().isAfter(date)
            || e.getEnd().toLocalDate().equals(date)))
        .map(AbstractEvent::toString)
        .collect(Collectors.toList());
  }

  /**
   * Gets event descriptions for all events occurring within a datetime range.
   *
   * @param from start datetime (inclusive)
   * @param to   end datetime (inclusive)
   * @return list of event description strings
   */
  @Override
  public List<String> getEventsBetween(LocalDateTime from, LocalDateTime to) {
    return events.stream()
        .filter(e -> !e.getEnd().isBefore(from) && !e.getStart().isAfter(to))
        .map(AbstractEvent::toString)
        .collect(Collectors.toList());
  }

  @Override
  public List<AbstractEvent> getAllEvents() {
    return events;
  }

  @Override
  public ZoneId getTimeZone() {
    return this.calendarTimezone;
  }

  /**
   * Checks if calendar is busy (has any event) at the specified datetime.
   *
   * @param dateTime datetime to check
   * @return true if any event is ongoing at the datetime, false otherwise
   */
  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return events.stream()
        .anyMatch(e -> !dateTime.isBefore(e.getStart()) && !dateTime
            .isAfter(e.getEnd()));
  }

  /**
   * Checks for scheduling conflicts between a new event and existing events.
   *
   * @param newEvent event to check for conflicts
   * @return true if conflict exists, false otherwise
   */
  private boolean conflicts(AbstractEvent newEvent) {

    for (AbstractEvent e : events) {

      ZonedDateTime newEventStart = ZonedDateTime.of(newEvent.getStart(), newEvent.getTimeZone());
      ZonedDateTime newEventEnd = ZonedDateTime.of(newEvent.getEnd(), newEvent.getTimeZone());

      ZonedDateTime eventStart = ZonedDateTime.of(e.getStart(), e.getTimeZone());
      ZonedDateTime eventEnd = ZonedDateTime.of(e.getEnd(), e.getTimeZone());

      boolean dup = e.getSubject().equalsIgnoreCase(newEvent.getSubject())
          && newEventStart.equals(eventStart)
          && newEventEnd.equals(eventEnd);
      if (dup) {
        return true;
      }
    }
    return false;
  }

  /**
   * Helper method to create event instances between startDate and endDate or count,
   * filtered by the specified days with given startTime and endTime.
   *
   * @param subject   subject of the event series
   * @param startDate start date of the series
   * @param endDate   the end date of the series (inclusive), can be null if using count
   * @param count     the number of occurrences, used if endDate is null
   * @param startTime start time of each event
   * @param endTime   end time of each event
   * @param days      set of days of week (MTWRFSU)
   * @return list of event instances created
   */
  private List<AbstractEvent> generateEventInstances(String subject, LocalDate startDate,
                                                     LocalDate endDate, int count,
                                                     LocalTime startTime,
                                                     LocalTime endTime, Set<DayOfWeek> days) {

    List<AbstractEvent> toAdd = new ArrayList<>();
    String seriesId = UUID.randomUUID().toString();
    LocalDate currentDate = startDate;

    while ((endDate != null && !currentDate.isAfter(endDate))
        || (endDate == null && toAdd.size() < count)) {
      if (days.contains(currentDate.getDayOfWeek())) {
        LocalDateTime startDateTime = currentDate.atTime(startTime);
        LocalDateTime endDateTime = currentDate.atTime(endTime);
        toAdd.add(new EventInstance.EventInstanceBuilder()
            .subject(subject)
            .start(startDateTime)
            .end(endDateTime)
            .seriesId(seriesId)
            .timezone(calendarTimezone)
            .build());
      }
      currentDate = currentDate.plusDays(1);
    }

    return toAdd;
  }

  /**
   * Finds a target event by subject, start time, and end time.
   *
   * @param subject the event subject
   * @param start   the start time
   * @param end     the end time
   * @return the matching event, or null if not found
   */
  private AbstractEvent findTargetEvent(String subject, LocalDateTime start, LocalDateTime end) {
    List<AbstractEvent> matches = events.stream()
        .filter(e -> e.getSubject().equalsIgnoreCase(subject)
            && e.getStart().equals(start)
            && e.getEnd().equals(end))
        .collect(Collectors.toList());

    return matches.size() == 1 ? matches.get(0) : null;
  }

  /**
   * Gets the collection of events to update based on edit mode.
   *
   * @param target the target event
   * @param mode   the edit mode
   * @return collection of events to update
   */
  private Collection<AbstractEvent> getEventsToUpdate(AbstractEvent target, EditMode mode) {
    String seriesId = getSeriesId(target);

    if (seriesId == null) {
      return List.of(target);
    }

    if (mode == EditMode.FORWARD) {
      return getForwardEvents(seriesId, target);
    } else if (mode == EditMode.SERIES) {
      return getSeriesEvents(seriesId);
    } else {
      return List.of(target);
    }
  }


  /**
   * Gets the series ID for an event if it's part of a series.
   *
   * @param event the event to check
   * @return the series ID, or null if not part of a series
   */
  private String getSeriesId(AbstractEvent event) {
    return (event instanceof EventInstance) ? ((EventInstance) event).getSeriesId() : null;
  }

  /**
   * Gets all events in the series starting from the target event forward.
   *
   * @param seriesId the series ID
   * @param target   the target event
   * @return collection of forward events
   */
  private Collection<AbstractEvent> getForwardEvents(String seriesId, AbstractEvent target) {
    return events.stream()
        .filter(e -> e instanceof EventInstance)
        .map(e -> (EventInstance) e)
        .filter(e -> seriesId.equals(e.getSeriesId()) && !e.getStart().isBefore(target.getStart()))
        .collect(Collectors.toList());
  }

  /**
   * Gets all events in the entire series.
   *
   * @param seriesId the series ID
   * @return collection of all series events
   */
  private Collection<AbstractEvent> getSeriesEvents(String seriesId) {
    return events.stream()
        .filter(e -> e instanceof EventInstance)
        .map(e -> (EventInstance) e)
        .filter(e -> seriesId.equals(e.getSeriesId()))
        .collect(Collectors.toList());
  }

  /**
   * Updates the properties of the specified events.
   *
   * @param events   the events to update
   * @param prop     the property to edit
   * @param newValue the new property value
   * @param mode     the edit mode
   * @param target   the original target event
   */
  private void updateEvents(Collection<AbstractEvent> events, EventProperty prop,
                            String newValue, EditMode mode, AbstractEvent target) {

    boolean startTimeChange = (prop == EventProperty.START_TIME);
    String newSeriesIdForSplit = getNewSeriesIdForSplit(startTimeChange, mode);

    for (AbstractEvent ev : events) {
      ev.editProperty(prop, newValue);
      if (newSeriesIdForSplit != null && ev instanceof EventInstance) {
        ((EventInstance) ev).setSeriesId(newSeriesIdForSplit);
      }
    }
  }

  /**
   * Generates a new series ID for splitting series when start time changes.
   *
   * @param startTimeChange whether start time is being changed
   * @param mode            the edit mode
   * @return new series ID, or null if no split needed
   */
  private String getNewSeriesIdForSplit(boolean startTimeChange, EditMode mode) {
    return (startTimeChange && (mode == EditMode.SINGLE || mode == EditMode.FORWARD))
        ? UUID.randomUUID().toString()
        : null;
  }

  /**
   * The method is used for setting the timezone of the calendar.
   *
   * @param newTimeZone - the timezone given by the controller.
   */
  @Override
  public void changeTimeZone(ZoneId newTimeZone) {
    if (newTimeZone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }

    if (this.calendarTimezone.equals(newTimeZone)) {
      return;
    }

    ZoneId oldTimezone = this.calendarTimezone;
    for (AbstractEvent event : events) {
      if (event instanceof EventInstance) {
        LocalDateTime convertedStart = convertDateTime(
            event.getStart(), oldTimezone, newTimeZone);
        LocalDateTime convertedEnd = convertDateTime(
            event.getEnd(), oldTimezone, newTimeZone);

        if (!convertedStart.toLocalDate().equals(convertedEnd.toLocalDate())) {
          throw new IllegalArgumentException(
              "Cannot change timezone: Event '" + event.getSubject()
                  + "' starting at " + event.getStart()
                  + " would span multiple days after timezone conversion. "
                  + "Series events must start and end on the same day.");
        }
      }
    }
    List<AbstractEvent> convertedEvents = new ArrayList<>();
    for (AbstractEvent event : events) {
      AbstractEvent convertedEvent = convertEventTimezone(event, oldTimezone,
          newTimeZone);
      convertedEvents.add(convertedEvent);
    }

    this.events.clear();
    this.events.addAll(convertedEvents);
    this.calendarTimezone = newTimeZone;

  }

  /**
   * Converts an event's date times from source timezone to target timezone.
   */
  private AbstractEvent convertEventTimezone(AbstractEvent event, ZoneId sourceZone,
                                             ZoneId targetZone) {
    LocalDateTime convertedStart = convertDateTime(event.getStart(), sourceZone, targetZone);
    LocalDateTime convertedEnd = convertDateTime(event.getEnd(), sourceZone, targetZone);

    AbstractEvent.AbstractEventBuilder<?> builder = event.toBuilder();

    if (event instanceof EventInstance) {
      EventInstance ei = (EventInstance) event;
      return new EventInstance.EventInstanceBuilder()
          .subject(ei.getSubject())
          .start(convertedStart)
          .end(convertedEnd)
          .timezone(targetZone)
          .location(ei.getLocation())
          .description(ei.getDescription())
          .status(ei.getStatus())
          .seriesId(ei.getSeriesId())
          .build();
    } else {
      return new SingleEvent.SingleEventBuilder()
          .subject(event.getSubject())
          .start(convertedStart)
          .end(convertedEnd)
          .timezone(targetZone)
          .location(event.getLocation())
          .description(event.getDescription())
          .status(event.getStatus())
          .build();
    }
  }

  /**
   * Utility method to convert LocalDateTime between timezones.
   */
  private LocalDateTime convertDateTime(LocalDateTime dateTime, ZoneId sourceZone,
                                        ZoneId targetZone) {
    ZonedDateTime sourceZoned = ZonedDateTime.of(dateTime, sourceZone);
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetZone);
    return targetZoned.toLocalDateTime();
  }

}
