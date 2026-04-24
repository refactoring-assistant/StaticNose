package calendar.model;

/**
 * Represents all the properties that can be edited.
 */
public enum PropertyType {
  SUBJECT("subject"),
  START("start"),
  END("end"),
  DESCRIPTION("description"),
  LOCATION("location"),
  STATUS("status");

  private final String value;

  PropertyType(String value) {
    this.value = value;
  }

  /**
   * Gets the string value of the property type.
   *
   * @return the string value
   */
  public String getValue() {
    return value;
  }

  /**
   * Converts a string to a PropertyType enum.
   *
   * @param text the string to convert (e.g., "subject")
   * @return the corresponding PropertyType
   * @throws IllegalArgumentException if the text does not match any property type
   */
  public static PropertyType fromString(String text) {
    for (PropertyType type : PropertyType.values()) {
      if (type.value.equalsIgnoreCase(text)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid property: " + text
        + ". Must be one of: subject, start, end, description, location, status");
  }
}