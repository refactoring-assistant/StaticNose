package calendar.exceptions;

/**
 * Thrown when an attempt is made to access or modify an invalid property.
 * This occurs when a property name does not match any of the valid
 * properties for an event or calendar entity.
 */
public class InvalidPropertyException extends Exception {

  /**
   * Constructs an InvalidPropertyException for the specified invalid property.
   * The exception message includes the invalid property name and lists all valid properties.
   *
   * @param property the invalid property name that was specified
   * @param validProperties an array of valid property names for reference
   */
  public InvalidPropertyException(String property, String[] validProperties) {
    super(String.format("Unknown property: '%s'. Valid properties: %s",
        property, String.join(", ", validProperties)));
  }
}