package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.time.LocalDateTime;

/**
 * Command to edit a single event.
 */
public class EditEventCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditEventCommand.
   *
   * @param subject the subject of the event to edit
   * @param start the start date and time of the event
   * @param property the property to edit
   * @param newValue the new value for the property
   */
  public EditEventCommand(String subject, LocalDateTime start,
                          String property, String newValue) {
    this.subject = subject;
    this.startDateTime = start;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      calendar.editEvent(subject, startDateTime, property, newValue);
      view.displayMessage("Event edited successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}