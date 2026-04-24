package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a calendar event with required and optional properties.
 *
 * <p>Events can be single occurrences or part of a recurring series.
 *
 * <p>Design rationale: By using an interface, we achieve:
 * 1. Abstraction - Clients depend on behavior, not implementation
 * 2. Encapsulation - Internal representation can change without breaking clients
 * 3. Testability - Easy to create mock events for testing
 * 4. Flexibility - Multiple implementations possible (e.g., persistent vs in-memory)
 * 5. SOLID principles - Interface segregation and dependency inversion
 */
public interface Ievent {

  /** Returns the subject/title of the event. */
  String getSubject();

  /** Returns the start date and time. */
  LocalDateTime getStart();

  /** Returns the end date and time. */
  LocalDateTime getEnd();

  /** Returns the optional description. */
  Optional<String> getDescription();

  /** Returns the optional location. */
  Optional<String> getLocation();

  /** Returns the optional status (public/private). */
  Optional<String> getStatus();

  /** Returns the optional series ID for recurring events. */
  Optional<UUID> getSeriesId();

  /**
   * Creates a copy with a new subject.
   *
   * <p>Events are immutable, so modifications return new instances.
   *
   * @param newSubject the new subject
   * @return new event with updated subject
   */
  Ievent withSubject(String newSubject);

  /**
   * Creates a copy with a new start time, preserving duration.
   *
   * @param newStart the new start date/time
   * @return new event with updated start (and adjusted end to maintain duration)
   */
  Ievent withStart(LocalDateTime newStart);

  /**
   * Creates a copy with a new end time.
   *
   * @param newEnd the new end date/time
   * @return new event with updated end
   */
  Ievent withEnd(LocalDateTime newEnd);

  /**
   * Creates a copy with an updated description.
   *
   * @param description the new description (null to clear)
   * @return new event with updated description
   */
  Ievent withDescription(String description);

  /**
   * Creates a copy with an updated location.
   *
   * @param location the new location (null to clear)
   * @return new event with updated location
   */
  Ievent withLocation(String location);

  /**
   * Creates a copy with an updated status.
   *
   * @param status the new status (null to clear)
   * @return new event with updated status
   */
  Ievent withStatus(String status);

  /**
   * Returns true if event overlaps the given time range.
   *
   * <p>An event overlaps if any part of it falls within [from, to].
   *
   * @param from start of range (inclusive)
   * @param to end of range (inclusive)
   * @return true if there is any overlap
   */
  boolean overlaps(LocalDateTime from, LocalDateTime to);

  /**
   * Returns true if event occurs on the given date.
   *
   * <p>An event occurs on a date if any part of it falls on that date.
   *
   * @param date the date to check
   * @return true if event occurs on this date
   */
  boolean isOn(LocalDate date);
}
