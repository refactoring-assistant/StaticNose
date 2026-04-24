package calendar.model;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents days of the week using single char codes.
 */
public enum WeekDay {
  MONDAY('M', DayOfWeek.MONDAY),
  TUESDAY('T', DayOfWeek.TUESDAY),
  WEDNESDAY('W', DayOfWeek.WEDNESDAY),
  THURSDAY('R', DayOfWeek.THURSDAY),
  FRIDAY('F', DayOfWeek.FRIDAY),
  SATURDAY('S', DayOfWeek.SATURDAY),
  SUNDAY('U', DayOfWeek.SUNDAY);

  private final char code;
  private final DayOfWeek dayOfWeek;

  WeekDay(char code, DayOfWeek dayOfWeek) {
    this.code = code;
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Gets the single-character code for this day.
   *
   * @return the day code
   */
  public char getCode() {
    return code;
  }

  /**
   * Gets the Java DayOfWeek equivalent.
   *
   * @return the DayOfWeek
   */
  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }

  /**
   * Parses a single character to a WeekDay.
   *
   * @param code the character code
   * @return the corresponding WeekDay
   * @throws IllegalArgumentException if code is invalid
   */
  public static WeekDay fromCode(char code) throws IllegalArgumentException {
    char upper = Character.toUpperCase(code);
    for (WeekDay day : values()) {
      if (day.code == upper) {
        return day;
      }
    }
    throw new IllegalArgumentException("Invalid weekday code: " + code);
  }

  /**
   * Parses a string of weekday codes to a list of WeekDays.
   *
   * @param codes the string of codes (e.g. "MWF")
   * @return list of WeekDays
   * @throws IllegalArgumentException if any code is invalid
   */
  public static List<WeekDay> fromCodes(String codes) throws IllegalArgumentException {
    if (codes == null || codes.trim().isEmpty()) {
      throw new IllegalArgumentException("Weekday codes cannot be empty");
    }

    List<WeekDay> days = new ArrayList<>();
    String trimmed = codes.trim().toUpperCase();

    for (char c : trimmed.toCharArray()) {
      days.add(fromCode(c));
    }

    return days;
  }

  /**
   * Converts a DayOfWeek to a WeekDay.
   *
   * @param dayOfWeek the Java DayOfWeek
   * @return the corresponding WeekDay
   */
  public static WeekDay fromDayOfWeek(DayOfWeek dayOfWeek) {
    for (WeekDay day : values()) {
      if (day.dayOfWeek == dayOfWeek) {
        return day;
      }
    }
    throw new IllegalArgumentException("Invalid DayOfWeek: " + dayOfWeek);
  }

  /**
   * Delegates directly to fromCodes().
   *
   * @param codes string like "MWF"
   * @return list of WeekDays
   */
  public static List<WeekDay> parseDays(String codes) {
    return fromCodes(codes);
  }
}