package calendar.model.calendar;

import calendar.model.event.EventSeries;
import calendar.model.event.Ievent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of a calendar that manages events and event series.
 * Ensures no two events have the same subject, start time, and end time.
 */
public class Calendar implements Icalendar {

  private final List<Ievent> events;
  private final List<EventSeries> eventSeries;
  private final Map<String, Set<Ievent>> eventIndex;

  /**
   * Constructs an empty calendar.
   */
  public Calendar() {
    this.events = new ArrayList<>();
    this.eventSeries = new ArrayList<>();
    this.eventIndex = new HashMap<>();
  }

  /**
   * Adds an event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event is null
   * @throws IllegalStateException    if an identical event already exists
   */
  @Override
  public void addEvent(Ievent event) throws IllegalArgumentException, IllegalStateException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    String eventKey = createEventKey(event);

    if (eventIndex.containsKey(eventKey)) {
      throw new IllegalStateException(
          String.format("An event with subject '%s', start '%s', and end '%s' already exists",
              event.getSubject(), event.getStartDateTime(), event.getEndDateTime()));
    }

    events.add(event);
    eventIndex.computeIfAbsent(eventKey, k -> new HashSet<>()).add(event);
  }

  /**
   * Adds an event series to the calendar.
   *
   * @param series the event series to add
   * @throws IllegalArgumentException if series is null
   * @throws IllegalStateException    if any event in the series conflicts
   */
  @Override
  public void addEventSeries(EventSeries series)
      throws IllegalArgumentException, IllegalStateException {
    if (series == null) {
      throw new IllegalArgumentException("Event series cannot be null");
    }

    List<Ievent> seriesEvents = series.getEvents();
    for (Ievent event : seriesEvents) {
      if (!canAddEvent(event)) {
        throw new IllegalStateException(
            String.format("Event in series conflicts with existing event: %s at %s",
                event.getSubject(), event.getStartDateTime()));
      }
    }

    for (Ievent event : seriesEvents) {
      events.add(event);
      String eventKey = createEventKey(event);
      eventIndex.computeIfAbsent(eventKey, k -> new HashSet<>()).add(event);
    }

    eventSeries.add(series);
  }

  /**
   * Removes an event from the calendar and any containing series.
   *
   * @param event the event to remove
   * @return true if the event was found and removed
   */
  @Override
  public boolean removeEvent(Ievent event) {
    if (event == null) {
      return false;
    }

    String eventKey = createEventKey(event);
    Set<Ievent> indexedEvents = eventIndex.get(eventKey);

    if (indexedEvents != null) {
      indexedEvents.remove(event);
      eventIndex.remove(eventKey);
    }

    Optional<EventSeries> containingSeries = findSeriesContaining(event);
    containingSeries.ifPresent(series -> series.removeEvent(event));

    return events.remove(event);
  }

  /**
   * Finds an event by subject and start time.
   *
   * @param subject       the event subject
   * @param startDateTime the event start time
   * @return the matching event, or empty if not found
   */
  @Override
  public Optional<Ievent> findEvent(String subject, LocalDateTime startDateTime) {
    return events.stream().filter(e -> e.getSubject().equals(subject)
        && e.getStartDateTime().equals(startDateTime)).findFirst();
  }

  /**
   * Finds all events with the given subject.
   *
   * @param subject the subject to search for
   * @return list of matching events sorted by start time
   */
  @Override
  public List<Ievent> findEventsBySubject(String subject) {
    return events.stream()
        .filter(e -> e.getSubject().equals(subject))
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Gets all events occurring on the specified date.
   *
   * @param date the date to check
   * @return list of events sorted by start time
   */
  @Override
  public List<Ievent> getEventsOnDate(LocalDate date) {
    return events.stream()
        .filter(e -> {
          LocalDate startDate = e.getStartDateTime().toLocalDate();
          LocalDate endDate = e.getEndDateTime().toLocalDate();
          return !date.isBefore(startDate) && !date.isAfter(endDate);
        })
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Gets events within the specified time range.
   *
   * @param startDateTime range start (inclusive)
   * @param endDateTime   range end (exclusive)
   * @return list of overlapping events sorted by start time
   */
  @Override
  public List<Ievent> getEventsInRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return events.stream()
        .filter(e -> e.getStartDateTime().isBefore(endDateTime)
            && e.getEndDateTime().isAfter(startDateTime))
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Checks if the calendar has any events at the specified time.
   *
   * @param dateTime the time to check
   * @return true if an event is occurring at this time
   */
  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    return events.stream()
        .anyMatch(e -> !dateTime.isBefore(e.getStartDateTime())
            && dateTime.isBefore(e.getEndDateTime()));
  }

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events sorted by start time
   */
  @Override
  public List<Ievent> getAllEvents() {
    return new ArrayList<>(events).stream()
        .sorted(Comparator.comparing(Ievent::getStartDateTime))
        .collect(Collectors.toList());
  }

  /**
   * Gets all event series in the calendar.
   *
   * @return list of all event series
   */
  @Override
  public List<EventSeries> getAllEventSeries() {
    return new ArrayList<>(eventSeries);
  }

  /**
   * Finds the series containing the specified event.
   *
   * @param event the event to search for
   * @return the containing series, or empty if not in a series
   */
  @Override
  public Optional<EventSeries> findSeriesContaining(Ievent event) {
    return eventSeries.stream()
        .filter(series -> series.containsEvent(event))
        .findFirst();
  }

  /**
   * Checks if an event can be added without conflicts.
   *
   * @param event the event to check
   * @return true if the event can be added
   */
  @Override
  public boolean canAddEvent(Ievent event) {
    if (event == null) {
      return false;
    }

    String eventKey = createEventKey(event);
    return !eventIndex.containsKey(eventKey);
  }

  /**
   * Removes all events and series from the calendar.
   */
  @Override
  public void clear() {
    events.clear();
    eventSeries.clear();
    eventIndex.clear();
  }

  /**
   * Returns the total number of events.
   *
   * @return the event count
   */
  @Override
  public int size() {
    return events.size();
  }

  /**
   * Creates a unique key for an event based on subject, start, and end times.
   *
   * @param event the event to create a key for
   * @return a string key identifying the event
   */
  private String createEventKey(Ievent event) {
    return String.format("%s|%s|%s",
        event.getSubject(),
        event.getStartDateTime(),
        event.getEndDateTime());
  }

  /**
   * Updates an event in a series with different modification scopes.
   *
   * @param originalEvent the original event to modify
   * @param modifiedEvent the new version of the event
   * @param modifyType    scope of modification (SINGLE, FROM_THIS, ALL)
   * @throws IllegalStateException    if modification creates conflicts
   * @throws IllegalArgumentException if events are null or original not found
   */
  public void updateEventInSeries(Ievent originalEvent, Ievent modifiedEvent,
                                  EventSeries.ModificationType modifyType)
      throws IllegalStateException, IllegalArgumentException {

    if (originalEvent == null) {
      throw new IllegalArgumentException("Original event cannot be null");
    }
    if (modifiedEvent == null) {
      throw new IllegalArgumentException("Modified event cannot be null");
    }

    if (!originalEvent.conflictsWith(modifiedEvent) && !canAddEvent(modifiedEvent)) {
      throw new IllegalStateException(
          String.format(
              "Cannot modify event: an event with subject '%s', start '%s', "
                  + "and end '%s' already exists",
              modifiedEvent.getSubject(), modifiedEvent.getStartDateTime(),
              modifiedEvent.getEndDateTime()));
    }

    Optional<EventSeries> seriesOpt = findSeriesContaining(originalEvent);

    if (seriesOpt.isEmpty()) {
      if (!removeEvent(originalEvent)) {
        throw new IllegalArgumentException("Original event not found in calendar");
      }
      addEvent(modifiedEvent);
      return;
    }

    EventSeries series = seriesOpt.get();

    switch (modifyType) {
      case SINGLE:
        series.removeEvent(originalEvent);
        removeEvent(originalEvent);
        addEvent(modifiedEvent);

        if (series.isEmpty()) {
          eventSeries.remove(series);
        }
        break;

      case FROM_THIS:
        List<Ievent> eventsToModify = series.getEventsFromDate(originalEvent.getStartDateTime());

        // Check for conflicts first
        for (Ievent e : eventsToModify) {
          if (!e.equals(originalEvent)) {
            Ievent wouldBeModified = applyModifications(e, originalEvent, modifiedEvent);
            if (!e.conflictsWith(wouldBeModified) && !canAddEvent(wouldBeModified)) {
              throw new IllegalStateException(
                  String.format("Cannot modify series: would create conflict at %s",
                      e.getStartDateTime()));
            }
          }
        }

        boolean timeChanged = !originalEvent.getStartDateTime().toLocalTime()
            .equals(modifiedEvent.getStartDateTime().toLocalTime());

        for (Ievent e : eventsToModify) {
          removeEvent(e);
          series.removeEvent(e);
          Ievent modified = applyModifications(e, originalEvent, modifiedEvent);
          addEvent(modified);
        }

        if (series.isEmpty()) {
          eventSeries.remove(series);
        }
        break;

      case ALL:
        List<Ievent> allEvents = new ArrayList<>(series.getEvents());

        // Check for conflicts first
        for (Ievent e : allEvents) {
          Ievent wouldBeModified = applyModifications(e, originalEvent, modifiedEvent);
          if (!e.conflictsWith(wouldBeModified) && !canAddEvent(wouldBeModified)) {
            throw new IllegalStateException(
                String.format("Cannot modify series: would create conflict at %s",
                    e.getStartDateTime()));
          }
        }

        LocalTime originalTime = originalEvent.getStartDateTime().toLocalTime();
        LocalTime modifiedTime = modifiedEvent.getStartDateTime().toLocalTime();

        for (Ievent e : allEvents) {
          removeEvent(e);
          Ievent modified = applyModifications(e, originalEvent, modifiedEvent);
          addEvent(modified);
        }

        // Remove series if time changed
        if (originalTime.compareTo(modifiedTime) != 0) {
          eventSeries.remove(series);
        }
        break;

      default:
        throw new IllegalArgumentException("Unknown modification type: " + modifyType);
    }
  }

  /**
   * Applies changes from a modified event to a target event.
   *
   * @param target   the event to apply changes to
   * @param original the original event before modification
   * @param modified the modified version with changes
   * @return a new event with modifications applied
   */
  private Ievent applyModifications(Ievent target, Ievent original, Ievent modified) {
    Ievent result = target;

    if (!original.getSubject().equals(modified.getSubject())) {
      result = result.withSubject(modified.getSubject());
    }

    if (!original.getEndDateTime().toLocalTime().equals(modified.getEndDateTime().toLocalTime())) {
      LocalDateTime newEnd = target.getEndDateTime().toLocalDate()
          .atTime(modified.getEndDateTime().toLocalTime());
      result = result.withEndDateTime(newEnd);
    }

    if (!original.getStartDateTime().toLocalTime()
        .equals(modified.getStartDateTime().toLocalTime())) {
      LocalDateTime newStart = target.getStartDateTime().toLocalDate()
          .atTime(modified.getStartDateTime().toLocalTime());
      result = result.withStartDateTime(newStart);
    }

    if (!Objects.equals(original.getDescription(), modified.getDescription())) {
      result = result.withDescription(modified.getDescription());
    }

    if (!Objects.equals(original.getLocation(), modified.getLocation())) {
      result = result.withLocation(modified.getLocation());
    }

    if (original.getStatus() != modified.getStatus()) {
      result = result.withStatus(modified.getStatus());
    }

    return result;
  }
}