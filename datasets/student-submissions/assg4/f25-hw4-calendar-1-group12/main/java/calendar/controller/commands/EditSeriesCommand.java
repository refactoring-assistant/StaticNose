package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Command to edit all events in a series.
 */
public class EditSeriesCommand extends AbstractCommand {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditSeriesCommand to edit all events in a series.
   *
   * @param subject the subject of the event series to edit
   * @param startDateTime the start date and time of any event in the series
   * @param property the property to modify
   * @param newValue the new value for the property
   */
  public EditSeriesCommand(String subject, LocalDateTime startDateTime,
                           String property, String newValue) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    try {
      Object value = parsePropertyValue(property, newValue);
      model.editAllEventsInSeries(subject, startDateTime, property, value);
      view.displayMessage("Event series edited successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}