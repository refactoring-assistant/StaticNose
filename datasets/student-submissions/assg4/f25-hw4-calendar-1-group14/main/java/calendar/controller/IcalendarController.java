package calendar.controller;

import calendar.exceptions.InvalidCommandException;

/**
 * Interface for the Calendar Controller.
 * The calendar.controller coordinates between the calendar.model (calendar) and calendar.view,
 * processing commands and displaying results.
 */
public interface IcalendarController {

  /**
   * Execute a single command string.
   * Parses the command, executes it on the calendar,
   * and displays results through the calendar.view.
   *
   * @param commandString the command to execute
   * @throws InvalidCommandException if the command is invalid or cannot be parsed
   * @throws Exception if command execution fails
   */
  void executeCommand(String commandString) throws Exception;

  /**
   * Run the calendar.controller in interactive mode.
   * Continuously reads commands from user input until 'exit' command.
   */
  void runInteractive();

  /**
   * Run the calendar.controller in headless mode.
   * Reads and executes commands from a file.
   *
   * @param commandFilePath path to the file containing commands
   * @throws Exception if file reading or command execution fails
   */
  void runHeadless(String commandFilePath) throws Exception;
}