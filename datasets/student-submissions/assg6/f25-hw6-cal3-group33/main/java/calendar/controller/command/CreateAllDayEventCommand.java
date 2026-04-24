package calendar.controller.command;

import calendar.model.calendar.CalendarInterface;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to create a single all-day event.
 *
 * <p>Creates an event that spans an entire day (8:00 AM to 5:00 PM by default).
 */
public class CreateAllDayEventCommand implements Command {

  private final String subject;
  private final String date;

  /**
   * Creates a command to create a single all-day event.
   *
   * @param subject the event subject/title
   * @param date the date for the event (format: YYYY-MM-DD)
   */
  public CreateAllDayEventCommand(String subject, String date) {
    this.subject = subject;
    this.date = date;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    CalendarInterface calendar = manager.getCurrentCalendar();

    calendar.newEvent(subject, date).create(calendar);

    view.displayMessage("Created Single All-Day Event: " + subject);
  }
}