package controller.command;

import controller.CommandResult;
import model.Icalendar;

/**
 * Command implementation for exiting the calendar application.
 * This command signals that the application should terminate gracefully.
 *
 */
public class ExitCommand implements Command {

  /**
   * Executes the exit command.
   *
   * @param calendar the calendar model (not used by this command)
   * @return a CommandResult with shouldExit flag set to true
   */
  @Override
  public CommandResult execute(Icalendar calendar) {
    return new CommandResult(true, "Goodbye!", true);
  }
}
