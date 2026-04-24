package calendar.control.commands;

import calendar.control.results.CommandResult;

/**
 * Executable command unit.
 * Implementations encapsulate a single user instruction (create, edit, print, etc.)
 * and can be executed by the controller.
 */

public interface Icommand {

  /**
   * Executes the command using the model and/or view provided at construction time.
   *
   * @return CommandResult indicating success or failure with appropriate message
   */
  CommandResult execute();
}
