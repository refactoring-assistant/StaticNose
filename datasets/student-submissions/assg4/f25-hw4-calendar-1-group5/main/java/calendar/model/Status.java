package calendar.model;

/**
 * Enum representing the status of event.
 */
public enum Status {
  PUBLIC,
  PRIVATE;

  /**
   * Method to convert string literal to status enum.
   *
   * @param value the string value passed
   * @return Status enum
   */
  public static Status stringToStatus(String value) {
    switch (value) {
      case "public":
        return PUBLIC;
      case "private":
        return PRIVATE;
      default:
        throw new IllegalArgumentException("Unknown status");
    }
  }
}
