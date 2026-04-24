package calendar.model;

import java.time.DayOfWeek;

/**
 * Represents the days of the week with short single-letter codes (M, T, W, R, F, S, U). Used
 * for compact representation of recurring event patterns (e.g., "MWF" for Monday, Wednesday,
 * Friday).
 */
public enum Weekday {
  MONDAY("M"), TUESDAY("T"), WEDNESDAY("W"), THURSDAY("R"),
  FRIDAY("F"), SATURDAY("S"), SUNDAY("U");

  private final String code;

  Weekday(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  /**
   * Returns the short single-letter code for this weekday.
   *
   * @return the code (e.g., "M", "T", "W").
   */
  public DayOfWeek toDayOfWeek() {
    switch (this) {
      case MONDAY:
        return DayOfWeek.MONDAY;
      case TUESDAY:
        return DayOfWeek.TUESDAY;
      case WEDNESDAY:
        return DayOfWeek.WEDNESDAY;
      case THURSDAY:
        return DayOfWeek.THURSDAY;
      case FRIDAY:
        return DayOfWeek.FRIDAY;
      case SATURDAY:
        return DayOfWeek.SATURDAY;
      case SUNDAY:
        return DayOfWeek.SUNDAY;
      default:
        throw new IllegalStateException("Unexpected value: " + this);
    }
  }

  /**
   * Converts this Weekday to the corresponding DayOfWeek.
   *
   * @return the equivalent DayOfWeek value.
   */
  public static Weekday fromCode(String code) {
    if (code == null || code.isEmpty()) {
      throw new IllegalArgumentException("Empty code");
    }
    switch (code.toUpperCase()) {
      case "M":
        return MONDAY;
      case "T":
        return TUESDAY;
      case "W":
        return WEDNESDAY;
      case "R":
        return THURSDAY;
      case "F":
        return FRIDAY;
      case "S":
        return SATURDAY;
      case "U":
        return SUNDAY;
      default:
        throw new IllegalArgumentException("Invalid weekday code: " + code);
    }
  }
}