package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.time.LocalDateTime;

/**
 * Command to edit an entire event series.
 */
public class EditSeriesCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditSeriesCommand.
   *
   * @param subject the subject of the event series
   * @param start the start date and time of any event in the series
   * @param property the property to edit
   * @param newValue the new value for the property
   */
  public EditSeriesCommand(String subject, LocalDateTime start,
                           String property, String newValue) {
    this.subject = subject;
    this.startDateTime = start;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      calendar.editEntireSeries(subject, startDateTime, property, newValue);
      view.displayMessage("Series edited successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}