package calendar.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum to represent the day of the week and the associated day code.
 * */
public enum WeekDays {
  MONDAY('M'),
  TUESDAY('T'),
  WEDNESDAY('W'),
  THURSDAY('R'),
  FRIDAY('F'),
  SATURDAY('S'),
  SUNDAY('U');

  private final char dayCode;
  private static final Map<Character, WeekDays> MAPPING = new HashMap<>();

  static {
    for (WeekDays weekDays : WeekDays.values()) {
      MAPPING.put(weekDays.dayCode, weekDays);
    }
  }

  /**
   * Constructor to initialise the Enum.
   *
   * @param dayCode the provided day coe
   * */
  WeekDays(char dayCode) {
    this.dayCode = dayCode;
  }

  /**
   * Function to obtain day based on the day code.
   *
   * @param code the provided day code
   * */
  public static WeekDays fromCode(char code) {
    WeekDays days = MAPPING.get(code);
    if (days == null) {
      throw new IllegalArgumentException("Unknown week day code " + code);
    }

    return days;
  }
}
