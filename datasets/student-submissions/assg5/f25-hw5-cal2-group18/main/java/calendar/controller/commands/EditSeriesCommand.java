package calendar.controller.commands;

import calendar.model.Calendar;
import java.time.LocalDateTime;

/**
 * Command to edit all events in a series.
 */
public class EditSeriesCommand implements CalendarCommand {

  private final String property;
  private final String subject;
  private final LocalDateTime start;
  private final String newValue;

  /**
   * Constructs an EditSeriesCommand.
   *
   * @param property the event property to edit
   * @param subject  the subject of the event to edit
   * @param start    the start time of the event
   * @param newValue the new value to set for the property
   */
  public EditSeriesCommand(String property, String subject,
                           LocalDateTime start, String newValue) {
    this.property = property;
    this.subject = subject;
    this.start = start;
    this.newValue = newValue;
  }

  @Override
  public String execute(Calendar model) {
    try {
      model.editEntireSeries(subject, start, property, newValue);
      return "Updated entire series: " + subject;
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }
}