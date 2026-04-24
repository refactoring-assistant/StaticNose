package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;

/**
 * Command to display help information.
 */
public class HelpCommand implements CalendarCommand {

  @Override
  public void execute(CalendarService service, CalendarView view) {
    view.showHelp();
  }
}