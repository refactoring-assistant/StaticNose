package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to create a single event with specified start and end times.
 *
 * <p>Creates a non-recurring event that occurs once at the specified date and time.
 */
public class CreateEventCommand implements Command {

  private final String subject;
  private final String startDateTime;
  private final String endDateTime;

  /**
   * Creates a command to create a single event.
   *
   * @param subject the event subject/title
   * @param startDateTime the start date and time (format: YYYY-MM-DDThh:mm)
   * @param endDateTime the end date and time (format: YYYY-MM-DDThh:mm)
   */
  public CreateEventCommand(String subject, String startDateTime, String endDateTime) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    calendar.newEvent(subject, startDateTime)
        .end(endDateTime)
        .create(calendar);

    view.displayMessage("Created Single Event: " + subject);
  }
}