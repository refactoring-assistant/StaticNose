package calendar.model;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Identify an event by subject + start (+ optional end) for editing.
 */
public final class EventSelector {
  private final String subject;
  private final LocalDateTime start;
  private final Optional<LocalDateTime> end;

  /**
   * Creates a new event selector.
   *
   * @param subject the event subject (must not be blank)
   * @param start   the event start time (must not be null)
   * @param end     the event end time, or {@code null} if not specified
   */
  public EventSelector(String subject, LocalDateTime start, LocalDateTime end) {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("Subject required");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start required");
    }
    this.subject = subject;
    this.start = start.withSecond(0).withNano(0);
    this.end = Optional.ofNullable(end != null ? end.withSecond(0).withNano(0) : null);
  }

  /**
   * Gets the subject of the event.
   *
   * @return the event subject
   */
  public String subject() {
    return subject;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time
   */
  public LocalDateTime start() {
    return start;
  }

  /**
   * Gets the optional end time of the event.
   *
   * @return time containing the end time, if available
   */
  public Optional<LocalDateTime> end() {
    return end;
  }
}
