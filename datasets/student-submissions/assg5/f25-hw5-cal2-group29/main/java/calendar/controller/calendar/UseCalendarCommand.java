package calendar.controller.calendar;

import calendar.controller.Command;
import calendar.model.CalendarApplication;
import calendar.view.CalendarView;

/**
 * Command class for using calendar.
 * Implements the Command interface and calls the
 * corresponding method on the CalendarApplication model.
 */
public class UseCalendarCommand implements Command {

  private final String calendarName;

  /**
   * Constructs a command to set the active calendar.
   *
   * @param calendarName The name of the calendar to set as active.
   */
  public UseCalendarCommand(String calendarName) {
    this.calendarName = calendarName;
  }

  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      model.useCalendar(calendarName);
      view.displaySuccess("Now using calendar '" + calendarName + "'.");
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}