package calendar.model;

/**
 * Represents a Calendar property for fields like name and timezone.
 */
public enum CalendarProperty {
  NAME("name"),
  TIMEZONE("timezone");

  private final String prop;

  /**
   * Constructs an enum calendar property given property name in String.
   *
   * @param prop name of a calendar property.
   */
  private CalendarProperty(String prop) {
    this.prop = prop;
  }
}
