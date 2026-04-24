package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.time.LocalDateTime;

/**
 * Command to edit events in a series from a specific date forward.
 */
public class EditEventsFromDateCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditEventsFromDateCommand.
   *
   * @param subject the subject of the event series
   * @param start the start date and time to begin editing from
   * @param property the property to edit
   * @param newValue the new value for the property
   */
  public EditEventsFromDateCommand(String subject, LocalDateTime start,
                                   String property, String newValue) {
    this.subject = subject;
    this.startDateTime = start;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    try {
      calendar.editEventsFromDate(subject, startDateTime, property, newValue);
      view.displayMessage("Events edited successfully.");
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}