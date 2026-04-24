package calendar.view.dto;

/**
 * The class below is the data transfer object which is used by the select calendar functionality.
 */
public class SelectCalDto implements SelectCalDtoI {
  private final String calendarName;

  /**
   * Public constructor which initializes the object.
   *
   * @param calendarName The name of the calendar to set.
   */
  public SelectCalDto(String calendarName) {
    this.calendarName = calendarName;
  }

  /**
   * Return the name of the calendar.
   *
   * @return name of calendar.
   */
  public String getCalendarName() {
    return calendarName;
  }
}