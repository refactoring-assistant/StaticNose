package calendar.view.dto;

/**
 * The class below is the data transfer object for the create calendar functionality.
 */
public class CreateCalDto implements CreateCalDtoI {
  private final String calendarName;
  private final String timezone;

  /**
   * Public constructor used to initialize the object.
   *
   * @param calendarName name of calendar.
   * @param timezone     the timezone of the calendar
   */
  public CreateCalDto(String calendarName, String timezone) {
    this.calendarName = calendarName;
    this.timezone = timezone;
  }

  /**
   * Return the name of calendar.
   *
   * @return the calendar name.
   */
  public String getCalendarName() {
    return calendarName;
  }

  /**
   * Return the time zone of the calendar.
   *
   * @return the timezone.
   */
  public String getTimezone() {
    return timezone;
  }
}