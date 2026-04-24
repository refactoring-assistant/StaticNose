package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;

/**
 * Handles the calendar usage.
 */
public class UseCalendarCommand implements Command {

  private final String name;

  /**
   * Constructing command from user input.
   *
   * @param input user input
   * @throws IllegalArgumentException Invalid syntax.
   */
  public UseCalendarCommand(String input) {
    String[] tokens = input.trim().split("\\s+");
    if (tokens.length < 4 || !tokens[2].equalsIgnoreCase("--name")) {
      throw new IllegalArgumentException(
          "Invalid syntax. Expected: use calendar --name <calendarName>");
    }
    this.name = tokens[3];
  }

  @Override
  public String execute(CalendarSystemModel model) {
    try {
      model.useCalendar(name);
      return "Using calendar: " + name;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}
