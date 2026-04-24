package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;

/**
 * Handles the editing of the calendar.
 */
public class EditCalendarCommand implements Command {

  private final String name;
  private final String property;
  private final String value;

  /**
   * Input is parsed to get the target calendar, properties and the new value.
   *
   * @param input user input command.
   * @throws IllegalArgumentException Invalid command.
   */
  public EditCalendarCommand(String input) {
    String[] tokens = input.trim().split("\\s+");
    if (tokens.length < 7 || !tokens[2].equalsIgnoreCase("--name")
        || !tokens[4].equalsIgnoreCase("--property")) {
      throw new IllegalArgumentException(
          "Invalid syntax. Expected: edit calendar --name <name> --property <property> <value>");
    }
    this.name = tokens[3];
    this.property = tokens[5];
    this.value = tokens[6];
  }

  @Override
  public String execute(CalendarSystemModel model) {
    try {
      if (property.equalsIgnoreCase("name")) {
        model.renameCalendar(name, value);
        return "Calendar renamed to: " + value;
      } else if (property.equalsIgnoreCase("timezone")) {
        model.changeCalendarTimezone(name, value);
        return "Calendar timezone updated to: " + value;
      } else {
        return "Error: Unsupported property '" + property + "'";
      }
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}
