package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Command to edit events from a specific point forward.
 */
public class EditEventsCommand extends AbstractCommand {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditEventsCommand to edit events from a point onwards in a series.
   *
   * @param subject the subject of the event to edit
   * @param startDateTime the start date and time of the event to begin editing from
   * @param property the property to modify
   * @param newValue the new value for the property
   */
  public EditEventsCommand(String subject, LocalDateTime startDateTime,
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
      model.editEventsFrom(subject, startDateTime, property, value);
      view.displayMessage("Events edited successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}