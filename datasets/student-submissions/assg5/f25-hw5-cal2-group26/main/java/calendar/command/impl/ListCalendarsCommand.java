package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarManagerControllerInterface;
import java.util.Collection;

/**
 * Command to list all calendars.
 * Example: list calendars
 */
public class ListCalendarsCommand implements CommandInterface {
  private final CalendarManagerControllerInterface controller;

  /**
   * Creates a command that lists all calendars managed by the given controller.
   *
   * @param controller the calendar manager controller to query.
   */
  public ListCalendarsCommand(CalendarManagerControllerInterface controller) {
    this.controller = controller;
  }

  @Override
  public String execute() {
    try {
      Collection<String> calendars = controller.listCalendars();
      if (calendars.isEmpty()) {
        return "No calendars found";
      }
      String active = controller.getActiveCalendarName();
      StringBuilder sb = new StringBuilder("Calendars:\n");
      for (String cal : calendars) {
        sb.append("  ");
        if (cal.equals(active)) {
          sb.append("* ");
        }
        sb.append(cal).append("\n");
      }
      return sb.toString().trim();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "List all calendars";
  }
}
