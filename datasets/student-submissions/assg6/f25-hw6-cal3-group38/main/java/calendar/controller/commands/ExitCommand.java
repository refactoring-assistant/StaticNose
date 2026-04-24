package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.controller.Command;

/**
 * Command for exiting the application.
 */
public class ExitCommand implements Command {

  @Override
  public String execute(CalendarController controller) {
    // Return the message instead of calling System.exit
    // Let the view/runner handle the actual exit
    return "Goodbye!";
  }
}