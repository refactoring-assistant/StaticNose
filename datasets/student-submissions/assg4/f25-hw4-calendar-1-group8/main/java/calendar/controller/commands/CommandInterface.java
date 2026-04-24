package calendar.controller.commands;

import calendar.model.InterfaceCalendarModel;
import calendar.view.InterfaceCalendarView;
import java.util.List;

/**
 * An interface for the Command Pattern. Each command handles
 * its own argument parsing, model calls, and view calls.
 */
public interface CommandInterface {

  /**
   * Executes the command.
   *
   * @param model The calendar model to operate on.
   * @param view  The view to display results to.
   * @param args  The list of command arguments (all tokens *after* the command itself).
   * @throws Exception if parsing fails or the model throws an error.
   */
  void execute(InterfaceCalendarModel model, InterfaceCalendarView view, List<String> args)
      throws Exception;
}