package calendar.controller;

/**
 * Defines the interface for executing the calendar application.
 * Implementations handle user input, command dispatching,
 * and mode-specific execution (interactive or headless).
 */
public interface IcalendarExecution {

  /**
   * Starts the calendar in the specified mode.
   *
   * @param mode the execution mode, either interactive or headless
   * @param filePath optional file path used in headless mode to read commands;
   *                 may be null in interactive mode
   */
  void start(String mode, String filePath);
}