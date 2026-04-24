package calendar.controller.commands;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Command to edit a single event.
 */
public class EditEventCommand extends AbstractCommand {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditEventCommand to edit a single event's property.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param property      the property to modify
   * @param newValue      the new value for the property
   */
  public EditEventCommand(String subject, LocalDateTime startDateTime,
                          LocalDateTime endDateTime, String property, String newValue) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarModel model, CalendarView view) throws IOException {
    try {
      Object value = parsePropertyValue(property, newValue);
      model.editEvent(subject, startDateTime, endDateTime, property, value);
      view.displayMessage("Event edited successfully.");
    } catch (Exception e) {
      view.displayError("ERROR DETAILS: " + e.getClass().getName() + ": " + e.getMessage());
    }
  }
}