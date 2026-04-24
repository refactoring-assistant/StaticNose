package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;

/**
 * Exit command to exit the program.
 */
public class ExitCommand implements Command {

  @Override
  public String execute(CalendarSystemModel model) {
    return "Exiting program.";
  }
}