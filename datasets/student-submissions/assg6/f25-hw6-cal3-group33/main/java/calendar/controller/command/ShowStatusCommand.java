package calendar.controller.command;

import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.calendar.ReadOnlyCalendarAdapter;
import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to show busy/free status at a specific datetime.
 *
 * <p>Checks if the user is busy (has an event scheduled) at the specified time.
 * Returns true if any event is scheduled at that exact time, false otherwise.
 */
public class ShowStatusCommand implements Command {

  private final String dateTime;

  /**
   * Creates a command to check busy status.
   *
   * @param dateTime the datetime to check (format: YYYY-MM-DDThh:mm)
   */
  public ShowStatusCommand(String dateTime) {
    this.dateTime = dateTime;
  }

  /**
   * Performs the execution and calls the view.
   */
  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    ReadOnlyCalendar calendar = new ReadOnlyCalendarAdapter(manager.getCurrentCalendar());
    view.displayBusyStatus(calendar, dateTime);
  }
}