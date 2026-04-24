package calendar.command;

import calendar.model.CalendarManager;
import calendar.view.CalendarTextView;

/**
 * Represents an executable command in the calendar application.
 */
public interface Command {

  /**
   * Executes the command.
   *
   * @param manager The calendar manager to act upon.
   * @param view  The view to display results to.
   */
  void execute(CalendarManager manager, CalendarTextView view);
}
