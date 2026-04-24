package calendar.controller;

/**
 * This interface holds the controller's functions.
 */
public interface CalendarControllerInterface {

  /**
   * Executes a given command by interacting with the model and view components. Processes the
   * input, determines the correct action, and then delegates task to model for calendar
   * operations.
   *
   * @param command the input command that needs to be executed
   */
  void executeCommand(String command);

  /**
   * Starts calendar application. Enters a loop, waiting for input commands, executing commands, and
   * interacting with view to show results, until application is exited.
   */
  void go();
}
