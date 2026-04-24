package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface for all calendar data operations.
 *
 * <p>This interface represents the core model in the MVC architecture.
 * It encapsulates all business logic for managing calendar events including
 * creating single/recurring events, editing them, querying by date/time,
 * and providing data for export operations.
 *
 * <p>Design rationale: By using an interface, we decouple the model from
 * implementation details, allowing for different storage mechanisms
 * (in-memory, database, file-based) without changing client code.
 * This follows the Dependency Inversion Principle - high-level modules
 * (controllers, views) depend on this abstraction, not concrete implementations.
 */
public interface CalendarModel {

  /**
   * Adds a single event.
   *
   * @param draft event object
   * @return created event
   */
  Event createSingleEvent(Event draft);

  /**
   * Creates a recurring series of events.
   *
   * @param template event template
   * @param rule recurrence rule
   * @return list of created events
   */
  List<Event> createEventSeries(Event template, RecurrenceRule rule);

  /**
   * Finds an event by subject and start time.
   *
   * @param subject subject
   * @param start start time
   * @return optional event
   */
  Optional<Event> findBySubjectAndStart(String subject, LocalDateTime start);

  /** Edits a single event. */
  void editSingle(String subject, LocalDateTime start, String property, String newValue);

  /** Edits all future events from the given date in a series. */
  void editFrom(String subject, LocalDateTime start, String property, String newValue);

  /** Edits all events in a series. */
  void editSeries(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Gets all events on a date.
   *
   * @param date date
   * @return list of events
   */
  List<Event> eventsOn(LocalDate date);

  /** Gets all events that overlap a date/time range. */
  List<Event> eventsOverlapping(LocalDateTime from, LocalDateTime to);

  /** Returns if user is busy or available at a time. */
  BusyStatus statusAt(LocalDateTime instant);

  /**
   * Gets all events in the calendar.
   *
   * <p>Used for exporting and bulk operations.
   *
   * <p>This method was added to support export functionality without
   * the model handling file I/O. Controllers and utilities can
   * retrieve all events to perform operations like CSV export.
   *
   * @return list of all events sorted by start time
   */
  List<Event> getAllEvents();
}
