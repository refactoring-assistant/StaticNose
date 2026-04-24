package calendar.controller.commanddata;

/**
 * Data transfer object for UseCalendarCommand parsed data.
 */
public class UseCalendarCommandData {
  private final String calendarName;

  /**
   * Constructor for UseCalendarCommandData.
   *
   * @param calendarName the name of the calendar to use
   */
  public UseCalendarCommandData(String calendarName) {
    this.calendarName = calendarName;
  }

  public String getCalendarName() {
    return calendarName;
  }
}

