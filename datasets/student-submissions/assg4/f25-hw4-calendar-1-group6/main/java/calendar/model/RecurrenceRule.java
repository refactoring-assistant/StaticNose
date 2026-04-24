package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines a weekly recurrence by selected weekdays,
 * either by a fixed number of occurrences OR until an inclusive end-date.
 */
public final class RecurrenceRule {
  private final EnumSet<DayOfWeek> weekdays;
  private final Optional<Integer> count;
  private final Optional<LocalDate> untilInclusive;

  private RecurrenceRule(EnumSet<DayOfWeek> weekdays,
                         Optional<Integer> count,
                         Optional<LocalDate> untilInclusive) {
    this.weekdays = Objects.requireNonNull(weekdays);
    this.count = Objects.requireNonNull(count);
    this.untilInclusive = Objects.requireNonNull(untilInclusive);

    if (this.count.isPresent() == this.untilInclusive.isPresent()) {
      throw new IllegalArgumentException("RecurrenceRule must have either count OR until date.");
    }
    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("Recurrence weekdays cannot be empty.");
    }
    if (this.count.isPresent() && this.count.get() <= 0) {
      throw new IllegalArgumentException("Recurrence count must be positive.");
    }
  }

  /**
   * Creates a recurrence rule that repeats on the specified weekdays,
   * for a given number of occurrences.
   *
   * @param weekdays the days of the week on which the event recurs
   * @param count the number of occurrences
   * @return a RecurrenceRule representing the specified count-based recurrence
   */
  public static RecurrenceRule forCount(EnumSet<DayOfWeek> weekdays, int count) {
    return new RecurrenceRule(weekdays, Optional.of(count), Optional.empty());
  }

  /**
   * Creates a recurrence rule that repeats on the specified weekdays until a given date.
   *
   * @param weekdays the days of the week on which the event recurs
   * @param untilInclusive the last date of recurrence (inclusive)
   * @return a RecurrenceRule representing the specified date-based recurrence
   */
  public static RecurrenceRule untilDate(EnumSet<DayOfWeek> weekdays, LocalDate untilInclusive) {
    return new RecurrenceRule(weekdays, Optional.empty(), Optional.of(untilInclusive));
  }

  /**
   * Returns the set of weekdays on which this recurrence occurs.
   *
   * @return an EnumSet of DayOfWeek values
   */
  public EnumSet<DayOfWeek> weekdays() {
    return EnumSet.copyOf(weekdays);
  }


  /**
   * Returns the number of occurrences for this recurrence, if defined.
   *
   * @return an Optional containing the count, or empty if not set
   */
  public Optional<Integer> count() {
    return count;
  }

  /**
   * Returns the end date of the recurrence, if defined.
   *
   * @return an Optional containing the end date (inclusive), or empty if not set
   */
  public Optional<LocalDate> untilInclusive() {
    return untilInclusive;
  }
}

