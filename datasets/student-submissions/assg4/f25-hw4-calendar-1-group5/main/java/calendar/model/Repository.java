package calendar.model;

import java.util.List;
import java.util.function.Predicate;

/**
 * This interface represents a repository used by the calendar application's model.
 */
public interface Repository extends Iterable<Event> {
  /**
   * A method to check if an event exists using the object's equals method.
   *
   * @param event The event to be checked.
   * @return True if exists, false otherwise.
   */
  boolean exists(Event event);

  /**
   * A method to store a single event.
   *
   * @param event The event to be stored.
   */
  void insert(Event event);

  /**
   * A method to store a list of events.
   *
   * @param events The List of events to be saved.
   */
  void insertAll(List<Event> events);

  /**
   * A method to delete an event.
   *
   * @param event the event to be deleted.
   */
  void delete(Event event);

  /**
   * A method to find all events that satisfy a given criteria.
   *
   * @param predicate The criteria wrapped in a Predicate function.
   * @return A list of events satisfying the given criteria.
   */
  List<Event> filter(Predicate<Event> predicate);
}
