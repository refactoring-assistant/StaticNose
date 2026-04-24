package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;

/**
 * Command to switch the active calendar context. After executing this command, all
 * subsequent event operations will apply to the specified calendar.
 * Example: use calendar --name Work
 */
public class UseCalendarCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;
  private final String calendarName;

  /**
   * Constructs a command to switch to the specified calendar.
   *
   * @param calendarName name of the calendar to activate
   */
  public UseCalendarCommand(CalendarManagerControllerInterface controller,
                            String calendarName) {
    this.controller = controller;
    this.calendarName = calendarName;
  }

  @Override
  public String execute() {
    try {
      controller.switchCalendar(calendarName);
      return "Switched to calendar: " + calendarName;
    } catch (RuntimeException e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Switch to calendar: " + calendarName;
  }
}