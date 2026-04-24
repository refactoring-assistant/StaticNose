package model;

import static java.util.Collections.unmodifiableNavigableSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

/**
 * A class that represents an Event Series, which is composed of multiple Events. These events
 * are sorted by the start date and time, end date and time, and subject in that order.
 */
public class EventSeries {
  /**
   * Uses a TreeSet to maintain the structure and proper sorting (by start date and time,
   * end date and time, and then subject) required in Event Series.
   */
  private final NavigableSet<Event> byStart = new TreeSet<>(
      Comparator.comparing(Event::getStartTime)
          .thenComparing(Event::getEndTime)
          .thenComparing(Event::getSubject)
  );

  /**
   * Creates an empty EventSeries object.
   */
  public EventSeries() {
  }

  //  /**
  //   * Creates an EventSeries object and initializes it with a given collection of Events.
  //   *
  //   * @param initial a collection of events
  //   */
  //  public EventSeries(Collection<Event> initial) {
  //    addAll(initial);
  //  }

  /**
   * Creates a placeholder event that positions at the desired start instant.
   *
   * @param start a start date and time
   */
  private static Event sentinel(LocalDateTime start) {
    return new Event(start, start, "");
  }

  /**
   * Adds an Event to an Event Series.
   *
   * @param e an Event
   * @throws NullPointerException     if the event is null
   * @throws IllegalArgumentException if the added event is a duplicate
   */
  public void addEvent(Event e) {
    Objects.requireNonNull(e, "event");
    if (!byStart.add(e)) {
      throw new IllegalArgumentException("Duplicate event in series: " + e);
    }
    e.setEventSeries(this);
  }

  /**
   * Removes a specific event from this series, clearing its back-reference.
   *
   * @param event event to remove
   * @return true if the event was removed
   */
  public boolean removeEvent(Event event) {
    Objects.requireNonNull(event, "event");
    boolean removed = byStart.remove(event);
    if (removed) {
      event.setEventSeries(null);
    }
    return removed;
  }

  /**
   * Returns true when no events remain in the series.
   *
   * @return whether the series is empty
   */
  public boolean isEmpty() {
    return byStart.isEmpty();
  }

  //  /**
  //   * Add a collection of events to an Event Series.
  //   *
  //   * @param events a collection of Events
  //   * @throws NullPointerException     if the event is null
  //   * @throws IllegalArgumentException if the added event is a duplicate
  //   */
  //  public void addAll(Collection<Event> events) {
  //    for (Event e : events) {
  //      addEvent(e);
  //    }
  //  }

  /**
   * Returns an unmodifiable, sorted view of all events.
   *
   * @return a set of events sorted by start date and time
   */
  public NavigableSet<Event> events() {
    return unmodifiableNavigableSet(byStart);
  }

  /**
   * Returns the first event that starts at or after a given date and time,
   * or null if it doesn't exist.
   *
   * @param t a given date and time
   * @return the first event at or after t, null if such an event does not exist
   */
  public Event firstAtOrAfter(LocalDateTime t) {
    return byStart.ceiling(sentinel(t));
  }

  /**
   * Returns all events at or after a certain time as a new Event Series.
   *
   * @param t a given date and time
   * @return an Event Series for at or after t, empty if such events do not exist
   */
  public EventSeries splitFrom(LocalDateTime t) {
    NavigableSet<Event> tail = byStart.tailSet(sentinel(t), true);
    if (tail.isEmpty()) {
      return new EventSeries();
    }

    // copy because we’ll mutate
    List<Event> toMove = new ArrayList<>(tail);
    EventSeries newSeries = new EventSeries();
    for (Event e : toMove) {
      byStart.remove(e);
      newSeries.addEvent(e); // sets back-pointer to new series
    }
    return newSeries;
  }

}
