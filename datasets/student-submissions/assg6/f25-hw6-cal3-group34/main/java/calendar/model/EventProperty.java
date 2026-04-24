package calendar.model;

/**
 * The set of editable properties supported by the calendar model.
 */
public enum EventProperty {
  SUBJECT,
  START,
  END,
  DESCRIPTION,
  LOCATION,
  STATUS;

  /**
    * Parses the property name from a command input.
    *
    * @param token user supplied token
    * @return the property
    * @throws IllegalArgumentException if no property matches the token
    */
  public static EventProperty fromToken(String token) {
    if (token == null) {
      throw new IllegalArgumentException("Property token cannot be null.");
    }
    switch (token.trim().toLowerCase()) {
      case "subject":
        return SUBJECT;
      case "start":
        return START;
      case "end":
        return END;
      case "description":
        return DESCRIPTION;
      case "location":
        return LOCATION;
      case "status":
        return STATUS;
      default:
        throw new IllegalArgumentException("Unsupported property: " + token);
    }
  }
}
