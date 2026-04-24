package controller;

/**
 * Interface defining the contract for calendar controllers.
 * This interface provides a single point of entry for executing
 * commands against the calendar system.
 */
public interface IcalendarController {
  /**
   * Execute a command string.
   *
   * @param command The command to execute
   * @return Result of the command execution
   */
  CommandResult executeCommand(String command);
}