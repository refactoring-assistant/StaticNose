package calendar.controller.commands;

import calendar.model.CalendarSystem;

/**
 * Command to set the current calendar context.
 */
public class UseCalendarCommand implements SystemCommand {
  private final String calendarName;

  /**
   * Sets the current cal.
   *
   * @param calendarName name of the cal
   */
  public UseCalendarCommand(String calendarName) {
    this.calendarName = calendarName;
  }

  @Override
  public String execute(CalendarSystem system) {
    try {
      system.setCurrentCalendar(calendarName);
      return "Now using calendar: " + calendarName;
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }
}