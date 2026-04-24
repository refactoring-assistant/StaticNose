package calendar.command;

import calendar.controller.CalendarControllerImpl;
import calendar.controller.CalendarManagerControllerImpl;

/**
 * Executes commands using a parser and controllers.
 */
public class CommandExecutor {

  private final CommandParser parser;

  /**
   * The constructor to execute the commands.
   *
   * @param eventController   is the controller that handles event commands.
   * @param managerController is the controller than handles calendars.
   */
  public CommandExecutor(CalendarControllerImpl eventController,
                         CalendarManagerControllerImpl managerController) {
    this.parser = new CommandParser(eventController, managerController);
  }

  /**
   * Parse and execute a single command string.
   *
   * @param input command line
   * @return result message
   */
  public String execute(String input) {
    if (input == null || input.isBlank()) {
      return "Error: Empty command";
    }

    try {
      var command = parser.parse(input);
      if (command == null) {
        return "Error: Unknown command '" + input + "'";
      }
      return command.execute();
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }
}
