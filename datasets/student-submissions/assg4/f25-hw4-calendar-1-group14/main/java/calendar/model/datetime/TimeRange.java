package calendar.model.datetime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Immutable record representing a time range with start and end times.
 * Ensures that start time is before or equal to end time.
 */
public final class TimeRange {
  private final DateTime start;
  private final DateTime end;

  /**
   * Validates that start is before or equal to end.
   */
  public TimeRange(DateTime start, DateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end times cannot be null");
    }
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start time must be before or equal to end time");
    }
    this.start = start;
    this.end = end;
  }

  /**
   * Gets the start date-time.
   *
   * @return the start date-time.
   */
  public DateTime start() {
    return start;
  }

  /**
   * Gets the end date-time.
   *
   * @return the end date-time.
   */
  public DateTime end() {
    return end;
  }

  /**
   * Creates a TimeRange from LocalDateTime objects.
   *
   * @param start the start date-time
   * @param end   the end date-time
   * @return a new TimeRange
   */
  public static TimeRange of(LocalDateTime start, LocalDateTime end) {
    return new TimeRange(new DateTime(start), new DateTime(end));
  }

  /**
   * Creates a TimeRange from date and time components.
   *
   * @param date      the date for both start and end
   * @param startTime the start time
   * @param endTime   the end time
   * @return a new TimeRange
   */
  public static TimeRange of(LocalDate date, LocalTime startTime, LocalTime endTime) {
    return new TimeRange(
        DateTime.of(date, startTime),
        DateTime.of(date, endTime)
    );
  }

  /**
   * Creates an all-day TimeRange (8:00 AM to 5:00 PM).
   *
   * @param date the date
   * @return a new all-day TimeRange
   */
  public static TimeRange allDay(LocalDate date) {
    return new TimeRange(
        DateTime.allDayStart(date),
        DateTime.allDayEnd(date)
    );
  }

  /**
   * Creates a TimeRange that spans multiple days.
   *
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate   the end date
   * @param endTime   the end time
   * @return a new TimeRange
   */
  public static TimeRange multiDay(LocalDate startDate, LocalTime startTime,
                                   LocalDate endDate, LocalTime endTime) {
    return new TimeRange(
        DateTime.of(startDate, startTime),
        DateTime.of(endDate, endTime)
    );
  }

  /**
   * Checks if this time range contains a specific date-time.
   *
   * @param dateTime the date-time to check
   * @return true if the date-time is within this range (inclusive)
   */
  public boolean contains(DateTime dateTime) {
    return !dateTime.isBefore(start) && !dateTime.isAfter(end);
  }

  /**
   * Checks if this time range contains a specific LocalDateTime.
   *
   * @param dateTime the date-time to check
   * @return true if the date-time is within this range (inclusive)
   */
  public boolean contains(LocalDateTime dateTime) {
    return contains(new DateTime(dateTime));
  }

  /**
   * Checks if this time range overlaps with another.
   *
   * @param other the other time range
   * @return true if there's any overlap
   */
  public boolean overlaps(TimeRange other) {
    return start.isBefore(other.end) && end.isAfter(other.start);
  }

  /**
   * Checks if this time range is entirely within a single day.
   *
   * @return true if start and end are on the same date
   */
  public boolean isSingleDay() {
    return start.getDate().equals(end.getDate());
  }

  /**
   * Checks if this time range spans multiple days.
   *
   * @return true if start and end are on different dates
   */
  public boolean isMultiDay() {
    return !isSingleDay();
  }

  /**
   * Gets the duration of this time range.
   *
   * @return the duration between start and end
   */
  public Duration getDuration() {
    return Duration.between(start.toLocalDateTime(), end.toLocalDateTime());
  }

  /**
   * Gets the duration in hours.
   *
   * @return the number of hours in this range
   */
  public long getHours() {
    return getDuration().toHours();
  }

  /**
   * Gets the duration in minutes.
   *
   * @return the number of minutes in this range
   */
  public long getMinutes() {
    return getDuration().toMinutes();
  }

  /**
   * Checks if this is an all-day event (8:00 AM to 5:00 PM).
   *
   * @return true if this matches all-day event times
   */
  public boolean isAllDay() {
    return start.getTime().equals(LocalTime.of(8, 0))
        && end.getTime().equals(LocalTime.of(17, 0))
        && isSingleDay();
  }

  /**
   * Shifts this time range by a number of days.
   *
   * @param days number of days to shift (can be negative)
   * @return a new shifted TimeRange
   */
  public TimeRange shiftDays(long days) {
    return new TimeRange(start.plusDays(days), end.plusDays(days));
  }

  /**
   * Shifts this time range by a number of weeks.
   *
   * @param weeks number of weeks to shift (can be negative)
   * @return a new shifted TimeRange
   */
  public TimeRange shiftWeeks(long weeks) {
    return new TimeRange(start.plusWeeks(weeks), end.plusWeeks(weeks));
  }

  /**
   * Creates a new TimeRange with a different start time.
   *
   * @param newStart the new start time
   * @return a new TimeRange with the updated start
   */
  public TimeRange withStart(DateTime newStart) {
    return new TimeRange(newStart, end);
  }

  /**
   * Creates a new TimeRange with a different end time.
   *
   * @param newEnd the new end time
   * @return a new TimeRange with the updated end
   */
  public TimeRange withEnd(DateTime newEnd) {
    return new TimeRange(start, newEnd);
  }

  /**
   * Formats this time range as a string.
   *
   * @return formatted string representation
   */
  public String format() {
    if (isSingleDay()) {
      return String.format("%s from %s to %s",
          start.formatDate(),
          start.formatTime(),
          end.formatTime());
    } else {
      return String.format("from %s to %s",
          start.formatDateTime(),
          end.formatDateTime());
    }
  }

  /**
   * Returns a string representation of this object.
   *
   * @return the formatted string
   */
  @Override
  public String toString() {
    return format();
  }

  /**
   * Checks equality with another object.
   *
   * @param obj the object to compare
   * @return true if all fields are equal.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TimeRange timeRange = (TimeRange) obj;
    return Objects.equals(start, timeRange.start)
        && Objects.equals(end, timeRange.end);
  }

  /**
   * Generates a hash code for the time range.
   *
   * @return hash based on all fields.
   */
  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }
}