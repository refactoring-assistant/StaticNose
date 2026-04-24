package calendar.model;

import java.util.Objects;

/**
 * Represents an immutable time in the format hh:mm (24-hour format).
 * Provides comparison and utility methods for time operations.
 */
public class Time implements Comparable<Time> {
  private final int hour;
  private final int minute;
  public static final Time ALL_DAY_EVENT_START = new Time(8, 0);
  public static final Time ALL_DAY_EVENT_END = new Time(17, 0);

  /**
   * Constructs a Time with the given hour and minute.
   *
   * @param hour   the hour (0-23)
   * @param minute the minute (0-59)
   * @throws IllegalArgumentException if hour or minute are out of valid range
   */
  public Time(int hour, int minute) {
    if (hour < 0 || hour > 23) {
      throw new IllegalArgumentException("Hour must be between 0 and 23");
    }
    if (minute < 0 || minute > 59) {
      throw new IllegalArgumentException("Minute must be between 0 and 59");
    }
    this.hour = hour;
    this.minute = minute;
  }

  /**
   * Gets the hour.
   *
   * @return the hour (0-23)
   */
  public int getHour() {
    return hour;
  }

  /**
   * Gets the minute.
   *
   * @return the minute (0-59)
   */
  public int getMinute() {
    return minute;
  }

  @Override
  public int compareTo(Time other) {
    if (this.hour != other.hour) {
      return this.hour - other.hour;
    }
    return this.minute - other.minute;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Time time = (Time) o;
    return hour == time.hour && minute == time.minute;
  }

  @Override
  public int hashCode() {
    return Objects.hash(hour, minute);
  }

  @Override
  public String toString() {
    return String.format("%02d:%02d", hour, minute);
  }
}

