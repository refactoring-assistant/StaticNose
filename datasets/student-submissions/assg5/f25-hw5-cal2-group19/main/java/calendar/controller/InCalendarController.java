package calendar.controller;

import calendar.exception.InvalidCommandException;

/**
 * Controller interface defining command execution contract.
 * Decouples view from command processing logic.
 */
public interface InCalendarController {

  /**
   * Executes a command string.
   *
   * @param commandString the command to execute
   * @throws InvalidCommandException if command is invalid
   */
  void executeCommand(String commandString) throws InvalidCommandException;

  /**
   * Starts the controller (begins accepting commands).
   */
  void start();

  /**
   * Shuts down the controller.
   */
  void shutdown();
}