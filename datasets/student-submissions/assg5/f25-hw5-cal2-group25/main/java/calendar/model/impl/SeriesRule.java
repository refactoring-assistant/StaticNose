package calendar.model.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Defines recurrence rules for creating a series of calendar events.
 */
public final class SeriesRule {
  public final Set<DayOfWeek> days;
  public final Integer occurrences;
  public final LocalDate until;
  public final LocalTime startTime;

  /**
   * Creates a new recurrence rule.
   *
   * @param days        the set of weekdays on which the series repeats.
   * @param occurrences the number of times the event should occur, or null if using until.
   * @param until       the inclusive end date for the series, or null if using occurrences.
   * @param startTime   the start time of each occurrence.
   */
  public SeriesRule(Set<DayOfWeek> days, Integer occurrences, LocalDate until,
                    LocalTime startTime) {
    this.days = days;
    this.occurrences = occurrences;
    this.until = until;
    this.startTime = startTime;
  }
}