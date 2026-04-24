package calendar.controller;

import java.io.IOException;

/**
 * Controller interface for the calendar application.
 * Handles user interaction in both interactive and headless modes.
 */
public interface CalendarController {
  /**
   * Run the controller in interactive mode, accepting commands from user input.
   *
   * @throws IOException if there's an error reading input
   */
  void runInteractive() throws IOException;

  /**
   * Run the controller in headless mode, reading commands from a file.
   *
   * @param commandsFile path to the file containing commands
   * @throws IOException if there's an error reading the file
   */
  void runHeadless(String commandsFile) throws IOException;
}
