package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;

/**
 * Interface for all calendar commands.
 * Each command contains a specific operation on the calendar.
 */
public interface CalendarCommand {
  /**
   * Executes the command.
   *
   * @param service The calendar service.
   * @param view  The view to display results.
   * @throws IllegalArgumentException if the command cannot be executed.
   */
  void execute(CalendarService service, CalendarView view) throws IllegalArgumentException;
}