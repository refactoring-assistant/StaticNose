package calendarmodel;

import calendarmodel.enums.EditMode;
import calendarmodel.enums.Location;
import calendarmodel.exceptions.AmbiguousEditException;
import calendarmodel.exceptions.DuplicateEventException;
import calendarmodel.exceptions.EventNotFoundException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the CalendarModel interface.
 *
 * <p>This class manages all event data and business logic for the calendar.
 * It uses a List for chronological operations and Maps for efficient
 * lookups, duplicate checking, and series management.</p>
 */
public class CalendarModelImpl implements CalendarModel {

  private final List<Event> allEvents = new ArrayList<>();
  private final Map<EventKey, Event> eventMap = new HashMap<>();
  private final Map<String, List<Event>> seriesMap = new HashMap<>();

  /**
   * An immutable key for the eventMap, based on the assignment's uniqueness rules.
   */
  private static final class EventKey {

    private final String subject;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    EventKey(Event event) {
      this.subject = event.getSubject();
      this.startTime = event.getStartTime();
      this.endTime = event.getEndTime();
    }

    EventKey(String subject, LocalDateTime startTime, LocalDateTime endTime) {
      this.subject = subject;
      this.startTime = startTime;
      this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      EventKey eventKey = (EventKey) o;
      return Objects.equals(subject, eventKey.subject)
          && Objects.equals(startTime, eventKey.startTime)
          && Objects.equals(endTime, eventKey.endTime);
    }

    @Override
    public int hashCode() {
      return Objects.hash(subject, startTime, endTime);
    }

    @Override
    public String toString() {
      return "EventKey{"
          + "subject='" + subject + '\''
          + ", startTime=" + startTime
          + ", endTime=" + endTime
          + '}';
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createSingleEvent(Event newEvent) throws DuplicateEventException {
    checkIfEventExists(newEvent);
    addEventToStorage(newEvent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createEventSeries(Event prototype, List<DayOfWeek> weekdays, int numOccurrences)
      throws DuplicateEventException {
    List<Event> generatedEvents = generateEventSeries(prototype, weekdays, numOccurrences, null);
    checkFordDuplicates(generatedEvents);
    for (Event e : generatedEvents) {
      addEventToStorage(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createEventSeries(Event prototype, List<DayOfWeek> weekdays, LocalDate untilDate)
      throws DuplicateEventException {
    List<Event> generatedEvents = generateEventSeries(prototype, weekdays, -1, untilDate);
    checkFordDuplicates(generatedEvents);
    for (Event e : generatedEvents) {
      addEventToStorage(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Event> getEventsOn(LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDayExclusive = date.plusDays(1).atStartOfDay();
    return getEventsFrom(startOfDay, endOfDayExclusive);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Event> getEventsFrom(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    return allEvents.stream()
        .filter(e -> e.getStartTime().isBefore(rangeEnd) && e.getEndTime().isAfter(rangeStart))
        .sorted()
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return allEvents.stream()
        .anyMatch(e -> !dateTime.isBefore(e.getStartTime()) && dateTime.isBefore(e.getEndTime()));
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void editSingleEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                              String propertyToChange, Object newValue)
      throws EventNotFoundException, DuplicateEventException {

    EventKey key = new EventKey(subject, startTime, endTime);
    Event eventToEdit = eventMap.get(key);

    if (eventToEdit == null) {
      throw new EventNotFoundException("No event found with specified subject, start, and end.");
    }

    boolean isSplitting =
        "start".equalsIgnoreCase(propertyToChange) && eventToEdit.isPartOfSeries();
    String originalSeriesId = eventToEdit.getSeriesId();

    Event newEvent = buildNewEventFromOld(eventToEdit, propertyToChange, newValue);

    if (isSplitting) {
      newEvent = Event.newBuilder(newEvent).withSeriesId(null).build();
    }

    performEditTransaction(List.of(eventToEdit), List.of(newEvent));

    if (isSplitting && originalSeriesId != null) {
      List<Event> series = seriesMap.get(originalSeriesId);
      if (series != null) {
        Collections.sort(series);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void editEventSeries(String subject, LocalDateTime startTime, EditMode mode,
                              String propertyToChange, Object newValue)
      throws EventNotFoundException, AmbiguousEditException, DuplicateEventException,
      IllegalStateException {

    List<Event> matchingEvents = findEventsByAmbiguousKey(subject, startTime);

    if (matchingEvents.isEmpty()) {
      throw new EventNotFoundException(
          "No event found matching subject '" + subject + "' and start time " + startTime);
    }
    if (matchingEvents.size() > 1) {
      throw new AmbiguousEditException(
          "Multiple events match subject and start; edit is ambiguous.");
    }

    Event seedEvent = matchingEvents.get(0);
    String seriesId = seedEvent.getSeriesId();

    if (seriesId == null) {
      throw new IllegalStateException("editEventSeries cannot be called on a non-series event.");
    }

    List<Event> allEventsInSeries = getEventsInSeries(seriesId);
    if (allEventsInSeries.isEmpty()) {
      throw new IllegalStateException(
          "Series data is inconsistent. Seed event series ID not found in seriesMap: " + seriesId);
    }

    boolean isSplittingProperty = "start".equalsIgnoreCase(propertyToChange)
        || "subject".equalsIgnoreCase(propertyToChange);
    final String newSeriesId = (isSplittingProperty && mode == EditMode.THIS_AND_FUTURE)
        ? UUID.randomUUID().toString() : null;

    Duration startDelta = null;
    if ("start".equalsIgnoreCase(propertyToChange)) {
      if (!(newValue instanceof LocalDateTime)) {
        throw new IllegalArgumentException("New value for 'start' must be LocalDateTime");
      }

      startDelta = Duration.between(seedEvent.getStartTime(), (LocalDateTime) newValue);
    }

    List<Event> eventsToChange;
    int seedIndex = -1;
    for (int i = 0; i < allEventsInSeries.size(); i++) {
      if (allEventsInSeries.get(i).equals(seedEvent)) {
        seedIndex = i;
        break;
      }
    }

    if (seedIndex == -1) {
      throw new IllegalStateException(
          "Seed event not found within its own retrieved series list. Series ID: " + seriesId);
    }

    if (mode == EditMode.ALL_IN_SERIES) {
      eventsToChange = new ArrayList<>(allEventsInSeries);
    } else {
      eventsToChange =
          new ArrayList<>(allEventsInSeries.subList(seedIndex, allEventsInSeries.size()));
    }

    List<Event> newEvents = new ArrayList<>();
    for (Event oldEvent : eventsToChange) {
      Object effectiveNewValue = newValue;
      if ("start".equalsIgnoreCase(propertyToChange) && startDelta != null) {
        effectiveNewValue = oldEvent.getStartTime().plus(startDelta);
      }

      Event newEvent = buildNewEventFromOld(oldEvent, propertyToChange, effectiveNewValue);

      if (newSeriesId != null) {
        newEvent = Event.newBuilder(newEvent).withSeriesId(newSeriesId).build();
      }
      newEvents.add(newEvent);
    }
    performEditTransaction(eventsToChange, newEvents);
    if (newSeriesId != null) {
      List<Event> oldSeriesList = seriesMap.get(seriesId);
      if (oldSeriesList != null) {
        Collections.sort(oldSeriesList);
      }
      List<Event> newSeriesList = newEvents.stream()
          .filter(e -> newSeriesId.equals(e.getSeriesId()))
          .sorted()
          .collect(Collectors.toList());
      if (!newSeriesList.isEmpty()) {
        seriesMap.put(newSeriesId, newSeriesList);
      }
    } else {
      List<Event> currentSeriesList = seriesMap.get(seriesId);
      if (currentSeriesList != null) {
        List<Event> updatedSeries = allEvents.stream()
            .filter(e -> seriesId.equals(e.getSeriesId()))
            .sorted()
            .collect(Collectors.toList());
        seriesMap.put(seriesId, updatedSeries);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Event> getAllEvents() {
    List<Event> sortedCopy = new ArrayList<>(this.allEvents);
    Collections.sort(sortedCopy);
    return sortedCopy;
  }

  private List<Event> generateEventSeries(Event prototype, List<DayOfWeek> weekdays,
                                          int numOccurrences, LocalDate untilDate) {
    List<Event> generatedEvents = new ArrayList<>();
    String seriesId = UUID.randomUUID().toString();
    Duration duration = Duration.between(prototype.getStartTime(), prototype.getEndTime());
    LocalDateTime eventStart = prototype.getStartTime();
    LocalTime startTimeOfDay = eventStart.toLocalTime();
    boolean byCount = numOccurrences > 0;
    int count = 0;
    LocalDate currentDate = eventStart.toLocalDate();
    while (true) {
      eventStart = currentDate.atTime(startTimeOfDay);
      if (byCount && count >= numOccurrences) {
        break;
      }
      if (!byCount && currentDate.isAfter(untilDate)) {
        break;
      }
      if (weekdays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventEnd = eventStart.plus(duration);
        if (!eventEnd.toLocalDate().equals(currentDate)) {
          System.err.println(
              "Warning: Series event prototype spans midnight, skipping occurrence at "
                  + eventStart);
        } else {
          Event newEvent = Event.newBuilder(prototype)
              .withStartTime(eventStart)
              .withEndTime(eventEnd)
              .withSeriesId(seriesId)
              .build();
          generatedEvents.add(newEvent);
        }
        count++;
      }
      currentDate = currentDate.plusDays(1);
    }
    return generatedEvents;
  }

  private Event buildNewEventFromOld(Event oldEvent, String property, Object newValue) {
    Event.Builder builder = Event.newBuilder(oldEvent);

    switch (property.toLowerCase()) {
      case "subject":
        if (!(newValue instanceof String)) {
          throw new IllegalArgumentException("New value for 'subject' must be String");
        }
        builder.withSubject((String) newValue);
        break;
      case "start":
        if (!(newValue instanceof LocalDateTime)) {
          throw new IllegalArgumentException("New value for 'start' must be LocalDateTime");
        }
        LocalDateTime newStart = (LocalDateTime) newValue;
        Duration duration = Duration.between(oldEvent.getStartTime(), oldEvent.getEndTime());
        builder.withStartTime(newStart);
        builder.withEndTime(newStart.plus(duration));
        break;
      case "end":
        if (!(newValue instanceof LocalDateTime)) {
          throw new IllegalArgumentException("New value for 'end' must be LocalDateTime");
        }
        LocalDateTime newEnd = (LocalDateTime) newValue;
        if (newEnd.isBefore(oldEvent.getStartTime())) {
          throw new IllegalArgumentException(
              "New end time cannot be before the event's start time.");
        }
        builder.withEndTime(newEnd);
        break;
      case "description":
        if (newValue != null && !(newValue instanceof String)) {
          throw new IllegalArgumentException("New value for 'description' must be String or null");
        }
        builder.withDescription((String) newValue);
        break;
      case "location":
        if (newValue != null && !(newValue instanceof Location)) {
          throw new IllegalArgumentException(
              "New value for 'location' must be Location enum or null");
        }
        builder.withLocation((Location) newValue);
        break;
      case "status":
        if (newValue != null && !(newValue instanceof String)) {
          throw new IllegalArgumentException("New value for 'status' must be String or null");
        }
        builder.withStatus((String) newValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown property to edit: " + property);
    }
    return builder.build();
  }


  private void performEditTransaction(List<Event> oldEvents, List<Event> newEvents)
      throws DuplicateEventException {

    List<Event> successfullyRemoved = new ArrayList<>();
    for (Event e : oldEvents) {
      if (removeEventFromStorage(e)) {
        successfullyRemoved.add(e);
      } else {
        System.err.println(
            "Warning: Event to be edited was not found in storage: " + e.getSubject() + " at "
                + e.getStartTime());
      }
    }

    List<Event> addedSoFar = new ArrayList<>();
    DuplicateEventException duplicateFound = null;
    try {
      for (Event e : newEvents) {
        checkIfEventExists(e);
        addEventToStorage(e);
        addedSoFar.add(e);
      }
    } catch (DuplicateEventException e) {
      duplicateFound = e;
    }

    if (duplicateFound != null) {
      for (Event added : addedSoFar) {
        removeEventFromStorage(added);
      }
      for (Event old : successfullyRemoved) {
        try {
          addEventToStorage(old);
        } catch (IllegalStateException rollbackEx) {
          throw new RuntimeException(
              "Rollback failed due to unexpected duplicate on re-adding original event.",
              rollbackEx);
        }
      }
      throw new DuplicateEventException("Edit failed, results in conflict with an existing event: "
          + duplicateFound.getMessage());
    }
  }

  private void checkIfEventExists(Event event) throws DuplicateEventException {
    EventKey key = new EventKey(event);
    if (eventMap.containsKey(key)) {
      throw new DuplicateEventException(String.format(
          "Event '%s' from %s to %s conflicts with an existing event.",
          event.getSubject(), event.getStartTime(), event.getEndTime()));
    }
  }

  private void checkFordDuplicates(List<Event> events) throws DuplicateEventException {
    for (Event e : events) {
      checkIfEventExists(e);
    }
  }

  /**
   * Adds a single event to all internal storage structures (allEvents, eventMap, seriesMap).
   * Assumes duplicate check has already passed.
   *
   * @param event The event to add.
   * @throws IllegalStateException if a duplicate is found despite pre-checks.
   */
  private void addEventToStorage(Event event) {
    EventKey key = new EventKey(event);
    if (eventMap.containsKey(key)) {
      throw new IllegalStateException("Attempted to add duplicate event after check: " + key);
    }

    allEvents.add(event);
    eventMap.put(key, event);
    if (event.getSeriesId() != null) {
      seriesMap.computeIfAbsent(event.getSeriesId(), k -> new ArrayList<>()).add(event);
      seriesMap.get(event.getSeriesId()).sort(Comparator.comparing(Event::getStartTime));
    }
  }


  private boolean removeEventFromStorage(Event event) {
    if (event == null) {
      return false;
    }

    Event removedFromMap = eventMap.remove(new EventKey(event));
    boolean removedFromList = allEvents.remove(event);

    boolean removedFromSeries = false;
    if (event.getSeriesId() != null) {
      List<Event> series = seriesMap.get(event.getSeriesId());
      if (series != null) {
        removedFromSeries = series.remove(event);
        if (series.isEmpty()) {
          seriesMap.remove(event.getSeriesId());
        }
      }
    }

    boolean successfullyRemoved = (removedFromMap != null && removedFromList);

    if (removedFromMap == null && removedFromList) {
      System.err.println("Warning: Event removed from list but not found in map: " + event);
    }
    if (removedFromMap != null && !removedFromList) {
      System.err.println("Warning: Event removed from map but not found in list: " + event);
    }
    if (successfullyRemoved && event.getSeriesId() != null && !removedFromSeries) {
      System.err.println(
          "Warning: Event removed from main storage but not found in its seriesMap list: " + event);
    }

    return successfullyRemoved;
  }


  private List<Event> findEventsByAmbiguousKey(String subject, LocalDateTime startTime) {
    return allEvents.stream()
        .filter(e -> e.getSubject().equals(subject) && e.getStartTime().equals(startTime))
        .collect(Collectors.toList());
  }

  private List<Event> getEventsInSeries(String seriesId) {
    List<Event> series = seriesMap.get(seriesId);
    if (series == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(new ArrayList<>(series));
  }
}