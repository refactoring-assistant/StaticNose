package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;

/**
 * Creates a new calendar using name and timezone.
 */
public class CreateCalendarCommand implements Command {

  private final String name;
  private final String timezone;

  /**
   * Constructing a command using command line input.
   *
   * @param input command entered by user.
   * @throws IllegalArgumentException Invalid syntax.
   */
  public CreateCalendarCommand(String input) {
    String[] tokens = input.trim().split("\\s+");
    if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("--name")
        || !tokens[4].equalsIgnoreCase("--timezone")) {
      throw new IllegalArgumentException(
          "Invalid syntax. Expected: create calendar --name <name> --timezone <area/location>");
    }
    this.name = tokens[3];
    this.timezone = tokens[5];
  }

  @Override
  public String execute(CalendarSystemModel model) {
    try {
      model.createCalendar(name, timezone);
      return "Created calendar: " + name + " [" + timezone + "]";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}
