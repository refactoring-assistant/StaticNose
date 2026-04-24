package calendar.command;

/**
 * Represents the scope of an edit operation on an event series.
 */
public enum EditScope {
  EVENT,
  EVENTS,
  SERIES;

  /**
   * Parses a string to an EditScope.
   *
   * @param s The string to parse.
   * @return The corresponding EditScope.
   * @throws IllegalArgumentException if the string is not a valid scope.
   */
  public static EditScope fromString(String s) {
    try {
      return valueOf(s.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid scope: " + s);
    }
  }
}
