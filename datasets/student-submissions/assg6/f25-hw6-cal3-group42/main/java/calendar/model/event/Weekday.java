package calendar.model.event;

import java.time.DayOfWeek;
import java.util.HashSet;

/**
 * Enum representing weekday codes used in the calendar application.
 * Maps single-character codes to Java's DayOfWeek enum values.
 */
public enum Weekday {
  MONDAY('M', DayOfWeek.MONDAY),
  TUESDAY('T', DayOfWeek.TUESDAY),
  WEDNESDAY('W', DayOfWeek.WEDNESDAY),
  THURSDAY('R', DayOfWeek.THURSDAY),
  FRIDAY('F', DayOfWeek.FRIDAY),
  SATURDAY('S', DayOfWeek.SATURDAY),
  SUNDAY('U', DayOfWeek.SUNDAY);

  private final char code;
  private final DayOfWeek dayOfWeek;

  /**
   * Constructs a WeekdayCode with its character code and DayOfWeek value.
   *
   * @param code      the single character code (M, T, W, R, F, S, U)
   * @param dayOfWeek the corresponding DayOfWeek value
   */
  Weekday(char code, DayOfWeek dayOfWeek) {
    this.code = code;
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Gets the single character code for this weekday.
   *
   * @return the character code (M, T, W, R, F, S, or U).
   */
  public char getCode() {
    return code;
  }

  /**
   * Gets the Java DayOfWeek enum value for this weekday.
   *
   * @return the corresponding DayOfWeek value.
   */
  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }

  /**
   * Converts a character code to a DayOfWeek value.
   *
   * @param c the weekday code character
   * @return the corresponding DayOfWeek value
   * @throws IllegalArgumentException if the character is not a valid weekday code
   */
  public static DayOfWeek toDayOfWeek(char c) {
    for (Weekday weekday : values()) {
      if (weekday.code == c) {
        return weekday.dayOfWeek;
      }
    }
    throw new IllegalArgumentException("Invalid day of week: " + c);
  }

  /**
   * Parses a string of weekday codes into a set of DayOfWeek values.
   *
   * @param weekdayString string containing weekday codes (e.g., "MTW")
   * @return set of DayOfWeek values
   * @throws IllegalArgumentException if any character is not a valid code
   */
  public static HashSet<DayOfWeek> parseDaysOfWeek(String weekdayString) {
    HashSet<DayOfWeek> setOfDays = new HashSet<>();
    for (char c : weekdayString.toUpperCase().toCharArray()) {
      setOfDays.add(toDayOfWeek(c));
    }
    return setOfDays;
  }
}