package calendar.command;

/**
 * Represents the properties of an event that can be edited.
 */
public enum EditProperty {
  SUBJECT,
  START,
  END,
  DESCRIPTION,
  LOCATION,
  STATUS;

  /**
   * Parses a string to an EditProperty.
   *
   * @param s The string to parse.
   * @return The corresponding EditProperty.
   * @throws IllegalArgumentException if the string is not a valid property.
   */
  public static EditProperty fromString(String s) {
    try {
      return valueOf(s.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid property: " + s);
    }
  }
}
