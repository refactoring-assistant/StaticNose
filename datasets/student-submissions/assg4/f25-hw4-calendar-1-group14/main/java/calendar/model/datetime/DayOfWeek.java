package calendar.model.datetime;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum representing days of the week with single-character codes.
 * Uses the convention from the assignment where R = Thursday and U = Sunday.
 */
public enum DayOfWeek {
  MONDAY('M', java.time.DayOfWeek.MONDAY),
  TUESDAY('T', java.time.DayOfWeek.TUESDAY),
  WEDNESDAY('W', java.time.DayOfWeek.WEDNESDAY),
  THURSDAY('R', java.time.DayOfWeek.THURSDAY),  // R for thuRsday
  FRIDAY('F', java.time.DayOfWeek.FRIDAY),
  SATURDAY('S', java.time.DayOfWeek.SATURDAY),
  SUNDAY('U', java.time.DayOfWeek.SUNDAY);       // U for sUnday

  private final char code;
  private final java.time.DayOfWeek javaDayOfWeek;

  /**
   * Constructs a DayOfWeek with its character code and Java equivalent.
   *
   * @param code          the single character code
   * @param javaDayOfWeek the corresponding Java DayOfWeek
   */
  DayOfWeek(char code, java.time.DayOfWeek javaDayOfWeek) {
    this.code = code;
    this.javaDayOfWeek = javaDayOfWeek;
  }

  /**
   * Gets the single character code for this day.
   *
   * @return the character code
   */
  public char getCode() {
    return code;
  }

  /**
   * Gets the Java DayOfWeek equivalent.
   *
   * @return the Java DayOfWeek
   */
  public java.time.DayOfWeek getJavaDayOfWeek() {
    return javaDayOfWeek;
  }

  /**
   * Parses a DayOfWeek from its character code.
   *
   * @param code the character code
   * @return the corresponding DayOfWeek
   * @throws IllegalArgumentException if code is invalid
   */
  public static DayOfWeek fromCode(char code) {
    for (DayOfWeek day : values()) {
      if (day.code == Character.toUpperCase(code)) {
        return day;
      }
    }
    throw new IllegalArgumentException("Invalid day code: " + code);
  }

  /**
   * Parses a set of DayOfWeek from a string of codes.
   * For example, "MWF" returns {MONDAY, WEDNESDAY, FRIDAY}
   *
   * @param codes string containing day codes
   * @return set of corresponding DayOfWeek values
   * @throws IllegalArgumentException if any code is invalid
   */
  public static Set<DayOfWeek> parseWeekdays(String codes) {
    if (codes == null || codes.isEmpty()) {
      return EnumSet.noneOf(DayOfWeek.class);
    }

    Set<DayOfWeek> weekdays = EnumSet.noneOf(DayOfWeek.class);
    for (char c : codes.toCharArray()) {
      if (!Character.isWhitespace(c)) {
        weekdays.add(fromCode(c));
      }
    }
    return weekdays;
  }

  /**
   * Converts a set of DayOfWeek to a string of codes.
   * For example, {MONDAY, WEDNESDAY, FRIDAY} returns "MWF"
   *
   * @param weekdays set of DayOfWeek values
   * @return string of character codes
   */
  public static String toCodes(Set<DayOfWeek> weekdays) {
    if (weekdays == null || weekdays.isEmpty()) {
      return "";
    }

    return weekdays.stream()
        .sorted()
        .map(day -> String.valueOf(day.code))
        .collect(Collectors.joining());
  }

  /**
   * Converts from Java's DayOfWeek.
   *
   * @param javaDayOfWeek the Java DayOfWeek
   * @return the corresponding DayOfWeek from this enum
   */
  public static DayOfWeek fromJavaDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
    if (javaDayOfWeek == null) {
      throw new IllegalArgumentException("Invalid Java DayOfWeek: null");
    }

    switch (javaDayOfWeek) {
      case MONDAY:
        return MONDAY;
      case TUESDAY:
        return TUESDAY;
      case WEDNESDAY:
        return WEDNESDAY;
      case THURSDAY:
        return THURSDAY;
      case FRIDAY:
        return FRIDAY;
      case SATURDAY:
        return SATURDAY;
      case SUNDAY:
        return SUNDAY;
      default:
        throw new IllegalArgumentException("Invalid Java DayOfWeek: " + javaDayOfWeek);
    }
  }

  /**
   * Gets the DayOfWeek for a specific date.
   *
   * @param date the date
   * @return the DayOfWeek for that date
   */
  public static DayOfWeek fromDate(LocalDate date) {
    return fromJavaDayOfWeek(date.getDayOfWeek());
  }

  /**
   * Checks if a date falls on this day of the week.
   *
   * @param date the date to check
   * @return true if the date is on this day of the week
   */
  public boolean matches(LocalDate date) {
    return date.getDayOfWeek() == javaDayOfWeek;
  }

  /**
   * Gets the next occurrence of this day of week from a given date.
   * If the given date is already this day, returns the date one week later.
   *
   * @param from the starting date
   * @return the next occurrence of this day
   */
  public LocalDate nextFrom(LocalDate from) {
    int daysToAdd = (javaDayOfWeek.getValue() - from.getDayOfWeek().getValue() + 7) % 7;
    if (daysToAdd == 0) {
      daysToAdd = 7; // If it's the same day, get next week's occurrence
    }
    return from.plusDays(daysToAdd);
  }

  /**
   * Gets the next occurrence of this day of week from a given date, inclusive.
   * If the given date is already this day, returns that date.
   *
   * @param from the starting date
   * @return the next occurrence of this day (including the start date)
   */
  public LocalDate nextFromInclusive(LocalDate from) {
    if (matches(from)) {
      return from;
    }
    return nextFrom(from);
  }

  /**
   * Gets a human-readable name for this day.
   *
   * @return the full name of the day
   */
  public String getDisplayName() {
    return javaDayOfWeek.toString().substring(0, 1)
        + javaDayOfWeek.toString().substring(1).toLowerCase();
  }

  /**
   * Gets the ISO-8601 value (1 = Monday, 7 = Sunday).
   *
   * @return the ISO day number
   */
  public int getValue() {
    return javaDayOfWeek.getValue();
  }

  /**
   * Returns a string representation of this object.
   *
   * @return the display name
   */
  @Override
  public String toString() {
    return getDisplayName();
  }
}