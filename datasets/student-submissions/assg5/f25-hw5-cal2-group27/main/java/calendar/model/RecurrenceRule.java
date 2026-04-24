package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Represents the recurrence rule for a series of events.
 */
public class RecurrenceRule {

  private final Set<DayOfWeek> weekdays;
  private final Integer count;
  private final LocalDate untilDate;

  /**
   * Creates a recurrence rule that repeats for a specific number of times.
   *
   * @param weekdays The days of the week the event should repeat on.
   * @param count    The number of occurrences.
   */
  public RecurrenceRule(Set<DayOfWeek> weekdays, int count) {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty.");
    }
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be positive.");
    }
    this.weekdays = weekdays;
    this.count = count;
    this.untilDate = null;
  }

  /**
   * Creates a recurrence rule that repeats until a specific date.
   *
   * @param weekdays  The days of the week the event should repeat on.
   * @param untilDate The end date for the recurrence (inclusive).
   */
  public RecurrenceRule(Set<DayOfWeek> weekdays, LocalDate untilDate) {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty.");
    }
    if (untilDate == null) {
      throw new IllegalArgumentException("Until date cannot be null.");
    }
    this.weekdays = weekdays;
    this.untilDate = untilDate;
    this.count = null;
  }

  public Integer getCount() {
    return count;
  }

  /**
   * Computes the next occurrence start after the given event start.
   *
   * @param lastStart        The start time of the most recently scheduled event.
   * @param occurrencesSoFar The number of occurrences that have already been scheduled
   *                         (including the template event).
   * @return The next start time, or empty if the series should end.
   */
  public Optional<ZonedDateTime> nextOccurrence(ZonedDateTime lastStart, int occurrencesSoFar) {
    if (count != null && occurrencesSoFar >= count) {
      return Optional.empty();
    }
    if (untilDate != null && lastStart.toLocalDate().isAfter(untilDate)) {
      return Optional.empty();
    }

    ZonedDateTime candidate = lastStart;
    for (int i = 0; i < 7; i++) {
      candidate = candidate.plusDays(1);
      if (weekdays.contains(candidate.getDayOfWeek())) {
        if (untilDate != null && candidate.toLocalDate().isAfter(untilDate)) {
          return Optional.empty();
        }
        return Optional.of(candidate);
      }
    }
    return Optional.empty();
  }
}