package utils;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * An enum that represents all days of the week.
 * M : Monday
 * T : Tuesday
 * W : Wednesday
 * R : Thursday
 * F : Friday
 * S : Saturday
 * U : Sunday
 */
public enum Weekday {
  MONDAY('M', DayOfWeek.MONDAY),
  TUESDAY('T', DayOfWeek.TUESDAY),
  WEDNESDAY('W', DayOfWeek.WEDNESDAY),
  THURSDAY('R', DayOfWeek.THURSDAY),
  FRIDAY('F', DayOfWeek.FRIDAY),
  SATURDAY('S', DayOfWeek.SATURDAY),
  SUNDAY('U', DayOfWeek.SUNDAY);

  private static final List<Weekday> ORDER =
      List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY);

  private final char code;
  private final DayOfWeek dow;   // composition

  /**
   * Constructs a Weekday object and initializes the day of the week and its corresponding
   * character to it.
   *
   * @param code the character representing the day of the week
   * @param dow  the day of the week
   */
  Weekday(char code, DayOfWeek dow) {
    this.code = code;
    this.dow = dow;
  }

  /**
   * Maps the character representing the day of the week to a Weekday.
   *
   * @param c a character for the day of the week
   * @return a Weekday
   * @throws IllegalArgumentException if the character does not correspond to a valid Weekday
   */
  public static Weekday fromCode(char c) {
    char up = Character.toUpperCase(c);
    for (Weekday d : values()) {
      if (d.code == up) {
        return d;
      }
    }
    throw new IllegalArgumentException("Invalid weekday code: " + c + " (use M,T,W,R,F,S,U)");
  }

  /**
   * Maps the day of the week to a Weekday.
   *
   * @param dow a day of the week
   * @return a Weekday
   * @throws IllegalArgumentException if the day of the week does not correspond to a valid Weekday
   */
  public static Weekday from(DayOfWeek dow) {
    switch (dow) {
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
        throw new IllegalArgumentException("Unknown DayOfWeek: " + dow);
    }

  }

  /**
   * Parses together characters representing a series of weekdays into an EnumSet of Weekday
   * following canonical order.
   *
   * @param pattern a string of characters representing a series of weekdays
   * @return an enumSet of Weekday
   * @throws IllegalArgumentException if the string is null
   */
  public static EnumSet<Weekday> parsePattern(String pattern) {
    if (pattern == null) {
      throw new IllegalArgumentException("Pattern cannot be null");
    }
    EnumSet<Weekday> out = EnumSet.noneOf(Weekday.class);
    for (int i = 0; i < pattern.length(); i++) {
      char ch = pattern.charAt(i);
      if (!Character.isWhitespace(ch)) {
        out.add(fromCode(ch));
      }
    }
    // ensure MTWRFSU order in any later serialization
    return out;
  }

  /**
   * Serializes the weekdays back into the correct weekday order in the form of a string.
   *
   * @param days collection of Weekdays
   * @return a string representing a series of weekdays
   */
  // Serialize weekdays back to "MTWRFSU" order
  public static String toPattern(Collection<Weekday> days) {
    if (days == null || days.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (Weekday d : ORDER) {
      if (days.contains(d)) {
        sb.append(d.code);
      }
    }
    return sb.toString();
  }

  /**
   * Returns the corresponding character representing the weekday.
   *
   * @return the corresponding code
   */
  public char code() {
    return code;
  }

  /**
   * Returns the corresponding day of the week for the weekday.
   *
   * @return the day of teh week
   */
  public DayOfWeek asDayOfWeek() {
    return dow;
  }
}
