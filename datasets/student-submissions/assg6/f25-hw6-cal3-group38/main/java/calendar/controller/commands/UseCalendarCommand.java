package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;

/**
 * Command for setting the current calendar.
 */
public class UseCalendarCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a UseCalendarCommand.
   *
   * @param tokens the command tokens
   */
  public UseCalendarCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 4 || !"--name".equals(tokens[2])) {
        return "Error: Invalid use calendar command format. "
            + "Usage: use calendar --name <name>";
      }

      String name = tokens[3];
      controller.useCalendar(name);
      return "Now using calendar: " + name;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}