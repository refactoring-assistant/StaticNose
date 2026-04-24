package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;

/**
 * Command for editing calendars.
 */
public class EditCalendarCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs an EditCalendarCommand.
   *
   * @param tokens the command tokens
   */
  public EditCalendarCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 7 || !"--name".equals(tokens[2]) || !"--property".equals(tokens[4])) {
        return "Error: Invalid edit calendar command format. "
            + "Usage: edit calendar --name <name> --property <property> <newValue>";
      }

      String name = tokens[3];
      String property = tokens[5];
      String newValue = tokens[6];

      controller.editCalendar(name, property, newValue);
      return "Calendar '" + name + "' updated successfully. " + property + " set to " + newValue;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}