package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the Calendar interface.
 * Manages a collection of events with duplicate checking and querying capabilities.
 */
public class MyCalendarImplement implements Calendar {
  private final List<Event> events;

  /**
   * Creates a new empty calendar.
   */
  public MyCalendarImplement() {
    this.events = new ArrayList<>();
  }

  @Override
  public void addEvent(Event event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (isDuplicate(event)) {
      String message = "Duplicate event: An event with the same subject, "
          + "start time, and end time already exists";
      throw new IllegalArgumentException(message);
    }

    events.add(event);
  }

  @Override
  public boolean removeEvent(Event event) {
    return events.remove(event);
  }

  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    return events.stream()
        .filter(event -> {
          LocalDate eventStart = event.getStart().toLocalDate();
          LocalDate eventEnd = event.getEnd().toLocalDate();
          return !date.isBefore(eventStart)
              && !date.isAfter(eventEnd);
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return events.stream()
        .filter(event -> {
          // Event overlaps range if:
          // event starts before range end AND event ends after range start
          return event.getStart().isBefore(end)
              && event.getEnd().isAfter(start);
        })
        .collect(Collectors.toList());
  }

  @Override
  public Event findEvent(String subject, LocalDateTime start, LocalDateTime end) {
    return events.stream()
        .filter(event -> event.getSubject().equals(subject)
            && event.getStart().equals(start)
            && event.getEnd().equals(end))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Event> findEvents(String subject, LocalDateTime start) {
    return events.stream()
        .filter(event -> event.getSubject().equals(subject)
            && event.getStart().equals(start))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return events.stream()
        .anyMatch(event ->
            !dateTime.isBefore(event.getStart())
                && !dateTime.isAfter(event.getEnd()));
  }

  @Override
  public void updateEvent(Event event) {
    for (int i = 0; i < events.size(); i++) {
      Event existing = events.get(i);
      if (existing.equals(event)) {
        events.set(i, event);
        return;
      }
    }
  }

  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public boolean isDuplicate(Event event) {
    return events.stream()
        .anyMatch(e -> e.getSubject().equals(event.getSubject())
            && e.getStart().equals(event.getStart())
            && e.getEnd().equals(event.getEnd()));
  }

  @Override
  public List<Event> getEventsBySeries(String seriesId) {
    if (seriesId == null) {
      return new ArrayList<>();
    }

    return events.stream()
        .filter(event -> seriesId.equals(event.getSeriesId()))
        .collect(Collectors.toList());
  }
}