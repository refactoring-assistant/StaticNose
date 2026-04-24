package calendar.controller.command;

import calendar.model.manager.CalendarManager;
import calendar.view.CalendarView;

/**
 * Command to display help information showing all available commands.
 *
 * <p>Displays a comprehensive list of all calendar commands with their syntax
 * and descriptions to help users understand how to use the calendar application.
 */
public class HelpCommand implements Command {

  @Override
  public void execute(CalendarManager manager, CalendarView view) throws Exception {
    view.displayCommandOptions();
  }
}