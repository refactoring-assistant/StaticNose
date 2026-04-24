package calendar.model;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration representing days of the week with single-character codes.
 * Used for specifying recurring event patterns.
 * M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday.
 */
public enum Weekday {
  /**
   * Monday.
   */
  M(DayOfWeek.MONDAY),

  /**
   * Tuesday.
   */
  T(DayOfWeek.TUESDAY),

  /**
   * Wednesday.
   */
  W(DayOfWeek.WEDNESDAY),

  /**
   * Thursday (R for thuRsday to avoid conflict with Tuesday).
   */
  R(DayOfWeek.THURSDAY),

  /**
   * Friday.
   */
  F(DayOfWeek.FRIDAY),

  /**
   * Saturday.
   */
  S(DayOfWeek.SATURDAY),

  /**
   * Sunday (U for sUnday).
   */
  U(DayOfWeek.SUNDAY);

  private static final Map<DayOfWeek, Weekday> DAY_MAP = new HashMap<>();

  static {
    for (Weekday weekday : values()) {
      DAY_MAP.put(weekday.dayOfWeek, weekday);
    }
  }

  private final DayOfWeek dayOfWeek;

  /**
   * Constructs a Weekday enum with associated DayOfWeek.
   *
   * @param dayOfWeek the Java DayOfWeek this weekday represents
   */
  Weekday(DayOfWeek dayOfWeek) {
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Converts a Java DayOfWeek to a Weekday enum.
   *
   * @param dayOfWeek the DayOfWeek to convert
   * @return the corresponding Weekday enum
   */
  public static Weekday fromDayOfWeek(DayOfWeek dayOfWeek) {
    return DAY_MAP.get(dayOfWeek);
  }

  /**
   * Parses a character code to a Weekday enum.
   *
   * @param code the single character code (M, T, W, R, F, S, U)
   * @return the corresponding Weekday enum
   * @throws IllegalArgumentException if code is invalid
   */
  public static Weekday fromChar(char code) {
    try {
      return Weekday.valueOf(String.valueOf(Character.toUpperCase(code)));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid weekday code: " + code);
    }
  }

  /**
   * Gets the Java DayOfWeek associated with this weekday.
   *
   * @return the DayOfWeek value
   */
  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }
}