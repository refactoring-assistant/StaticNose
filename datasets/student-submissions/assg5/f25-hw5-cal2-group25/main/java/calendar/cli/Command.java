package calendar.cli;

import calendar.manager.CalendarManager;
import calendar.model.CalendarModel;
import java.io.PrintStream;

/**
 * Represents an executable command within the calendar CLI.
 */
public interface Command {
  /**
   * Executes the command on the given calendar model.
   *
   * @param manager the calendar model to operate on.
   * @param out     the output stream used to display messages or results.
   */
  void execute(CalendarManager manager, PrintStream out);
}