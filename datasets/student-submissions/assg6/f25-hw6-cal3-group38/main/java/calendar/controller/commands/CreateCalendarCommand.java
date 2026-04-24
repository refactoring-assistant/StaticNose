package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;

/**
 * Command for creating calendars.
 */
public class CreateCalendarCommand implements Command {
  private final String[] tokens;

  /**
   * Constructs a CreateCalendarCommand.
   *
   * @param tokens the command tokens
   */
  public CreateCalendarCommand(String[] tokens) {
    this.tokens = tokens;
  }

  @Override
  public String execute(CalendarController controller) {
    try {
      if (tokens.length < 6 || !"--name".equals(tokens[2]) || !"--timezone".equals(tokens[4])) {
        return "Error: Invalid create calendar command format. "
            + "Usage: create calendar --name <calName> --timezone <timezone>";
      }

      String name = tokens[3];
      String timezone = tokens[5];

      controller.createCalendar(name, timezone);
      return "Calendar '" + name + "' created successfully with timezone " + timezone;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}