package calendar.controller.commands;

import calendar.controller.CalendarContext;
import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;

/**
 * Command to set the current calendar context.
 */
public class UseCalendarCommand implements Command {
  private final String calendarName;

  /**
   * Constructs a UseCalendarCommand.
   *
   * @param calendarName the calendar to use
   */
  public UseCalendarCommand(String calendarName) {
    this.calendarName = calendarName;
  }

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    throw new UnsupportedOperationException(
        "Use executeOnSystem() for calendar management commands");
  }

  /**
   * Executes command on the calendar system.
   *
   * @param context the calendar context
   * @param view the view
   */
  public void executeOnSystem(CalendarContext context, CalendarView view) {
    try {
      context.setCurrentCalendar(calendarName);
      view.displayMessage("Now using calendar: " + calendarName);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }
}