package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;

/**
 * Command to exit the application.
 */
public class ExitCommand implements Command {

  @Override
  public void execute(CalendarModel calendar, CalendarView view) {
    view.displayMessage("Exiting calendar application.");
    // The controller will handle actual termination
  }
}