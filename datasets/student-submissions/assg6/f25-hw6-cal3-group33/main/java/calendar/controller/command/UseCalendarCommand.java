package calendar.controller.command;

import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to switch to using a specific calendar.
 *
 * <p>Sets the specified calendar as the current active calendar.
 * All subsequent event operations will be performed on this calendar
 * until a different calendar is selected.
 */
public class UseCalendarCommand implements Command {

  private final String calendarName;

  /**
   * Creates a command to use the specified calendar.
   *
   * @param calendarName the name of the calendar to use
   */
  public UseCalendarCommand(String calendarName) {
    this.calendarName = calendarName;
  }

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    manager.useCalendar(calendarName);
    view.displayMessage("Now using calendar: " + calendarName);
  }
}