package calendar.model.datatypes;

/**
 * Enum for all the fields of the events.
 */
public enum EventProperties {
  SUBJECT,
  START_DATA_TIME,
  END_DATA_TIME,
  DESCRIPTION,
  LOCATION,
  EVENT_STATUS;

  /**
   * Used to map a string input to an enum.
   *
   * @param property string input.
   * @return a valid enum corresponding to the string input.
   */
  public EventProperties getEventProperty(String property) {
    switch (property.toLowerCase()) {
      case "subject":
        return EventProperties.SUBJECT;
      case "start":
        return EventProperties.START_DATA_TIME;
      case "end":
        return EventProperties.END_DATA_TIME;
      case "description":
        return EventProperties.DESCRIPTION;
      case "location":
        return EventProperties.LOCATION;
      case "status":
        return EventProperties.EVENT_STATUS;
      default:
        throw new IllegalArgumentException("Error: Invalid Property: " + property);
    }

  }
}


