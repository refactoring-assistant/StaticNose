package calendar.model.datatypes;

/**
 * Enum to represent the location of the event.
 */
public enum Location {
  PHYSICAL, ONLINE, UNKNOWN;

  /**
   * Used to map a string input to an enum.
   *
   * @param location string input.
   * @return a valid enum corresponding to the string input.
   */

  public static Location getLocation(String location) {
    if (location.equalsIgnoreCase("physical")) {
      return PHYSICAL;
    } else if (location.equalsIgnoreCase("online")) {
      return ONLINE;
    }
    return UNKNOWN;
  }
}
