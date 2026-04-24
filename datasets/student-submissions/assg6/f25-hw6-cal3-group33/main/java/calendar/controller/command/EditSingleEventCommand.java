package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to edit a single event instance.
 *
 * <p>Edits only the specified event occurrence. If the event is part of a series
 * and a temporal property (start/end) is changed, the event becomes standalone.
 *
 * <p>Editable properties: subject, start, end, description, location, status
 */
public class EditSingleEventCommand implements Command {

  private final String property;
  private final String subject;
  private final String startDateTime;
  private final String endDateTime;
  private final String newValue;

  /**
   * Creates a command to edit a single event.
   *
   * @param property the property to edit (subject, start, end, description, location, status)
   * @param subject the current subject of the event
   * @param startDateTime the current start date/time (format: YYYY-MM-DDThh:mm)
   * @param endDateTime the current end date/time (format: YYYY-MM-DDThh:mm)
   * @param newValue the new value for the property
   */
  public EditSingleEventCommand(String property, String subject, String startDateTime,
                                String endDateTime, String newValue) {
    this.property = property;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    calendar.editEvent(property, subject, startDateTime, endDateTime, newValue);

    view.displayMessage("Edited Single Event Instance: " + subject);
  }
}