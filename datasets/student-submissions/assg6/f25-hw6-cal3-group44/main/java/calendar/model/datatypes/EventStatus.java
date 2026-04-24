package calendar.model.datatypes;

/**
 * Enum used to mark the event status.
 */
public enum EventStatus {
  PUBLIC, PRIVATE, UNKNOWN;

  /**
   * Used to map a string input to an enum.
   *
   * @param status string input.
   * @return a valid enum corresponding to the string input.
   */
  public static EventStatus getEventStatus(String status) {
    if (status.equalsIgnoreCase("public")) {
      return PUBLIC;
    } else if (status.equalsIgnoreCase("private")) {
      return PRIVATE;
    }
    return UNKNOWN;
  }
}
