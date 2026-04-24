package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import java.time.ZonedDateTime;

/**
 * Command to edit all events in a recurring series.
 */
public class EditSeriesCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final String subject;
  private final ZonedDateTime start;
  private final String property;
  private final Object newValue;

  /**
   * Creates a command to edit an entire series of recurring events.
   *
   * @param subject   The name of the recurring event series.
   * @param start     The start time of the first event in the series.
   * @param property  The property to change (e.g., subject, start, end).
   * @param newValue  The new value to set for the property.
   */
  public EditSeriesCommand(CalendarControllerInterface controller, String subject,
                           ZonedDateTime start,
                           String property, Object newValue) {
    this.controller = controller;
    this.subject = subject;
    this.start = start;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public String execute() {
    try {
      controller.editEntireSeries(subject, start, property, newValue);
      return "Entire series edited successfully: " + subject;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Edit entire event series";
  }
}
