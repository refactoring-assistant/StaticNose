package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;

/**
 * Command to get the currently active calendar.
 * Example: get active calendar
 */
public class GetActiveCalendarCommand implements CommandInterface {

  private final CalendarManagerControllerInterface controller;

  /**
   * Creates a command that retrieves the currently active calendar
   * from the provided calendar manager controller.
   *
   * @param controller the calendar manager controller to query.
   */
  public GetActiveCalendarCommand(CalendarManagerControllerInterface controller) {
    this.controller = controller;
  }

  @Override
  public String execute() {
    try {
      String name = controller.getActiveCalendarName();
      String zone = controller.getActiveCalendarZone().getId();
      return "Active calendar: '" + name + "' (Timezone: " + zone + ")";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Get the active calendar";
  }
}
