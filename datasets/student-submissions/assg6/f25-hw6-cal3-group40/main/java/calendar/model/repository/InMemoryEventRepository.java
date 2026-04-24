package calendar.model.repository;

import calendar.model.Event;
import calendar.model.exceptions.ConflictException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * An optimized in-memory implementation of the EventRepository.
 * This class uses:
 * 1. A TreeMap (eventsByStartTime) for fast time-based range queries
 * (findEventsBetween).
 * 2. A HashMap (eventsBySeriesId) for fast series lookups (editSeries).
 * 3. A HashSet (conflictSet) for O(1) conflict checking.
 */
public class InMemoryEventRepository implements EventRepository {

  private final Map<Instant, List<Event>> eventsByStartTime = new TreeMap<>();

  private final Map<String, List<Event>> eventsBySeriesId = new HashMap<>();

  private final Set<Event> conflictSet = new HashSet<>();

  @Override
  public void addEvent(Event event) throws ConflictException {
    if (conflictSet.contains(event)) {
      throw new ConflictException("Event conflict: An event with the same subject"
          + ", start, and end time already exists.");
    }

    conflictSet.add(event);

    eventsByStartTime.computeIfAbsent(event.getStart(), k -> new ArrayList<>()).add(event);

    if (event.isSeries()) {
      eventsBySeriesId.computeIfAbsent(event.getSeriesId(), k -> new ArrayList<>()).add(event);
    }
  }

  @Override
  public List<Event> findEventsBetween(Instant start, Instant end) {
    List<Event> allEvents = getAllEvents();
    return allEvents.stream()
        .filter(e -> e.getStart().isBefore(end) && e.getEnd().isAfter(start))
        .collect(Collectors.toList());
  }

  @Override
  public Event findUniqueEvent(String subject, Instant start, Instant end) {
    List<Event> potentialEvents = eventsByStartTime.getOrDefault(start, Collections.emptyList());

    for (Event e : potentialEvents) {
      if (e.getSubject().equals(subject)) {
        if (end == null || e.getEnd().equals(end)) {
          return e;
        }
      }
    }
    return null;
  }

  @Override
  public List<Event> getAllEvents() {
    return eventsByStartTime.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  @Override
  public void updateEvent(Event eventToUpdate, Event originalState) throws ConflictException {
    boolean wasSeries = originalState.isSeries();
    if (wasSeries) {
      List<Event> seriesList = eventsBySeriesId.get(originalState.getSeriesId());
      if (seriesList != null) {
        seriesList.remove(originalState);
      }
    }
    List<Event> eventsAtOldStart = eventsByStartTime.get(originalState.getStart());
    if (eventsAtOldStart != null) {
      eventsAtOldStart.remove(originalState);
    }
    conflictSet.remove(originalState);

    if (conflictSet.contains(eventToUpdate)) {
      addEvent(originalState);
      throw new ConflictException("Event conflict: Update failed.");
    }

    conflictSet.add(eventToUpdate);
    eventsByStartTime.computeIfAbsent(eventToUpdate.getStart(),
        k -> new ArrayList<>()).add(eventToUpdate);
    if (eventToUpdate.isSeries()) {
      eventsBySeriesId.computeIfAbsent(eventToUpdate.getSeriesId(),
          k -> new ArrayList<>()).add(eventToUpdate);
    }
  }
}