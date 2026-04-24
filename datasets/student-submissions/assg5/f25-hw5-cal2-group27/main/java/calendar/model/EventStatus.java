package calendar.model;

/**
 * Represents the status of an event, either public or private.
 */
public enum EventStatus {
  PUBLIC,
  PRIVATE;

  /**
   * Parses a string to an EventStatus.
   *
   * @param s The string to parse.
   * @return The corresponding EventStatus.
   * @throws IllegalArgumentException if the string is not a valid status.
   */
  public static EventStatus fromString(String s) {
    for (EventStatus status : values()) {
      if (status.name().equalsIgnoreCase(s)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid status: " + s);
  }
}
