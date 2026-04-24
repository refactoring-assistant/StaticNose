package calendar.model;

/**
 * Enum to indicate location of the event in calendar.
 */
public enum EventLocation {
  ONLINE("Online"),
  PHYSICAL("Physical");
  private final String location;

  EventLocation(String location) {
    this.location = location;
  }

  /**
   * Static method to convert a string representation in the desired Location Enum.
   *
   * @param locate String to be converted from
   * @return corresponding enum value for that string representation
   * @throws IllegalArgumentException if the string is invalid i.e other than Online and Physical
   */
  public static EventLocation fromString(String locate) throws IllegalArgumentException {
    if (locate == null) {
      throw new IllegalArgumentException("Location cannot be null");
    }
    for (EventLocation l : EventLocation.values()) {
      if (l.location.equalsIgnoreCase(locate)) {
        return l;
      }
    }
    throw new IllegalArgumentException("Unknown event location: " + locate);
  }

  /**
   * Method to get the representation of status in string format.
   *
   * @return the status of event - ONLINE or PHYSICAL
   */
  @Override
  public String toString() {
    return location;
  }
}