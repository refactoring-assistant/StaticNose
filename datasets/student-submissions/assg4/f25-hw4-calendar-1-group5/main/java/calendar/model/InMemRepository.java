package calendar.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A repository to store the calendar events in memory.
 * This classes uses an HashSet to store the events in memory.
 * Using a HashSet allows for automatic merging of two similar events (determined by the equals
 * method on the events).
 */
public class InMemRepository implements Repository {
  private final HashSet<Event> events;

  /**
   * Initialize an empty repository.
   */
  public InMemRepository() {
    events = new HashSet<>();
  }

  @Override
  public boolean exists(Event event) {
    return events.contains(event);
  }

  @Override
  public void insert(Event event) {
    events.add(event);
  }

  @Override
  public void insertAll(List<Event> newEvents) {
    events.addAll(newEvents);
  }

  @Override
  public void delete(Event event) {
    if (!events.contains(event)) {
      throw new IllegalArgumentException("Event not found");
    }

    events.remove(event);
  }

  @Override
  public List<Event> filter(Predicate<Event> predicate) {
    List<Event> result = new ArrayList<>();

    for (Event event : events) {
      if (predicate.test(event)) {
        result.add(event);
      }
    }

    return result;
  }

  @Override
  public Iterator<Event> iterator() {
    return events.iterator();
  }
}
