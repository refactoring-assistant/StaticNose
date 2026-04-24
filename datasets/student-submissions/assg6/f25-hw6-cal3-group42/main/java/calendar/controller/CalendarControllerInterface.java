package calendar.controller;

/**
 * Interface dictating the methods used by the calendar controller.
 */
public interface CalendarControllerInterface {

  /**
   * Run the calendar in interactive mode.
   */
  void interactiveMode();

  /**
   * Run the calendar in headless mode.
   *
   * @param fileName the script input file name with path
   */
  void headlessMode(String fileName);

  /**
   * Run the calendar in GUI mode.
   *
   */
  void guiMode();

  /**
   * Takes in user input and uses the parser to parse the command line to get an
   * interface object which can be delegated to the model to process.
   *
   * @param command input command string
   */
  void processCommand(String command);

  /**
   * Show start up syntax and exit due to incorrect command line arguments.
   */
  void showUsageAndExit();
}
