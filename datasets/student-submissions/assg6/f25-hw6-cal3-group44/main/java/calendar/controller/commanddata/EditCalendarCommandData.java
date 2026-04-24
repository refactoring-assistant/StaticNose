package calendar.controller.commanddata;

/**
 * Data transfer object for EditCalendarCommand parsed data.
 */
public class EditCalendarCommandData {
  private final String calendarName;
  private final String propertyName;
  private final String newPropertyValue;

  /**
   * Constructor for EditCalendarCommandData.
   *
   * @param calendarName     the calendar name
   * @param propertyName     the property to update
   * @param newPropertyValue the new property value
   */
  public EditCalendarCommandData(String calendarName, String propertyName,
                                 String newPropertyValue) {
    this.calendarName = calendarName;
    this.propertyName = propertyName;
    this.newPropertyValue = newPropertyValue;
  }

  public String getCalendarName() {
    return calendarName;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getNewPropertyValue() {
    return newPropertyValue;
  }
}

