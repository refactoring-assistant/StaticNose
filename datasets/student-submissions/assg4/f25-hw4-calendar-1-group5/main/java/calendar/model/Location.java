package calendar.model;

/**
 * Enum representing the mode of location.
 */
public enum Location {
  PHYSICAL,
  ONLINE;

  /**
   * Method to convert string literal to Location enum.
   *
   * @param value the string value passed
   * @return Location enum
   */
  public static Location stringToLocation(String value) {
    switch (value) {
      case "physical":
        return PHYSICAL;
      case "online":
        return ONLINE;
      default:
        throw new IllegalArgumentException("Unknown location");
    }
  }
}
