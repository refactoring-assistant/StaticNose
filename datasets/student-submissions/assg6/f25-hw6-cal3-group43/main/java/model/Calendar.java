package model;

import static controller.CommandController.editInstruction;
import static controller.QueryController.DATETIME_ERROR;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import messaging.Messages;
import utils.Weekday;

/**
 * The class Calendar represents a calendar that is able to store events and event series. It is
 * not only possible to create the aforementioned type of events, but to also edit and query
 * about them.
 */
public class Calendar {
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/New_York");
  List<Event> events;
  List<EventSeries> eventSeries;
  private String name;
  private ZoneId timezone;

  /**
   * Constructs a Calendar with a default name and the system timezone.
   */
  public Calendar() {
    this("default", DEFAULT_ZONE);
  }

  /**
   * Constructs a Calendar with the provided metadata.
   *
   * @param name     unique calendar name
   * @param timezone timezone associated with the calendar
   */
  public Calendar(String name, ZoneId timezone) {
    this.name = normalizeName(name);
    this.timezone = Objects.requireNonNull(timezone, "timezone");
    this.events = new ArrayList<>();
    this.eventSeries = new LinkedList<>();
  }

  private static String normalizeName(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be blank");
    }
    return trimmed;
  }

  private static String eventKey(String subject, LocalDateTime start, LocalDateTime end) {
    return subject + "|" + start + "|" + end;
  }

  private static LocalDateTime convertInstant(LocalDateTime timestamp, ZoneId from, ZoneId to) {
    return timestamp.atZone(from).withZoneSameInstant(to).toLocalDateTime();
  }

  /**
   * Returns the calendar name.
   *
   * @return calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Updates the calendar name after validation.
   *
   * @param newName new calendar name
   */
  public void rename(String newName) {
    this.name = normalizeName(newName);
  }

  /**
   * Returns the calendar timezone.
   *
   * @return timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Updates the associated timezone.
   *
   * @param timezone new timezone
   */
  public void setTimezone(ZoneId timezone) {
    updateTimezone(timezone);
  }

  /**
   * Updates the timezone and shifts all existing events to preserve their absolute instants.
   *
   * @param newZone target timezone
   */
  public void updateTimezone(ZoneId newZone) {
    Objects.requireNonNull(newZone, "timezone");
    ZoneId current = this.timezone;
    if (current.equals(newZone)) {
      return;
    }

    List<ConvertedTime> conversions = new ArrayList<>();
    Set<String> occupied = new HashSet<>();
    for (Event event : events) {
      LocalDateTime newStart = convertInstant(event.getStartTime(), current, newZone);
      LocalDateTime newEnd = convertInstant(event.getEndTime(), current, newZone);
      String key = eventKey(event.getSubject(), newStart, newEnd);
      if (!occupied.add(key)) {
        throw new IllegalArgumentException(
            "Timezone change would create duplicate event for subject \"" + event.getSubject()
                + "\" from " + newStart + " to " + newEnd + ".");
      }
      conversions.add(new ConvertedTime(event, newStart, newEnd));
    }

    Map<EventSeries, List<Event>> memberships = new LinkedHashMap<>();
    for (Event event : events) {
      EventSeries series = event.getEventSeries();
      if (series != null) {
        memberships.computeIfAbsent(series, ignored -> new ArrayList<>()).add(event);
      }
    }
    for (Map.Entry<EventSeries, List<Event>> entry : memberships.entrySet()) {
      EventSeries series = entry.getKey();
      for (Event event : entry.getValue()) {
        series.removeEvent(event);
      }
    }

    for (ConvertedTime conversion : conversions) {
      conversion.event.setStartTime(conversion.start);
      conversion.event.setEndTime(conversion.end);
    }

    for (Map.Entry<EventSeries, List<Event>> entry : memberships.entrySet()) {
      EventSeries series = entry.getKey();
      for (Event event : entry.getValue()) {
        series.addEvent(event);
      }
    }

    this.timezone = newZone;
  }

  /**
   * Get all events (mutable list).
   *
   * @return a list of all events
   */
  public List<Event> getEvents() {
    return events;
  }

  /**
   * Add a single event.
   *
   * @param event the event to be added
   */
  public void addEvent(Event event) {
    ensureUniqueOrThrow(event.getSubject(), event.getStartTime(), event.getEndTime());
    events.add(event);
  }

  /**
   * Creates a new event given the subject, start date and time, and end date and time,
   * and adds it to the list of events.
   *
   * @param subject the subject of the event
   * @param start   the start date and time
   * @param end     the end date and time
   */
  public void addEvent(String subject, LocalDateTime start, LocalDateTime end) {
    ensureUniqueOrThrow(subject, start, end);
    events.add(new Event(start, end, subject));
  }

  /**
   * Creates a new event series that is able to repeat multiple times for the
   * specified days of the week from the start time to end time.
   *
   * @param subject  the subject of the event series
   * @param start    the start date and time of the event series
   * @param end      the end date and time of the event series
   * @param weekdays the weekdays the events in the series takes place
   * @param repeat   how many times the event series repeats
   * @throws IllegalArgumentException when repeat is less than or equal to 0
   * @throws IllegalArgumentException when weekdays has an invalid character
   */
  public void addEventSeries(String subject,
                             LocalDateTime start,
                             LocalDateTime end,
                             String weekdays,
                             int repeat) {
    if (repeat <= 0) {
      throw new IllegalArgumentException("repeat must be > 0");
    }

    EnumSet<Weekday> days = Weekday.parsePattern(weekdays);
    if (days.isEmpty()) {
      throw new IllegalArgumentException("weekdays pattern must contain M,T,W,R,F,S,U");
    }

    LocalTime startTime = start.toLocalTime();
    LocalTime endTime = end.toLocalTime();
    if (startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("start time must be before end time");
    }
    int created = 0;
    LocalDateTime cursor = start;
    EventSeries series = new EventSeries();  // sorted container

    while (created < repeat) {
      if (days.contains(Weekday.from(cursor.getDayOfWeek()))) {
        LocalDate date = cursor.toLocalDate();
        LocalDateTime occStart = LocalDateTime.of(date, startTime);
        LocalDateTime occEnd = LocalDateTime.of(date, endTime);
        if (!occEnd.isAfter(occStart)) {
          throw new IllegalArgumentException(
              "End time must be after start time for each occurrence");
        }
        ensureUniqueOrThrow(subject, occStart, occEnd);
        Event ev = new Event(occStart, occEnd, subject);
        events.add(ev);           // global registry (unsorted bag)
        series.addEvent(ev);      // sorted + back-pointer
        created++;
      }
      cursor = cursor.plusDays(1);
    }
    eventSeries.add(series);       // keep the series handle
  }

  /**
   * Creates a new event series that is able to repeat multiple times for the
   * specified days of the week. The events in the event series are considered
   * all day events.
   *
   * @param subject   the subject of the event series
   * @param startDate the start date of the event series
   * @param weekdays  the weekdays the events in the series takes place
   * @param repeat    how many times the event series repeats
   * @throws IllegalArgumentException when repeat is less than or equal to 0
   * @throws IllegalArgumentException when weekdays has an invalid character
   */
  public void addEventSeries(String subject,
                             LocalDate startDate,
                             String weekdays,
                             int repeat) {
    if (repeat <= 0) {
      throw new IllegalArgumentException("repeat must be > 0");
    }

    EnumSet<Weekday> days = Weekday.parsePattern(weekdays);
    if (days.isEmpty()) {
      throw new IllegalArgumentException("weekdays pattern must contain M,T,W,R,F,S,U");
    }

    int created = 0;
    LocalDate cursor = startDate;
    EventSeries series = new EventSeries();

    while (created < repeat) {
      if (days.contains(Weekday.from(cursor.getDayOfWeek()))) {
        LocalDateTime occStart = LocalDateTime.of(cursor, LocalTime.of(8, 0));
        LocalDateTime occEnd = LocalDateTime.of(cursor, LocalTime.of(17, 0));
        ensureUniqueOrThrow(subject, occStart, occEnd);
        Event ev = new Event(cursor, subject);
        events.add(ev);
        series.addEvent(ev);
        created++;
      }
      cursor = cursor.plusDays(1);
    }
    eventSeries.add(series);
  }

  /**
   * Creates a new event series that is able to repeat multiple
   * times until an inclusive date for the specified days of the
   * week from the start time to end time.
   *
   * @param subject        the subject of the event series
   * @param start          the start date and time of the event series
   * @param end            the end date and time of the event series
   * @param weekdays       the weekdays the events in the series takes place
   * @param untilInclusive the date the event series repeats up to
   * @throws IllegalArgumentException when untilInclusive is before the event starts
   * @throws IllegalArgumentException when weekdays has an invalid character
   * @throws IllegalArgumentException when the end time is not after the start time
   */
  public void addEventsUntil(String subject,
                             LocalDateTime start,
                             LocalDateTime end,
                             String weekdays,
                             LocalDate untilInclusive) {
    if (untilInclusive.isBefore(start.toLocalDate())) {
      throw new IllegalArgumentException("until date is before start date");
    }
    var days = Weekday.parsePattern(weekdays);
    if (days.isEmpty()) {
      throw new IllegalArgumentException("weekdays pattern must contain M,T,W,R,F,S,U");
    }

    LocalTime startTime = start.toLocalTime();
    LocalTime endTime = end.toLocalTime();
    if (!endTime.isAfter(startTime)) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    LocalDate cursor = start.toLocalDate();
    EventSeries series = new EventSeries();
    while (!cursor.isAfter(untilInclusive)) {
      if (days.contains(Weekday.from(cursor.getDayOfWeek()))) {
        LocalDateTime occStart = LocalDateTime.of(cursor, startTime);
        LocalDateTime occEnd = LocalDateTime.of(cursor, endTime);
        ensureUniqueOrThrow(subject, occStart, occEnd);
        Event ev = new Event(occStart, occEnd, subject);
        events.add(ev);
        series.addEvent(ev);
      }
      cursor = cursor.plusDays(1);
    }
    eventSeries.add(series);
  }

  /* ===================== Mutations used by controller ===================== */

  /**
   * Creates a new event series that is able to repeat multiple times until an
   * inclusive date for the specified days of the week. The events in the event
   * series are considered all day events.
   *
   * @param subject        the subject of the event series
   * @param startDate      the start date of the event series
   * @param weekdays       the weekdays the events in the series takes place
   * @param untilInclusive the date the event series repeats up to
   * @throws IllegalArgumentException when untilInclusive is before the event starts
   * @throws IllegalArgumentException when weekdays has an invalid character
   */
  public void addEventsUntil(String subject,
                             LocalDate startDate,
                             String weekdays,
                             LocalDate untilInclusive) {
    if (untilInclusive.isBefore(startDate)) {
      throw new IllegalArgumentException("until date is before start date");
    }
    var days = Weekday.parsePattern(weekdays);
    if (days.isEmpty()) {
      throw new IllegalArgumentException("weekdays pattern must contain M,T,W,R,F,S,U");
    }

    LocalDate cursor = startDate;
    EventSeries series = new EventSeries();
    while (!cursor.isAfter(untilInclusive)) {
      if (days.contains(Weekday.from(cursor.getDayOfWeek()))) {
        LocalDateTime occStart = LocalDateTime.of(cursor, LocalTime.of(8, 0));
        LocalDateTime occEnd = LocalDateTime.of(cursor, LocalTime.of(17, 0));
        ensureUniqueOrThrow(subject, occStart, occEnd);
        Event ev = new Event(cursor, subject);
        events.add(ev);
        series.addEvent(ev);
      }
      cursor = cursor.plusDays(1);
    }
    eventSeries.add(series);
  }

  /**
   * Return all event series.
   *
   * @return a list of all event series
   */
  public List<EventSeries> getEventSeries() {
    return eventSeries;
  }

  /**
   * Registers a new empty series with this calendar and returns it.
   *
   * @return newly added EventSeries
   */
  public EventSeries createSeries() {
    EventSeries series = new EventSeries();
    this.eventSeries.add(series);
    return series;
  }

  /**
   * Split the given event series from a start time (inclusive).
   * Returns the new tail series that starts at or after the given time, or null if none.
   * The new series is registered back into the calendar.
   *
   * @param series the event series that is going to be split
   * @param from   the inclusive start date and time
   * @return a new event series starting at or after from, null if none
   */
  public EventSeries splitEventSeries(EventSeries series, LocalDateTime from) {
    if (series == null) {
      return null;
    }
    EventSeries tail = series.splitFrom(from);
    if (tail != null && !tail.events().isEmpty()) {
      this.eventSeries.add(tail);
      return tail;
    }
    return null;
  }

  /**
   * Finds all events that match a given subject and start date and time.
   *
   * @param subject the subject to search for
   * @param start   the start date and time to search for
   * @return a list of events that match the subject and start date and time
   */
  public List<Event> findEvents(String subject, LocalDateTime start) {
    List<Event> matches = new ArrayList<>();
    for (Event e : events) {
      if (subject.equals(e.getSubject()) && start.equals(e.getStartTime())) {
        matches.add(e);
      }
    }
    return matches;
  }

  /**
   * Finds a single event that exactly matches the provided subject and start time.
   *
   * @param subject subject identifying the event
   * @param start   start timestamp to match
   * @return the matching event, or null if none found
   */
  public Event findExactEvent(String subject, LocalDateTime start) {
    Objects.requireNonNull(subject, "subject");
    Objects.requireNonNull(start, "start");
    for (Event event : events) {
      if (subject.equals(event.getSubject()) && start.equals(event.getStartTime())) {
        return event;
      }
    }
    return null;
  }

  private boolean hasConflict(String subject,
                              LocalDateTime start,
                              LocalDateTime end,
                              Event ignore) {
    for (Event event : events) {
      if (event == ignore) {
        continue;
      }
      if (subject.equals(event.getSubject())
          && start.equals(event.getStartTime())
          && end.equals(event.getEndTime())) {
        return true;
      }
    }
    return false;
  }

  private void ensureUniqueOrThrow(String subject, LocalDateTime start, LocalDateTime end) {
    if (!start.isBefore(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    if (hasConflict(subject, start, end, null)) {
      throw new IllegalArgumentException(
          "Event already exists for subject \"" + subject + "\" from " + start + " to " + end);
    }
  }

  private boolean hasConflicts(List<ProposedState> proposals) {
    if (proposals.isEmpty()) {
      return false;
    }
    Set<Event> changing = new HashSet<>();
    for (ProposedState proposal : proposals) {
      changing.add(proposal.event);
    }
    Set<String> occupied = new HashSet<>();
    for (Event event : events) {
      if (changing.contains(event)) {
        continue;
      }
      occupied.add(eventKey(event.getSubject(), event.getStartTime(), event.getEndTime()));
    }
    for (ProposedState proposal : proposals) {
      String key = eventKey(proposal.subject, proposal.start, proposal.end);
      if (!occupied.add(key)) {
        Messages.error("Duplicate event exists for subject \"" + proposal.subject
            + "\" from " + proposal.start + " to " + proposal.end + ".");
        return true;
      }
    }
    return false;
  }

  private LinkedHashSet<Event> gatherSeriesTailEvents(List<Event> anchors,
                                                      LocalDateTime anchorStart) {
    LinkedHashSet<Event> changes = new LinkedHashSet<>();
    Set<EventSeries> processed = new HashSet<>();

    for (Event anchor : anchors) {
      EventSeries series = anchor.getEventSeries();
      if (series != null && processed.add(series)) {
        Event first = series.firstAtOrAfter(anchorStart);
        if (first != null) {
          changes.addAll(new ArrayList<>(series.events().tailSet(first, true)));
        }
      } else if (series == null) {
        changes.add(anchor);
      }
    }
    return changes;
  }

  private void detachEventFromSeries(Event event) {
    EventSeries series = event.getEventSeries();
    if (series != null) {
      boolean removed = series.removeEvent(event);
      if (removed && series.isEmpty()) {
        this.eventSeries.remove(series);
      }
    }
  }

  private LocalDateTime resolveNewEndTime(Event event,
                                          Event anchor,
                                          LocalDateTime requestedEnd) {
    EventSeries anchorSeries = anchor.getEventSeries();
    if (anchorSeries == null || event == anchor || event.getEventSeries() != anchorSeries) {
      return requestedEnd;
    }
    LocalDate eventEndDate = event.getEndTime().toLocalDate();
    return LocalDateTime.of(eventEndDate, requestedEnd.toLocalTime());
  }

  /**
   * Returns all events in a series that start on or after a given time.
   *
   * @param series an event series to search
   * @param from   the start date and time to search at or after
   * @return a list of events that start at or after from
   */
  public List<Event> collectEventsFromSeries(EventSeries series, LocalDateTime from) {
    List<Event> result = new ArrayList<>();
    if (series == null) {
      return result;
    }
    Event first = series.firstAtOrAfter(from);
    if (first != null) {
      result.addAll(series.events().tailSet(first, true));
    }
    return result;
  }

  /**
   * Prints the events for the given date, which includes events that only partially
   * overlap.
   *
   * @param givenDate a date string that represents a date
   */
  public void printEventsToday(LocalDate givenDate) {
    List<Event> allEvents = this.getEvents();
    List<Event> todayEvents = new ArrayList<>();

    for (Event event : allEvents) {
      if (event.onDate(givenDate)) {
        todayEvents.add(event);
      }
    }
    if (todayEvents.isEmpty()) {
      Messages.info("No events found for " + givenDate);
    } else {
      for (Event event : todayEvents) {
        event.printEvent();
      }
    }
  }

  /**
   * Handles {@code edit event <property> <subject> from <start> to <end> with <value>}.
   *
   * @param property     which field to mutate
   * @param eventSubject subject identifying the event
   * @param startText    original start timestamp
   * @param endText      original end timestamp
   * @param newValue     new value supplied by the user
   */
  public void handleEditEventFromTo(String property,
                                    String eventSubject,
                                    String startText,
                                    String endText,
                                    String newValue) {

    final LocalDateTime startTime;
    final LocalDateTime endTime;
    Event targetEvent = null;
    try {
      startTime = LocalDateTime.parse(startText);
      endTime = LocalDateTime.parse(endText);
      if (!startTime.isBefore(endTime)) {
        Messages.error("Start time must be before end time.");
        return;
      }
      targetEvent = this.findEvent(eventSubject, startTime, endTime);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
      return;
    }
    if (targetEvent == null) {
      Messages.error("no such event found.");
      return;
    }

    switch (property) {
      case "start":
        try {
          LocalDateTime time = LocalDateTime.parse(newValue);
          if (!time.isBefore(targetEvent.getEndTime())) {
            Messages.error("Start time must be before end time.");
            return;
          }
          List<ProposedState> proposals = List.of(
              new ProposedState(targetEvent, targetEvent.getSubject(),
                  time, targetEvent.getEndTime()));
          if (hasConflicts(proposals)) {
            return;
          }
          detachEventFromSeries(targetEvent);
          targetEvent.setStartTime(time);
        } catch (DateTimeParseException e) {
          Messages.error(DATETIME_ERROR);
        }
        break;

      case "end":
        try {
          LocalDateTime time = LocalDateTime.parse(newValue);
          if (!time.isAfter(targetEvent.getStartTime())) {
            Messages.error("End time must be after start time.");
            return;
          }
          List<ProposedState> proposals = List.of(
              new ProposedState(targetEvent, targetEvent.getSubject(),
                  targetEvent.getStartTime(), time));
          if (hasConflicts(proposals)) {
            return;
          }
          targetEvent.setEndTime(time);
        } catch (DateTimeParseException e) {
          Messages.error(DATETIME_ERROR);
        }
        break;

      case "description":
        targetEvent.setDescription(newValue);
        break;

      case "subject":
        List<ProposedState> subjectChange = List.of(
            new ProposedState(targetEvent, newValue,
                targetEvent.getStartTime(), targetEvent.getEndTime()));
        if (hasConflicts(subjectChange)) {
          return;
        }
        targetEvent.setSubject(newValue);
        break;

      case "location":
        targetEvent.setLocation(newValue);
        break;

      case "status":
        targetEvent.setStatus(newValue);
        break;

      case "isAllDay":
        if (!newValue.equalsIgnoreCase("true") && !newValue.equalsIgnoreCase("false")) {
          Messages.error("Please enter true or false.");
          return;
        }
        boolean isAllDay = Boolean.parseBoolean(newValue);
        if (isAllDay) {
          LocalDate date = targetEvent.getStartTime().toLocalDate();
          LocalDateTime newStart = LocalDateTime.of(date, LocalTime.of(8, 0));
          LocalDateTime newEnd = LocalDateTime.of(date, LocalTime.of(17, 0));
          List<ProposedState> proposals = List.of(
              new ProposedState(targetEvent, targetEvent.getSubject(), newStart, newEnd));
          if (hasConflicts(proposals)) {
            return;
          }
        }
        targetEvent.setIsAllDay(isAllDay);
        break;

      default:
        editInstruction();
        break;
    }
  }

  /**
   * Prints the events in a given time span, which includes events that only partially
   * overlap.
   *
   * @param start a date and time string when the time span starts
   * @param end   a date and time string when the time span ends
   */
  public void printEventsSpan(LocalDateTime start, LocalDateTime end) {
    List<Event> allEvents = this.getEvents();
    List<Event> spanEvents = new ArrayList<>();

    for (Event event : allEvents) {
      if (event.inSpan(start, end)) {
        spanEvents.add(event);
      }
    }

    if (spanEvents.isEmpty()) {
      Messages.info("No events found from " + start + " to " + end);
    } else {
      for (Event event : spanEvents) {
        event.printEvent();
      }
    }

  }

  /**
   * Looks to see if there is an existing event overlapping at a specific
   * date and time. Prints the availability status.
   *
   * @param checkDate a date and time we want to check for an event
   */
  public void printAvailability(LocalDateTime checkDate) {
    List<Event> allEvents = this.getEvents();
    boolean occupied = false;

    for (Event event : allEvents) {
      if (checkDate.isEqual(event.getStartTime()) || checkDate.isEqual(event.getEndTime())
          || (checkDate.isAfter(event.getStartTime()) && checkDate.isBefore(event.getEndTime()))) {
        occupied = true;
        break;
      }
    }

    if (occupied) {
      Messages.info("Occupied");
    } else {
      Messages.info("Available");
    }
  }

  private Event findEvent(String eventSubject, LocalDateTime startTime, LocalDateTime endTime) {

    // Find exact event (business rule: subject + start + end must match)
    Event targetEvent = null;
    for (Event event : this.getEvents()) {
      if (eventSubject.equals(event.getSubject())
          && startTime.equals(event.getStartTime())
          && endTime.equals(event.getEndTime())) {
        targetEvent = event;
        break;
      }
    }
    if (targetEvent == null) {
      Messages.error("No such event exists, try again.");
      return null;
    }
    return targetEvent;
  }

  /**
   * Handles {@code edit events <property> <subject> from <start> with <value>} which edits the
   * anchor event and all occurrences after it.
   *
   * @param property        field to mutate
   * @param eventSubject    subject identifying the series
   * @param anchorStartText start timestamp of the anchor event
   * @param newValue        new property value
   */
  public void handleEditEventsFromWith(String property,
                                       String eventSubject,
                                       String anchorStartText,
                                       String newValue) {

    final LocalDateTime anchorStart;
    try {
      anchorStart = LocalDateTime.parse(anchorStartText);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
      return;
    }

    // Business rule: anchors are events with exact (subject, anchor start)
    List<Event> anchors = new ArrayList<>();
    for (Event ev : this.getEvents()) {
      if (eventSubject.equals(ev.getSubject()) && anchorStart.equals(ev.getStartTime())) {
        anchors.add(ev);
      }
    }
    if (anchors.isEmpty()) {
      Messages.error("No such event exists, try again.");
      return;
    }

    if (anchors.size() > 1) {
      Messages.error("Multiple events match the provided subject and start time.");
      return;
    }

    Set<Event> targetEvents = new LinkedHashSet<>();
    for (Event anchor : anchors) {
      EventSeries series = anchor.getEventSeries();
      if (series != null) {
        Event first = series.firstAtOrAfter(anchorStart);
        if (first != null) {
          targetEvents.addAll(series.events().tailSet(first, true));
        }
      } else {
        targetEvents.add(anchor);
      }
    }
    if (targetEvents.isEmpty()) {
      Messages.error("No events at or after " + anchorStart + " to edit in the series.");
      return;
    }

    switch (property) {
      case "start":
        {
        final LocalDateTime newStartDateT;
        try {
          newStartDateT = LocalDateTime.parse(newValue);
        } catch (DateTimeParseException e) {
          Messages.error(DATETIME_ERROR);
          return;
        }

        LinkedHashSet<Event> changes = gatherSeriesTailEvents(anchors, anchorStart);
        if (changes.isEmpty()) {
          Messages.error("No events at or after " + anchorStart + " to edit in the series.");
          return;
        }

        List<ProposedState> proposals = new ArrayList<>();
        for (Event ev : changes) {
          LocalDateTime newStartForThis =
              ev.getStartTime().toLocalDate().equals(anchorStart.toLocalDate())
                  ? newStartDateT
                  : LocalDateTime.of(ev.getStartTime().toLocalDate(),
                  newStartDateT.toLocalTime());

          java.time.Duration dur =
              java.time.Duration.between(ev.getStartTime(), ev.getEndTime());

          LocalDateTime newEndForThis = newStartForThis.plus(dur);
          proposals.add(new ProposedState(ev, ev.getSubject(), newStartForThis, newEndForThis));
        }
        if (hasConflicts(proposals)) {
          return;
        }
        for (ProposedState proposal : proposals) {
          detachEventFromSeries(proposal.event);
          proposal.event.setEndTime(proposal.end);
          proposal.event.setStartTime(proposal.start);
        }
        break;
        }
      case "end":
        {
        final LocalDateTime newEndDateT;
        try {
          newEndDateT = LocalDateTime.parse(newValue);
        } catch (DateTimeParseException e) {
          Messages.error(DATETIME_ERROR);
          return;
        }
        Event anchor = anchors.get(0);
        List<ProposedState> proposals = new ArrayList<>();
        for (Event ev : targetEvents) {
          LocalDateTime desiredEnd = resolveNewEndTime(ev, anchor, newEndDateT);
          if (!desiredEnd.isAfter(ev.getStartTime())) {
            Messages.error("End time must be after start time for event "
                + ev.getSubject() + " on " + ev.getStartTime() + ".");
            return;
          }
          proposals.add(new ProposedState(ev, ev.getSubject(), ev.getStartTime(), desiredEnd));
        }
        if (hasConflicts(proposals)) {
          return;
        }
        for (ProposedState proposal : proposals) {
          proposal.event.setEndTime(proposal.end);
        }
        break;
        }
      case "description":
        for (Event ev : targetEvents) {
          ev.setDescription(newValue);
        }
        break;

      case "subject":
        List<ProposedState> subjectChanges = new ArrayList<>();
        for (Event ev : targetEvents) {
          subjectChanges.add(
              new ProposedState(ev, newValue, ev.getStartTime(), ev.getEndTime()));
        }
        if (hasConflicts(subjectChanges)) {
          return;
        }
        for (Event ev : targetEvents) {
          ev.setSubject(newValue);
        }
        break;

      case "location":
        for (Event ev : targetEvents) {
          ev.setLocation(newValue);
        }
        break;

      case "status":
        for (Event ev : targetEvents) {
          ev.setStatus(newValue);
        }
        break;

      case "isAllDay":
        if (!newValue.equalsIgnoreCase("true") && !newValue.equalsIgnoreCase("false")) {
          Messages.error("Please enter true or false to change if the event lasts all day.");
          return;
        }
        boolean isAllDayEvents = Boolean.parseBoolean(newValue);
        if (isAllDayEvents) {
          List<ProposedState> allDayChanges = new ArrayList<>();
          for (Event ev : targetEvents) {
            LocalDate date = ev.getStartTime().toLocalDate();
            LocalDateTime newStart = LocalDateTime.of(date, LocalTime.of(8, 0));
            LocalDateTime newEnd = LocalDateTime.of(date, LocalTime.of(17, 0));
            allDayChanges.add(
                new ProposedState(ev, ev.getSubject(), newStart, newEnd));
          }
          if (hasConflicts(allDayChanges)) {
            return;
          }
        }
        for (Event ev : targetEvents) {
          ev.setIsAllDay(isAllDayEvents);
        }
        break;


      default:
        editInstruction();
        break;
    }
  }

  /**
   * Handles {@code edit series <property> <subject> from <start> with <value>} which edits the
   * anchor event and the rest of the series (or entire series depending on property).
   *
   * @param property        field to mutate
   * @param eventSubject    subject identifying the series
   * @param anchorStartText anchor start timestamp
   * @param newValue        new property value
   */
  public void handleEditSeries(String property,
                               String eventSubject,
                               String anchorStartText,
                               String newValue) {

    final LocalDateTime anchorStart;
    try {
      anchorStart = LocalDateTime.parse(anchorStartText);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
      return;
    }

    List<Event> anchors = new ArrayList<>();
    for (Event ev : this.getEvents()) {
      if (eventSubject.equals(ev.getSubject()) && anchorStart.equals(ev.getStartTime())) {
        anchors.add(ev);
      }
    }
    if (anchors.isEmpty()) {
      Messages.error("No such event exists, try again.");
      return;
    }
    if (anchors.size() > 1) {
      Messages.error("Multiple events match the provided subject and start time.");
      return;
    }

    Event anchor = anchors.get(0);

    switch (property) {
      case "start":
        {
        final LocalDateTime newStartDateT;
        try {
          newStartDateT = LocalDateTime.parse(newValue);
        } catch (DateTimeParseException e) {
          Messages.error(DATETIME_ERROR);
          return;
        }

        LinkedHashSet<Event> targets = (anchor.getEventSeries() != null)
            ? gatherSeriesTailEvents(List.of(anchor), anchorStart)
            : new LinkedHashSet<>(List.of(anchor));

        if (targets.isEmpty()) {
          Messages.error("No events at or after " + anchorStart
              + " to edit in the series.");
          return;
        }

        List<ProposedState> proposals = new ArrayList<>();
        for (Event ev : targets) {
          java.time.Duration dur =
              java.time.Duration.between(ev.getStartTime(), ev.getEndTime());

          LocalDateTime newStartForThis =
              ev.getStartTime().toLocalDate().equals(anchorStart.toLocalDate())
                  ? newStartDateT
                  : LocalDateTime.of(ev.getStartTime().toLocalDate(),
                  newStartDateT.toLocalTime());

          LocalDateTime newEndForThis = newStartForThis.plus(dur);
          proposals.add(new ProposedState(ev, ev.getSubject(), newStartForThis, newEndForThis));
        }
        if (hasConflicts(proposals)) {
          return;
        }
        for (ProposedState proposal : proposals) {
          detachEventFromSeries(proposal.event);
          proposal.event.setEndTime(proposal.end);
          proposal.event.setStartTime(proposal.start);
        }
        break;
        }

      case "end":
        {
        final LocalDateTime newEndDateT;
        try {
          newEndDateT = LocalDateTime.parse(newValue);
        } catch (DateTimeParseException e) {
          Messages.error(DATETIME_ERROR);
          return;
        }

        Collection<Event> targets =
            (anchor.getEventSeries() != null) ? anchor.getEventSeries().events() :
                List.of(anchor);
        List<ProposedState> proposals = new ArrayList<>();
        for (Event ev : targets) {
          LocalDateTime desiredEnd = resolveNewEndTime(ev, anchor, newEndDateT);
          if (!desiredEnd.isAfter(ev.getStartTime())) {
            Messages.error("End time must be after start time for event "
                + ev.getSubject() + " on " + ev.getStartTime() + ".");
            return;
          }
          proposals.add(new ProposedState(ev, ev.getSubject(), ev.getStartTime(), desiredEnd));
        }
        if (hasConflicts(proposals)) {
          return;
        }
        for (ProposedState proposal : proposals) {
          proposal.event.setEndTime(proposal.end);
        }

        break;
        }

      case "description":
        {
        Collection<Event> targets =
            (anchor.getEventSeries() != null) ? anchor.getEventSeries().events() :
                List.of(anchor);
        for (Event ev : targets) {
          ev.setDescription(newValue);
        }
        break;
        }

      case "subject":
        {
        Collection<Event> targets =
            (anchor.getEventSeries() != null) ? anchor.getEventSeries().events() :
                List.of(anchor);
        List<ProposedState> proposals = new ArrayList<>();
        for (Event ev : targets) {
          proposals.add(new ProposedState(ev, newValue, ev.getStartTime(), ev.getEndTime()));
        }
        if (hasConflicts(proposals)) {
          return;
        }
        for (Event ev : targets) {
          ev.setSubject(newValue);
        }
        break;
        }

      case "location":
        {
        Collection<Event> targets =
            (anchor.getEventSeries() != null) ? anchor.getEventSeries().events() :
                List.of(anchor);
        for (Event ev : targets) {
          ev.setLocation(newValue);
        }
        break;
        }

      case "status":
        {
        Collection<Event> targets =
            (anchor.getEventSeries() != null) ? anchor.getEventSeries().events() :
                List.of(anchor);
        for (Event ev : targets) {
          ev.setStatus(newValue);
        }
        break;
        }

      case "isAllDay":
        {
        if (!newValue.equalsIgnoreCase("true") && !newValue.equalsIgnoreCase("false")) {
          Messages.error(
              "Please enter true or false to change if the event lasts all day.");
          return;
        }
        boolean isAllDaySeries = Boolean.parseBoolean(newValue);
        Collection<Event> targets =
            (anchor.getEventSeries() != null) ? anchor.getEventSeries().events()
                : List.of(anchor);
        if (isAllDaySeries) {
          List<ProposedState> proposals = new ArrayList<>();
          for (Event ev : targets) {
            LocalDate date = ev.getStartTime().toLocalDate();
            LocalDateTime newStart = LocalDateTime.of(date, LocalTime.of(8, 0));
            LocalDateTime newEnd = LocalDateTime.of(date, LocalTime.of(17, 0));
            proposals.add(new ProposedState(ev, ev.getSubject(), newStart, newEnd));
          }
          if (hasConflicts(proposals)) {
            return;
          }
        }
        for (Event ev : targets) {
          ev.setIsAllDay(isAllDaySeries);
        }
        break;
        }


      default:
        editInstruction();
        break;
    }
  }

  private static final class ProposedState {
    final Event event;
    final String subject;
    final LocalDateTime start;
    final LocalDateTime end;

    ProposedState(Event event, String subject, LocalDateTime start, LocalDateTime end) {
      this.event = event;
      this.subject = subject;
      this.start = start;
      this.end = end;
    }
  }

  private static final class ConvertedTime {
    final Event event;
    final LocalDateTime start;
    final LocalDateTime end;

    ConvertedTime(Event event, LocalDateTime start, LocalDateTime end) {
      this.event = event;
      this.start = start;
      this.end = end;
    }
  }
}
