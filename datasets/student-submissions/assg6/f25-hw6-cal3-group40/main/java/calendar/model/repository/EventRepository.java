package calendar.model.repository;

import calendar.model.Event;
import calendar.model.exceptions.ConflictException;
import java.time.Instant;
import java.util.List;

/**
 * Interface defining all CRUD (Create, Read, Update, Delete)
 * operations for Event data.
 */
public interface EventRepository {

  /**
   * Adds a new event to the data store.
   *
   * @param event The event to add.
   * @throws ConflictException if the event conflicts with an existing one.
   */
  void addEvent(Event event) throws ConflictException;

  /**
   * Finds all events within a given date/time range.
   *
   * @param start The start of the range (inclusive).
   * @param end   The end of the range (exclusive).
   * @return A List of matching events.
   */
  List<Event> findEventsBetween(Instant start, Instant end);

  /**
   * Finds a single, unique event by its subject, start, and end time.
   *
   * @param subject The event's subject.
   * @param start   The event's start time (UTC).
   * @param end     The event's end time (UTC). Can be null for 'edit series' find.
   * @return The matching Event, or null if not found.
   */
  Event findUniqueEvent(String subject, Instant start, Instant end);

  /**
   * Gets all events in the repository.
   *
   * @return A list of all events.
   */
  List<Event> getAllEvents();

  /**
   * Updates an event.
   * This method primarily checks for conflicts upon update.
   * The object is assumed to be modified by reference.
   *
   * @param eventToUpdate The event to be updated.
   * @param originalState Current state of the event to be updated.
   * @throws ConflictException In case of a conflict.
   */
  void updateEvent(Event eventToUpdate, Event originalState) throws ConflictException;
}