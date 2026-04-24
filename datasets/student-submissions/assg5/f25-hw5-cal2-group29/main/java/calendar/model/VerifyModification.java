package calendar.model;

import calendar.model.utils.DateTimeCheck;
import calendar.model.utils.EventStatus;
import java.time.LocalDateTime;

/**
 * A utility class for the Controller layer to parse and verify new values
 * during an 'edit' operation before sending them to the Model.
 */
public final class VerifyModification {

  private VerifyModification() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
  }

  /**
   * Parses a new property value from its string representation into the
   * correct Java Object type required by the model.
   *
   * @param propertyName   The name of the property being changed (e.g., "subject", "start").
   * @param newValueString The raw string value from the user command.
   * @return A typed Object (e.g., String, LocalDateTime, EventStatus).
   * @throws IllegalArgumentException if the propertyName is unknown or the
   *                                  newValueString is invalid for that property type.
   */
  public static Object parseNewValue(String propertyName, String newValueString) {
    if (newValueString == null) {
      throw new IllegalArgumentException("New value cannot be null.");
    }

    switch (propertyName.toLowerCase()) {
      case "subject":
        if (newValueString.trim().isEmpty()) {
          throw new IllegalArgumentException("Subject cannot be empty.");
        }
        return newValueString;

      case "description":
      case "location":
        return newValueString;

      case "start":
      case "end":
        return DateTimeCheck.parseDateTime(newValueString.trim());

      case "status":
        String value = newValueString.trim();
        try {
          return EventStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid status value: '" + value
              + "'. Must be 'PUBLIC' or 'PRIVATE'.");
        }

      default:
        throw new IllegalArgumentException("Unknown property: " + propertyName);
    }
  }
}