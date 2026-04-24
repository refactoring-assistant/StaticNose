package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import java.util.Objects;

/**
 * Command to delete a calendar.
 * Example: delete calendar --name Work
 */
public class DeleteCalendarCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;
  private final String calendarName;

  /**
   * Creates a command to delete the calendar with the given name.
   */
  public DeleteCalendarCommand(CalendarManagerControllerInterface controller, String calendarName) {
    this.controller = controller;
    this.calendarName = Objects.requireNonNull(calendarName, "Calendar name cannot be null");
  }

  @Override
  public String execute() {
    try {
      String active = controller.getActiveCalendarName();
      if (calendarName.equals(active)) {
        return "Error: Cannot delete the active calendar. Switch to another calendar first.";
      }
      controller.deleteCalendar(calendarName);
      return "Calendar '" + calendarName + "' deleted successfully";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Delete calendar '" + calendarName + "'";
  }
}
