package calendar.repository;

import calendar.model.InEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of event repository.
 * Stores events in a list for the current session.
 * Data is lost when the application terminates.
 */
public class InMemoryEventRepository implements InEventRepository {

  private final List<InEvent> events;

  /**
   * Constructs an empty in-memory repository.
   */
  public InMemoryEventRepository() {
    this.events = new ArrayList<>();
  }

  @Override
  public void save(InEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (!events.contains(event)) {
      events.add(event);
    }
  }

  @Override
  public void delete(InEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    events.remove(event);
  }

  @Override
  public Optional<InEvent> findBySubjectAndDateTime(String subject,
                                                    LocalDateTime start,
                                                    LocalDateTime end) {
    if (subject == null || start == null || end == null) {
      return Optional.empty();
    }

    return events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(start)
            && e.getEndDateTime().equals(end))
        .findFirst();
  }

  @Override
  public List<InEvent> findAll() {
    return new ArrayList<>(events);
  }

  @Override
  public List<InEvent> findByDateRange(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      return Collections.emptyList();
    }

    return events.stream()
        .filter(e -> e.occursBetween(start, end))
        .collect(Collectors.toList());
  }

  @Override
  public boolean exists(String subject, LocalDateTime start, LocalDateTime end) {
    if (subject == null || start == null || end == null) {
      return false;
    }

    return events.stream()
        .anyMatch(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(start)
            && e.getEndDateTime().equals(end));
  }

  /**
   * Gets the total count of events in the repository.
   *
   * @return the number of events
   */
  public int getEventCount() {
    return events.size();
  }

  /**
   * Clears all events from the repository.
   * Used primarily for testing purposes.
   */
  public void clear() {
    events.clear();
  }

  /**
   * Finds events by subject (partial match).
   *
   * @param subjectKeyword the keyword to search in subjects
   * @return list of events matching the keyword
   */
  public List<InEvent> findBySubjectContaining(String subjectKeyword) {
    if (subjectKeyword == null || subjectKeyword.trim().isEmpty()) {
      return Collections.emptyList();
    }

    String keyword = subjectKeyword.toLowerCase();
    return events.stream()
        .filter(e -> e.getSubject().toLowerCase().contains(keyword))
        .collect(Collectors.toList());
  }

  /**
   * Finds events occurring at a specific date/time.
   *
   * @param dateTime the date/time to check
   * @return list of events occurring at that time
   */
  public List<InEvent> findEventsAt(LocalDateTime dateTime) {
    if (dateTime == null) {
      return Collections.emptyList();
    }

    return events.stream()
        .filter(e -> !e.getEndDateTime().isBefore(dateTime)
            && !e.getStartDateTime().isAfter(dateTime))
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "InMemoryEventRepository{eventCount=" + events.size() + "}";
  }
}