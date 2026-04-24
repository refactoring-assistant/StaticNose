package calendar.model;

import java.util.Objects;

/**
 * Represents an immutable date in the format YYYY-MM-DD.
 * Provides comparison and utility methods for date operations.
 */
public class Date implements Comparable<Date> {
  private final int year;
  private final int month;
  private final int day;

  /**
   * Constructs a Date with the given year, month, and day.
   *
   * @param year  the year
   * @param month the month (1-12)
   * @param day   the day (1-31)
   * @throws IllegalArgumentException if month or day are out of valid range
   */
  public Date(int year, int month, int day) {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }
    if (day < 1 || day > 31) {
      throw new IllegalArgumentException("Day must be between 1 and 31");
    }
    this.year = year;
    this.month = month;
    this.day = day;
  }

  /**
   * Gets the year.
   *
   * @return the year
   */
  public int getYear() {
    return year;
  }

  /**
   * Gets the month.
   *
   * @return the month (1-12)
   */
  public int getMonth() {
    return month;
  }

  /**
   * Gets the day.
   *
   * @return the day (1-31)
   */
  public int getDay() {
    return day;
  }

  @Override
  public int compareTo(Date other) {
    if (this.year != other.year) {
      return this.year - other.year;
    }
    if (this.month != other.month) {
      return this.month - other.month;
    }
    return this.day - other.day;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Date date = (Date) o;
    return year == date.year && month == date.month && day == date.day;
  }

  @Override
  public int hashCode() {
    return Objects.hash(year, month, day);
  }

  @Override
  public String toString() {
    return String.format("%04d-%02d-%02d", year, month, day);
  }
}

