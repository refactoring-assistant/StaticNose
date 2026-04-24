package calendar.controller.commanddata;

import java.time.ZoneId;

/**
 * Data transfer object for CreateCalendarCommand parsed data.
 */
public class CreateCalendarCommandData {
  private final String name;
  private final ZoneId timezone;

  /**
   * Constructor for CreateCalendarCommandData.
   *
   * @param name     the calendar name
   * @param timezone the timezone
   */
  public CreateCalendarCommandData(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  public String getName() {
    return name;
  }

  public ZoneId getTimezone() {
    return timezone;
  }
}

