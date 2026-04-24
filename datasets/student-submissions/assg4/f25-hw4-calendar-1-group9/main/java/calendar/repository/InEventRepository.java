package calendar.repository;

import calendar.model.InEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pattern for event persistence.
 * Abstracts data storage mechanism from business logic.
 */
public interface InEventRepository {

  /**
   * Saves an event to the repository.
   *
   * @param event the event to save
   */
  void save(InEvent event);

  /**
   * Deletes an event from the repository.
   *
   * @param event the event to delete
   */
  void delete(InEvent event);

  /**
   * Finds an event by subject and date/time range.
   *
   * @param subject the event subject
   * @param start   the start date/time
   * @param end     the end date/time
   * @return Optional containing the event if found, empty otherwise
   */
  Optional<InEvent> findBySubjectAndDateTime(String subject, LocalDateTime start,
                                             LocalDateTime end);

  /**
   * Gets all events in the repository.
   *
   * @return list of all events
   */
  List<InEvent> findAll();

  /**
   * Finds events within a date/time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return list of events in the range
   */
  List<InEvent> findByDateRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if an event with specific properties exists.
   *
   * @param subject the event subject
   * @param start   the start date/time
   * @param end     the end date/time
   * @return true if exists, false otherwise
   */
  boolean exists(String subject, LocalDateTime start, LocalDateTime end);
}