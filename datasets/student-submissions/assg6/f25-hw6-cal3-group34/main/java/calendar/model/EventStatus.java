package calendar.model;

/**
 * Represents the visibility of an event on the calendar.
 */
public enum EventStatus {
  PUBLIC,
  PRIVATE;

  /**
   * Parses an event status from a free-form string.
   *
   * @param value the raw value
   * @return the corresponding status
   * @throws IllegalArgumentException if the value cannot be parsed
   */
  public static EventStatus fromString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Status value cannot be null.");
    }
    String normalized = value.trim().toUpperCase();
    switch (normalized) {
      case "PUBLIC":
        return PUBLIC;
      case "PRIVATE":
        return PRIVATE;
      default:
        throw new IllegalArgumentException("Unsupported status value: " + value);
    }
  }
}
