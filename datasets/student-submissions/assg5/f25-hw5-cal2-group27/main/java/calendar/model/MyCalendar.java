package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A simple calendar implementation that stores events in a map keyed by event ID.
 */
public class MyCalendar {

  private static final Comparator<Event> EVENT_ORDER = Comparator
          .comparing(Event::getStart)
          .thenComparing(Event::getEnd)
          .thenComparing(Event::getSubject);

  private ZoneId zoneId;
  private final Map<UUID, Event> events;
  private final Map<EventKey, UUID> eventIndex;
  private final Map<SubjectStartKey, Set<UUID>> subjectStartIndex;
  private final NavigableSet<Event> sortedEvents;

  /**
   * Constructs a new {@code MyCalendar}.
   *
   * @param zoneId timezone used to interpret local dates/times
   */
  public MyCalendar(ZoneId zoneId) {
    this.zoneId = zoneId;
    this.events = new HashMap<>();
    this.eventIndex = new HashMap<>();
    this.subjectStartIndex = new HashMap<>();
    this.sortedEvents = new TreeSet<>(EVENT_ORDER);
  }

  /**
   * Returns the calendar's timezone.
   *
   * @return the current {@link ZoneId}
   */
  public ZoneId getZoneId() {
    return zoneId;
  }

  /**
   * Changes the calendar's timezone and converts all stored events so that they
   * refer to the same instant in time (using {@code withZoneSameInstant}).
   *
   * @param newZoneId the new timezone (must not be {@code null})
   * @throws IllegalArgumentException if {@code newZoneId} is {@code null}
   */
  public void setZoneId(ZoneId newZoneId) {
    if (newZoneId == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    if (newZoneId.equals(this.zoneId)) {
      return;
    }

    final List<Event> convertedEvents = events.values().stream()
            .map(event -> new EventBuilder()
                    .id(event.getId())
                    .seriesId(event.getSeriesId())
                    .subject(event.getSubject())
                    .start(event.getStart().withZoneSameInstant(newZoneId))
                    .end(event.getEnd().withZoneSameInstant(newZoneId))
                    .description(event.getDescription())
                    .location(event.getLocation())
                    .status(event.getStatus())
                    .recurrence(event.getRecurrence())
                    .build())
            .collect(Collectors.toList());

    events.clear();
    eventIndex.clear();
    subjectStartIndex.clear();
    sortedEvents.clear();

    for (Event converted : convertedEvents) {
      addEventInternal(converted);
    }

    this.zoneId = newZoneId;
  }

  /**
   * Adds a new event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if an identical event already exists
   */
  public void addEvent(Event event) {
    EventKey key = EventKey.from(event);
    if (eventIndex.containsKey(key)) {
      throw new IllegalArgumentException("Identical event already exists.");
    }
    addEventInternal(event, key);
  }

  /**
   * Adds a series of recurring events to the calendar.
   *
   * @param templateEvent  the first event in the series
   * @param recurrenceRule the rule for the recurrence
   * @return a list of events that conflicted and were not added
   */
  public List<Event> addEventSeries(Event templateEvent, RecurrenceRule recurrenceRule) {
    if (!templateEvent.getStart().toLocalDate()
            .equals(templateEvent.getEnd().toLocalDate())) {
      throw new IllegalArgumentException(
              "Recurring events must start and end on the same day.");
    }
    List<Event> conflictingEvents = new ArrayList<>();

    try {
      addEvent(templateEvent);
    } catch (IllegalArgumentException e) {
      conflictingEvents.add(templateEvent);
      return conflictingEvents;
    }

    int occurrences = 1;
    ZonedDateTime currentStart = templateEvent.getStart();
    Duration duration = Duration.between(templateEvent.getStart(), templateEvent.getEnd());

    while (true) {
      Optional<ZonedDateTime> maybeNextStart =
              recurrenceRule.nextOccurrence(currentStart, occurrences);
      if (maybeNextStart.isEmpty()) {
        break;
      }
      ZonedDateTime nextStart = maybeNextStart.get();

      Event nextEvent = templateEvent.toBuilder()
              .start(nextStart)
              .end(nextStart.plus(duration))
              .build();

      try {
        addEvent(nextEvent);
      } catch (IllegalArgumentException e) {
        conflictingEvents.add(nextEvent);
      }

      occurrences++;
      currentStart = nextStart;
    }
    return conflictingEvents;
  }

  /**
   * Gets all events in the calendar.
   *
   * @return a new list containing all events in the calendar
   */
  public List<Event> getEvents() {
    return new ArrayList<>(sortedEvents);
  }

  /**
   * Gets all events that occur on a specific date.
   *
   * @param date the date to check for events
   * @return a list of events on the given date
   */
  public List<Event> getEventsOnDate(LocalDate date) {
    return sortedEvents.stream()
            .filter(event -> occursOnDate(event, date))
            .collect(Collectors.toList());
  }

  /**
   * Gets all events that overlap with the given time range (inclusive on both ends).
   *
   * @param startRange the start of the query range
   * @param endRange   the end of the query range
   * @return a list of overlapping events
   */
  public List<Event> getEventsInRange(ZonedDateTime startRange, ZonedDateTime endRange) {
    return sortedEvents.stream()
            .filter(event -> event.getStart().isBefore(endRange)
                    && event.getEnd().isAfter(startRange))
            .collect(Collectors.toList());
  }

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date and time to check
   * @return {@code true} if busy, otherwise {@code false}
   */
  public boolean isBusy(ZonedDateTime dateTime) {
    return events.values().stream()
            .anyMatch(event ->
                    !dateTime.isBefore(event.getStart()) && dateTime.isBefore(event.getEnd()));
  }

  /**
   * Finds a single event by its exact subject, start, and end time.
   *
   * @param subject the subject of the event
   * @param start   the start time of the event
   * @param end     the end time of the event
   * @return an {@link Optional} containing the event if found, otherwise empty
   */
  public Optional<Event> findEvent(String subject, ZonedDateTime start, ZonedDateTime end) {
    EventKey key = new EventKey(subject, start, end);
    UUID eventId = eventIndex.get(key);
    return Optional.ofNullable(eventId).map(events::get);
  }

  /**
   * Finds events by their subject and start time.
   *
   * @param subject the subject of the event
   * @param start   the start time of the event
   * @return a list of matching events
   */
  public List<Event> findEventsBySubjectAndStart(String subject, ZonedDateTime start) {
    SubjectStartKey key = new SubjectStartKey(subject, start);
    Set<UUID> ids = subjectStartIndex.get(key);
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }
    List<Event> result = new ArrayList<>(ids.size());
    for (UUID id : ids) {
      Event event = events.get(id);
      if (event != null) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Replaces a single event with a new one.
   *
   * @param eventId  the ID of the event to replace
   * @param newEvent the new event to insert
   * @throws IllegalArgumentException if the replacement would cause a duplicate event
   */
  public void replaceEvent(UUID eventId, Event newEvent) {
    Event existing = events.get(eventId);
    if (existing == null) {
      return;
    }
    EventKey newKey = EventKey.from(newEvent);
    UUID duplicateId = eventIndex.get(newKey);
    if (duplicateId != null && !duplicateId.equals(eventId)) {
      throw new IllegalArgumentException(
              "Editing would result in a duplicate event.");
    }
    removeEventInternal(existing);
    addEventInternal(newEvent, newKey);
  }

  /**
   * Updates events in a series from a given date onwards.
   *
   * @param seriesId           the ID of the series to update
   * @param fromDate           the date from which to start updating
   * @param requiresNewSeriesId whether a new series ID is required
   * @param updater            a mutator applied to each cloned {@link EventBuilder}
   */
  public void updateEventsFrom(
          UUID seriesId,
          ZonedDateTime fromDate,
          boolean requiresNewSeriesId,
          Consumer<EventBuilder> updater) {

    List<Event> toUpdate = events.values().stream()
            .filter(e -> seriesId.equals(e.getSeriesId()) && !e.getStart().isBefore(fromDate))
            .collect(Collectors.toList());
    performSeriesUpdate(toUpdate, seriesId, requiresNewSeriesId, updater, fromDate);
  }

  /**
   * Updates all events in an entire series.
   *
   * @param seriesId            the ID of the series to update
   * @param requiresNewSeriesId whether a new series ID is required
   * @param updater             a mutator applied to each cloned {@link EventBuilder}
   */
  public void updateEntireSeries(
          UUID seriesId,
          ZonedDateTime pivotStart,
          boolean requiresNewSeriesId,
          Consumer<EventBuilder> updater) {

    List<Event> toUpdate = events.values().stream()
            .filter(e -> seriesId.equals(e.getSeriesId()))
            .collect(Collectors.toList());
    performSeriesUpdate(toUpdate, seriesId, requiresNewSeriesId, updater, pivotStart);
  }

  private void performSeriesUpdate(
          List<Event> toUpdate,
          UUID seriesId,
          boolean requiresNewSeriesId,
          Consumer<EventBuilder> updater,
          ZonedDateTime pivotStart) {

    if (toUpdate.isEmpty()) {
      return;
    }

    UUID newSeriesId = requiresNewSeriesId ? UUID.randomUUID() : seriesId;

    List<Event> orderedEvents = new ArrayList<>(toUpdate);
    orderedEvents.sort(Comparator
            .comparing((Event e) -> pivotStart == null
                    || !e.getStart().equals(pivotStart))
            .thenComparing(EVENT_ORDER));

    Duration startShift = null;
    List<Event> updatedEvents = new ArrayList<>();
    for (Event event : orderedEvents) {
      final Duration originalDuration = Duration.between(event.getStart(), event.getEnd());
      EventBuilder builder = event.toBuilder().seriesId(newSeriesId);
      updater.accept(builder);
      Event updatedEvent = builder.build();

      if (!event.getStart().equals(updatedEvent.getStart())) {
        if (startShift == null) {
          startShift = Duration.between(event.getStart(), updatedEvent.getStart());
        }
      }

      if (startShift != null) {
        ZonedDateTime shiftedStart = event.getStart().plus(startShift);
        if (!shiftedStart.equals(updatedEvent.getStart())) {
          updatedEvent = updatedEvent.toBuilder().start(shiftedStart).build();
        }
      }

      if (!event.getStart().equals(updatedEvent.getStart())
              && event.getEnd().equals(updatedEvent.getEnd())) {
        updatedEvent = updatedEvent.toBuilder()
                .end(updatedEvent.getStart().plus(originalDuration))
                .build();
      }

      updatedEvents.add(updatedEvent);
    }

    List<UUID> toUpdateIds =
            orderedEvents.stream().map(Event::getId).collect(Collectors.toList());

    for (Event updatedEvent : updatedEvents) {
      EventKey key = EventKey.from(updatedEvent);
      UUID duplicateId = eventIndex.get(key);
      if (duplicateId != null && !toUpdateIds.contains(duplicateId)) {
        throw new IllegalArgumentException(
                "Editing series would result in a duplicate event.");
      }
    }

    orderedEvents.forEach(this::removeEventInternal);
    for (Event updatedEvent : updatedEvents) {
      addEventInternal(updatedEvent);
    }
  }

  private void addEventInternal(Event event) {
    addEventInternal(event, EventKey.from(event));
  }

  private void addEventInternal(Event event, EventKey key) {
    events.put(event.getId(), event);
    eventIndex.put(key, event.getId());
    sortedEvents.add(event);

    SubjectStartKey startKey =
            new SubjectStartKey(event.getSubject(), event.getStart());

    subjectStartIndex
            .computeIfAbsent(startKey, ignored -> new LinkedHashSet<>())
            .add(event.getId());
  }

  private void removeEventInternal(Event event) {
    events.remove(event.getId());
    eventIndex.remove(EventKey.from(event));
    sortedEvents.remove(event);

    SubjectStartKey subjectKey =
            new SubjectStartKey(event.getSubject(), event.getStart());

    Set<UUID> ids = subjectStartIndex.get(subjectKey);
    if (ids != null) {
      ids.remove(event.getId());
      if (ids.isEmpty()) {
        subjectStartIndex.remove(subjectKey);
      }
    }
  }

  private boolean occursOnDate(Event event, LocalDate date) {
    LocalDate eventStart = event.getStart().toLocalDate();
    LocalDate eventEnd = event.getEnd().toLocalDate();
    return !date.isBefore(eventStart) && !date.isAfter(eventEnd);
  }

  private static final class EventKey {
    private final String subject;
    private final ZonedDateTime start;
    private final ZonedDateTime end;

    private EventKey(String subject, ZonedDateTime start, ZonedDateTime end) {
      this.subject = subject;
      this.start = start;
      this.end = end;
    }

    static EventKey from(Event event) {
      return new EventKey(event.getSubject(), event.getStart(), event.getEnd());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof EventKey)) {
        return false;
      }
      EventKey eventKey = (EventKey) o;
      return subject.equals(eventKey.subject)
              && start.equals(eventKey.start)
              && end.equals(eventKey.end);
    }

    @Override
    public int hashCode() {
      int result = subject.hashCode();
      result = 31 * result + start.hashCode();
      result = 31 * result + end.hashCode();
      return result;
    }
  }

  private static final class SubjectStartKey {
    private final String subject;
    private final ZonedDateTime start;

    private SubjectStartKey(String subject, ZonedDateTime start) {
      this.subject = subject;
      this.start = start;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof SubjectStartKey)) {
        return false;
      }
      SubjectStartKey that = (SubjectStartKey) o;
      return subject.equals(that.subject) && start.equals(that.start);
    }

    @Override
    public int hashCode() {
      int result = subject.hashCode();
      result = 31 * result + start.hashCode();
      return result;
    }
  }
}
