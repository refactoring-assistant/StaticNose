package calendar.model;

/**
 * Represents the visibility status of an event.
 */
public enum EventStatus {
  PUBLIC,
  PRIVATE;

  /**
   * Parses a string to an EventStatus.
   *
   * @param status the string to parse (case-insensitive)
   * @return the corresponding EventStatus
   * @throws IllegalArgumentException if status is invalid
   */
  public static EventStatus fromString(String status) throws IllegalArgumentException {
    if (status == null) {
      return PUBLIC;
    }

    String normalized = status.trim().toUpperCase();

    switch (normalized) {
      case "PUBLIC":
        return PUBLIC;
      case "PRIVATE":
        return PRIVATE;
      default:
        throw new IllegalArgumentException("Invalid status: " + status);
    }
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
