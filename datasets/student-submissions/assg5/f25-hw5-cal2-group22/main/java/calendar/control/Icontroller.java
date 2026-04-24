package calendar.control;

/**
 * Controller starting for running the application.
 * Provides separate methods for interactive and headless modes.
 */

public interface Icontroller {

  /**
   * Starts the interactive loop, reading commands from the console
   * until the user enters 'exit'.
   */
  void startInteractive();

  /**
   * Executes commands from a file (headless mode).
   * The file path is supplied by the active view.
   */
  void startHeadless();
}
