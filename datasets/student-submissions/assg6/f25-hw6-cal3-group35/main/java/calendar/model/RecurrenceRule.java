package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Represents repetition rules for a series of events.
 *
 * <p>Design Change (Assignment 5): All fields are now private to follow
 * proper encapsulation. Access is provided through getter methods only.
 * This prevents external modification and maintains immutability.
 *
 * @author MH
 * @version 2.0
 */
public class RecurrenceRule {

  private final EnumSet<DayOfWeek> days;
  private final Optional<Integer> count;
  private final Optional<LocalDate> until;

  /**
   * Constructs a recurrence rule.
   *
   * @param days set of days when event recurs (must not be empty)
   * @param count optional number of occurrences
   * @param until optional end date
   * @throws IllegalArgumentException if days is null/empty or both count and until are present
   */
  public RecurrenceRule(EnumSet<DayOfWeek> days,
                        Optional<Integer> count,
                        Optional<LocalDate> until) {
    if (days == null || days.isEmpty()) {
      throw new IllegalArgumentException("weekdays required");
    }
    if (count != null && until != null
        && count.isPresent() && until.isPresent()) {
      throw new IllegalArgumentException("use count or until, not both");
    }
    this.days = days;
    this.count = count == null ? Optional.empty() : count;
    this.until = until == null ? Optional.empty() : until;
  }

  public EnumSet<DayOfWeek> getDays() {
    return days;
  }

  public Optional<Integer> getCount() {
    return count;
  }

  public Optional<LocalDate> getUntil() {
    return until;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("RecurrenceRule{days=");
    sb.append(days);
    if (count.isPresent()) {
      sb.append(", count=").append(count.get());
    }
    if (until.isPresent()) {
      sb.append(", until=").append(until.get());
    }
    sb.append("}");
    return sb.toString();
  }
}