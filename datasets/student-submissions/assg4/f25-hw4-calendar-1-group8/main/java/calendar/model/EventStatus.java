package calendar.model;

/**
 * Enum to indicate the status of the calendar event.
 */
public enum EventStatus {
  PUBLIC("public"),
  PRIVATE("private");

  private final String statusName;

  EventStatus(String statusName) {
    this.statusName = statusName;
  }

  /**
   * Static method to convert a string representation in the desired EventStatus Enum.
   *
   * @param status String to be converted from
   * @return corresponding enum value for that string representation
   * @throws IllegalArgumentException if the string is invalid i.e other than Public and Private
   */
  public static EventStatus fromString(String status) throws IllegalArgumentException {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    for (EventStatus eventStatus : EventStatus.values()) {
      if (eventStatus.statusName.equals(status)) {
        return eventStatus;
      }
    }
    throw new IllegalArgumentException("Unknown event status: " + status);
  }

  /**
   * Method to get the representation of status in string format.
   *
   * @return the status of event - PUBLIC or PRIVATE
   */
  @Override
  public String toString() {
    return statusName;
  }
}