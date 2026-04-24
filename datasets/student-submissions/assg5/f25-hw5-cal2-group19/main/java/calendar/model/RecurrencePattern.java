package calendar.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Defines the recurrence pattern for event series.
 * Specifies which weekdays events repeat on and the termination condition.
 * Can terminate by occurrence count or by end date.
 */
public class RecurrencePattern {

  private final Set<Weekday> weekdays;
  private final Integer occurrences;
  private final LocalDate endDate;
  private final LocalTime startTime;
  private final LocalTime endTime;

  /**
   * Constructs a RecurrencePattern with occurrence count.
   *
   * @param weekdays    set of weekdays to repeat on (e.g., Monday, Wednesday, Friday)
   * @param startTime   the start time for all instances
   * @param endTime     the end time for all instances (null for all-day)
   * @param occurrences number of times to repeat (must be positive)
   */
  public RecurrencePattern(Set<Weekday> weekdays, LocalTime startTime,
                           LocalTime endTime, int occurrences) {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Occurrences must be positive");
    }
    if (startTime == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }

    this.weekdays = weekdays;
    this.startTime = startTime;
    this.endTime = endTime;
    this.occurrences = occurrences;
    this.endDate = null;
  }

  /**
   * Constructs a RecurrencePattern with end date.
   *
   * @param weekdays  set of weekdays to repeat on
   * @param startTime the start time for all instances
   * @param endTime   the end time for all instances (null for all-day)
   * @param endDate   the last date to repeat until (inclusive)
   */
  public RecurrencePattern(Set<Weekday> weekdays, LocalTime startTime,
                           LocalTime endTime, LocalDate endDate) {
    if (weekdays == null || weekdays.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be null or empty");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date cannot be null");
    }
    if (startTime == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }

    this.weekdays = weekdays;
    this.startTime = startTime;
    this.endTime = endTime;
    this.occurrences = null;
    this.endDate = endDate;
  }

  /**
   * Generates all occurrence dates for this pattern starting from a given date.
   * Iterates through days and includes dates that match the weekday pattern.
   *
   * @param startDate the first date to start generating from
   * @return list of dates matching the pattern
   */
  public List<LocalDate> generateOccurrences(LocalDate startDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }

    List<LocalDate> dates = new ArrayList<>();
    LocalDate current = startDate;
    int count = 0;

    if (occurrences != null) {
      while (count < occurrences) {
        Weekday currentWeekday = Weekday.fromDayOfWeek(current.getDayOfWeek());
        if (weekdays.contains(currentWeekday)) {
          dates.add(current);
          count++;
        }
        current = current.plusDays(1);

        if (dates.size() > 1000) {
          throw new IllegalStateException("Too many occurrences generated (limit: 1000)");
        }
      }
    } else {
      while (!current.isAfter(endDate)) {
        Weekday currentWeekday = Weekday.fromDayOfWeek(current.getDayOfWeek());
        if (weekdays.contains(currentWeekday)) {
          dates.add(current);
        }
        current = current.plusDays(1);

        if (dates.size() > 1000) {
          throw new IllegalStateException("Too many occurrences generated (limit: 1000)");
        }
      }
    }

    return dates;
  }

  /**
   * Gets the weekdays this pattern repeats on.
   *
   * @return unmodifiable set of weekdays
   */
  public Set<Weekday> getWeekdays() {
    return Collections.unmodifiableSet(weekdays);
  }

  /**
   * Gets the start time for all instances.
   *
   * @return the start time
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * Gets the end time for all instances.
   *
   * @return the end time, or null for all-day events
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * Checks if this pattern terminates by occurrence count.
   *
   * @return true if using occurrence count, false if using end date
   */
  public boolean hasOccurrenceCount() {
    return occurrences != null;
  }

  /**
   * Gets the occurrence count if applicable.
   *
   * @return the occurrence count, or null if using end date
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * Gets the end date if applicable.
   *
   * @return the end date, or null if using occurrence count
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Checks if this is an all-day pattern.
   *
   * @return true if end time is null, false otherwise
   */
  public boolean isAllDay() {
    return endTime == null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("RecurrencePattern{weekdays=");
    sb.append(weekdays);
    sb.append(", startTime=").append(startTime);
    sb.append(", endTime=").append(endTime);
    if (occurrences != null) {
      sb.append(", occurrences=").append(occurrences);
    } else {
      sb.append(", endDate=").append(endDate);
    }
    sb.append("}");
    return sb.toString();
  }
}